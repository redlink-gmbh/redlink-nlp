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

import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Span;
import io.redlink.nlp.model.Span.SpanTypeEnum;
import io.redlink.nlp.model.SpanCollection;
import io.redlink.nlp.regex.ner.RegexNerProcessor.NamedEntity;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extract patterns based on regexes.
 * <p>
 * Subclasses need to provide a list of {@link NamedPattern}.
 * For matching {@link NamedPattern} the {@link #createNamedEntity(String, MatchResult)}
 * is called with the name of the matching pattern and the matching result.
 *
 * @see RegexNerDetector
 * @see NamedRegexDetector
 */
public abstract class RegexNamedEntityFactory {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public final void process(SpanCollection section, String lang, List<NamedEntity> namedEntities) {
        log.debug("extract Named Entities for {} (doc lang: {})", section, lang);
        //check for section specific language annotations
        if (SpanTypeEnum.TextSection == section.getType()) {
            String sectionLang = section.getAnnotation(NlpAnnotations.LANGUAGE_ANNOTATION);
            if (sectionLang != null) {
                log.debug("  - with section specific language: {}", sectionLang);
                lang = sectionLang;
            }
        }
        final List<NamedPattern> patterns = getRegexes(section, lang);
        Iterator<Span> subSections = section.getEnclosed(EnumSet.of(SpanTypeEnum.TextSection, SpanTypeEnum.Sentence));
        SpanCollection active = section;
        while (subSections.hasNext()) {
            SpanCollection subSection = (SpanCollection) subSections.next();
            if (subSection.getStart() >= active.getEnd()) {
                process(active.getType(), active.getStart(), active.getSpan(), patterns, namedEntities);
            } else if (subSection.getEnd() < active.getEnd()) {
                if (subSection.getStart() > active.getStart()) {
                    process(active.getType(), active.getStart(), active.getSpan().substring(0, subSection.getStart() - active.getStart()), patterns, namedEntities);
                }
            }
            active = subSection;
        }
        process(active.getType(), active.getStart(), active.getSpan(), patterns, namedEntities);

    }

    private void process(SpanTypeEnum spanType, int offset, String text, List<NamedPattern> patterns, List<NamedEntity> namedEntities) {
        if (log.isTraceEnabled()) {
            log.trace(" - process {} [{}, {}] - {}", spanType, offset, offset + text.length(), StringUtils.abbreviate(text, 50));
        }
        for (NamedPattern namedPattern : patterns) {
            final Matcher matcher = namedPattern.getPattern().matcher(text);
            while (matcher.find()) {
                final NamedEntity ne = createNamedEntity(namedPattern.getName(), matcher.toMatchResult());
                if (ne == null) continue;
                ne.setOffset(offset);
                namedEntities.add(ne);
                log.debug("add {}", ne);
            }
        }
    }

    /**
     * Creates a token for the parsed {@link MatchResult} originating from the
     * {@link NamedPattern} with the parsed name
     *
     * @param patternName the name of the {@link NamedPattern}
     * @param match       the {@link MatchResult}
     * @return the {@link NamedEntity} or <code>null</code> if no Token was created.
     */
    protected abstract NamedEntity createNamedEntity(String patternName, MatchResult match);

    /**
     * Getter for the {@link NamedPattern} to be used by the {@link RegexNerProcessor}
     *
     * @param section  the section of an {@link AnalyzedText} to be analyzed with the
     *                 returned patterns
     * @param language the language of the parsed text section
     * @return the list of {@link NamedPattern} or an empty list if none
     */
    protected abstract List<NamedPattern> getRegexes(SpanCollection section, String language);

    /**
     * A regex {@link Pattern} with an assigned Name. The name is parsed
     * to the {@link RegexNamedEntityFactory#createNamedEntity(String, MatchResult)}
     * method so that implementers know what pattern caused the parsed
     * {@link MatchResult}.
     *
     * @author Rupert Westenthaler
     */
    public static final class NamedPattern {

        private final String name;
        private final Pattern pattern;

        public NamedPattern(String name, Pattern pattern) {
            assert StringUtils.isNotBlank(name);
            this.name = name;
            assert pattern != null;
            this.pattern = pattern;
        }

        public String getName() {
            return name;
        }

        public Pattern getPattern() {
            return pattern;
        }
    }
}
