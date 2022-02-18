/*
 * Copyright (c) 2016-2022 Redlink GmbH.
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Simplifies the Implementation of a {@link RegexNamedEntityFactory} in cases where
 * <ol>
 * <li> all created {@link NamedEntity NamedEntities} do use a single {@link Token.Type}
 * <li> the created {@link NamedEntity NamedEntities} do use the {@link NamedPattern#getName()} as {@link Token#getValue()}
 * </ol>
 * Subclasses just need to implement the {@link #loadPatterns()} Method that
 * is called once and is expected to provide the list of Regex patterns.
 * <p>
 * The {@link #acceptMatch(String, MatchResult)} provides a callback that allows to filter out
 * unwanted matches. The default implementation will filter out all
 * {@link StringUtils#isBlank(CharSequence) blank} matches
 *
 * @author Rupert Westenthaler
 */
public abstract class NamedRegexDetector extends RegexNamedEntityFactory {

    private final NerTag type;
    private Map<String, List<NamedPattern>> langPatterns = null;

    public NamedRegexDetector(NerTag type) {
        this.type = type;
    }

    public NerTag getType() {
        return type;
    }

    @PostConstruct
    public final void init() throws IOException {
        this.langPatterns = loadPatterns();
    }

    protected abstract Map<String, List<NamedPattern>> loadPatterns() throws IOException;


    @Override
    protected List<NamedPattern> getRegexes(SpanCollection section, String lang) {
        if (langPatterns == null) {
            synchronized (this) {
                if (langPatterns == null) {
                    try {
                        langPatterns = loadPatterns();
                    } catch (IOException e) {
                        log.error("Unable to load NamedPatterns", e);
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
    protected NamedEntity createNamedEntity(String name, MatchResult match) {
        if (acceptMatch(name, match)) {
            log.debug("Create {} Token for match {} (pattern name: {})", type, match, name);
            final NamedEntity ne = new NamedEntity(match.start(), match.end(), type);
            ne.setConfidence(1f);
            return ne;
        } else {
            log.trace("Ignore match {} (pattern name: {})", match, name);
            return null;
        }

    }

    /**
     * If the match for the Pattern with the parsed name should be accepted.
     * The default implementation accepts all none blank {@link MatchResult#group()}
     *
     * @param name  the name of the pattern
     * @param match the match
     * @return <code>true</code> if a {@link Token} should be created for this match. Otherwise <code>false</code>
     */
    protected boolean acceptMatch(String name, MatchResult match) {
        return StringUtils.isNotBlank(match.group());
    }

}
