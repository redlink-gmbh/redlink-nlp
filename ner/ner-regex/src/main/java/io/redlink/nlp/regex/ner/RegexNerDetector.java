/*
 * Copyright (c) 2022 Redlink GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.redlink.nlp.regex.ner;

import io.redlink.nlp.model.SpanCollection;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.regex.ner.RegexNerProcessor.NamedEntity;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Simplifies the Implementation of a {@link RegexNamedEntityFactory} in cases where
 * <ol>
 * <li> all created {@link NamedEntity NamedEntities} do use a single {@link NerTag}
 * <li> the created {@link NamedEntity NamedEntities} do use {@link MatchResult#group()} as {@link Token#getValue()}
 * </ol>
 * Subclasses just need to implement the {@link #initPatterns()} Method that
 * is called once and is expected to provide the list of Regex patterns.
 * The {@link #acceptMatch(String)} provides an callback so that unwanted matches can be
 * filtered out. The default implementation will filter all {@link StringUtils#isBlank(CharSequence) blank}
 * matches
 *
 * @author Rupert Westenthaler
 */
public abstract class RegexNerDetector extends RegexNamedEntityFactory {

    private final String name;
    private final NerTag type;
    private Map<String, List<NamedPattern>> langPatterns;


    public RegexNerDetector(String name, NerTag type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public NerTag getType() {
        return type;
    }

    @PostConstruct
    protected final void init() throws IOException {
        this.langPatterns = loadPatterns();
    }

    private Map<String, List<NamedPattern>> loadPatterns() throws IOException {
        Map<String, List<NamedPattern>> langPatterns = new HashMap<>();
        Map<String, List<Pattern>> patternMap = initPatterns();
        int numPattern = 0;
        log.debug("load Patterns for {}", getClass().getSimpleName());
        if (patternMap != null) {
            for (Entry<String, List<Pattern>> e : initPatterns().entrySet()) {
                List<NamedPattern> patterns = new LinkedList<>();
                String lang = e.getKey();
                if (lang != null) {
                    lang = lang.toLowerCase(Locale.ROOT);
                }
                for (Pattern pattern : e.getValue()) {
                    if (pattern != null) {
                        patterns.add(new NamedPattern(name, pattern));
                        numPattern++;
                    }
                }
                if (!patterns.isEmpty()) {
                    log.debug("loaded {} patterns for language {}", patterns.size(), lang);
                    langPatterns.put(lang, patterns);
                }
            }
        } else {
            log.warn(" {}#initPatterns() has returned NULL. No Regex Pattern for extracting NamedEntities are active", getClass().getSimpleName());
        }
        if (numPattern == 0) {
            log.warn("{}#initPatterns() has not returned any Regex Pattern for extracting NamedEntities.", getClass().getSimpleName());
        } else {
            log.debug("loaded {} patterns for {} language(s)", numPattern, langPatterns.size());
        }
        return langPatterns;
    }

    protected abstract Map<String, List<Pattern>> initPatterns() throws IOException;

    @Override
    protected final List<NamedPattern> getRegexes(SpanCollection section, String lang) {
        if (langPatterns == null) {
            synchronized (this) {
                if (langPatterns == null) {
                    try {
                        langPatterns = loadPatterns();
                    } catch (IOException e) {
                        log.error("Unable to load Regex Patterns!", e);
                    }
                }
            }
        }
        String normLang = lang == null ? null : lang.toLowerCase(Locale.ROOT).split("-_")[0];
        List<NamedPattern> patterns = langPatterns.get(normLang);
        if (lang != null) {
            //Patterns for the NULL language are used for all languages
            List<NamedPattern> defPatterns = langPatterns.get(null);
            if (CollectionUtils.isNotEmpty(defPatterns)) {
                if (CollectionUtils.isNotEmpty(patterns)) {
                    patterns = ListUtils.union(patterns, defPatterns);
                } else {
                    patterns = defPatterns;
                }
            }
        }
        //ensure we return a read-only list so that no one messes around with the patterns we manage internally
        return patterns == null ? patterns : Collections.unmodifiableList(patterns);
    }

    @Override
    protected final NamedEntity createNamedEntity(String patternName, MatchResult match) {
        if (acceptMatch(match.group())) {
            log.debug("[{}] Create Token for [{},{}] - {}", name, match.start(), match.end(), match.group());
            acceptMatch(match.group());
            final NamedEntity ne = new NamedEntity(match.start(), match.end(), type);
            ne.setConfidence(1f);
            return ne;
        } else {
            log.debug("[{}] No Token for [{},{}] - {}", name, match.start(), match.end(), match.group());
            return null;
        }
    }

    /**
     * Can be overwritten to validate matches based on the {@link Token#getValue()}.
     * The default implementation accepts all none black values
     *
     * @param value the value
     */
    protected boolean acceptMatch(String value) {
        return StringUtils.isNotBlank(value);
    }

}
