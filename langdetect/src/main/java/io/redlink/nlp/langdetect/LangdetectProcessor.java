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

package io.redlink.nlp.langdetect;

import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Section;
import io.redlink.nlp.model.section.SectionStats;
import io.redlink.nlp.model.section.SectionTag;
import io.redlink.nlp.model.util.NlpUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.redlink.nlp.model.NlpAnnotations.LANGUAGE_ANNOTATION;
import static io.redlink.nlp.model.NlpAnnotations.SECTION_CLASSIFICATION_CONTENT_SECTION;

@Component
public class LangdetectProcessor extends Processor {

    private static final Logger LOG = LoggerFactory.getLogger(LangdetectProcessor.class);

    private static final int MAX_CONTENT_SECTIONS = 20;

    private static final float MIN_RANKING = 0.5f;

    private static final String KEY = "nlp.langdetect";
    public static final String LANGDETECT_MIN_CONF = KEY + ".min-conf";

    public static final float DEFAULT_MIN_CONF = 0.7f;

    private static final int MIN_CONTENT_LENGTH = 15;
    private static final int DEF_CONTENT_LENGTH = 30;
    private static final float MAX_WHITESPACE_FRACTION = 0.35f;
    private static final float DEF_WHITESPACE_FRACTION = 0.25f;
    private static final float MIN_ALPHA_FRACTION = 0.65f;

    private final static Map<String, Object> DEFAULT_CONFIG;

    static {
        Map<String, Object> c = new HashMap<String, Object>();
        c.put(LANGDETECT_MIN_CONF, DEFAULT_MIN_CONF);
        DEFAULT_CONFIG = Collections.unmodifiableMap(c);
    }

    protected LanguageIdentifier langIdentifier;

    @Autowired
    public LangdetectProcessor(LanguageIdentifier languageIdentifier) {
        super(KEY, "Lanugage Detector", Phase.langDetect);
        this.langIdentifier = languageIdentifier;
    }

    @Override
    protected void init() throws Exception {
        langIdentifier.loadProfiles();
    }

    @Override
    public void doProcessing(ProcessingData pd) {

        AnalyzedText at = NlpUtils.getOrInitAnalyzedText(pd);
        if (at == null) {
            LOG.debug("Unable to detect language for {} because no AnalyzedText is present or can be created", pd);
            return;
        }

        float minConf = pd.getConfiguration(LANGDETECT_MIN_CONF, DEFAULT_MIN_CONF);
        if (minConf <= 0.5) {
            LOG.warn("configured '{}'={} MUST BE > 0.5f (using default: {})", LANGDETECT_MIN_CONF, minConf, DEFAULT_MIN_CONF);
            minConf = DEFAULT_MIN_CONF;
        }
        LOG.trace("  - minConf: {}", minConf);
        float inclConf = 1 - minConf;

        //process the sections and collect the content to be used for detection the language on Document level
        StringBuilder content = new StringBuilder();
        if (at.getSections().hasNext()) {
            List<Pair<Float, Section>> contentSections = getRankedContentSections(at);
            //detect languages for content sections
            contentSections.forEach(contentSection -> detectLanguages(contentSection.getRight(), inclConf));

            //sort the rankings based on content ranking
            Collections.sort(contentSections, new Comparator<Pair<Float, Section>>() {

                @Override
                public int compare(Pair<Float, Section> o1, Pair<Float, Section> o2) {
                    return o2.getKey().compareTo(o1.getKey());
                }

            });

            float ranking = Float.MAX_VALUE;
            int numSections = 0;
            for (Iterator<Pair<Float, Section>> it = contentSections.iterator(); it.hasNext() && ranking >= MIN_RANKING && numSections < MAX_CONTENT_SECTIONS; ) {
                Pair<Float, Section> p = it.next();
                ranking = p.getKey();
                if (ranking > 0.5) {
                    LOG.debug("{} -> {}: {}", p.getKey(), p.getValue(), p.getValue().getSpan());
                    content.append(p.getValue().getSpan()).append('\n');
                    numSections++;
                }
            }
        } else if (StringUtils.isNotBlank(at.getSpan())) { //content could not be retrieved from the AnalyzedText Sections
            content.append(at.getSpan());
        }

        if (content.length() < MIN_CONTENT_LENGTH) {
            LOG.debug(" - unable to detect language for {} because content "
                    + "is to short (length: {}, required: {})", pd, content.length(), MIN_CONTENT_LENGTH);
            return;
        }

        try {
            //try to get the content from the content sections in the analyzed text
            for (Language lang : langIdentifier.getLanguages(content.toString())) {
                if (lang.prob >= inclConf) {
                    LOG.debug(" - language: {} (conf: {})", lang.lang, lang.prob);
                    Value<String> langValue = Value.value(lang.lang, lang.prob);
                    at.addValue(NlpAnnotations.LANGUAGE_ANNOTATION, langValue);
                    if (lang.prob >= minConf) {
                        pd.addValue(Annotations.LANGUAGE, langValue); //high level language annotation
                        //NOTE: usage of the NlpAnnotations.LANGUAGE_ANNOTATION on AnalyzedText level is deprecated
                        //at.addValue(NlpAnnotations.LANGUAGE_ANNOTATION, langValue); //low level annotation
                        //TODO: maybe we would like to have section level language annotations (this would require
                        //      some refactoring)
                    }
                } else {
                    LOG.trace(" - low confidence language: {} (conf: {})", lang.lang, lang.prob);
                }
            }
        } catch (LangDetectException e) {
            LOG.debug("Unable to detect language for document {} (message: {})", pd, e.getMessage());
            LOG.trace("STACKTRACE:", e);
        }
    }

    private void detectLanguages(Section section, float inclConf) {
        try {
            langIdentifier.getLanguages(section.getSpan()).stream()
                    .filter(lang -> lang.prob >= inclConf)
                    .map(lang -> Value.value(lang.lang, lang.prob))
                    .forEach(detected -> section.addValue(LANGUAGE_ANNOTATION, detected));
            if (LOG.isDebugEnabled()) {
                LOG.debug(" > {} (langs: {})", section, section.getValues(LANGUAGE_ANNOTATION));
            } else if (LOG.isTraceEnabled()) {
                LOG.debug(" > {}: {} (langs: {})", section, StringUtils.abbreviate(section.getSpan(), 60), section.getValues(LANGUAGE_ANNOTATION));
            }
        } catch (LangDetectException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unable to detect language for section {} (message: {})", section, e.getMessage());
            } else if (LOG.isTraceEnabled()) {
                LOG.debug("Unable to detect language for section {} (content: {})", section, section.getSpan(), e);
            }
        }
    }

    private List<Pair<Float, Section>> getRankedContentSections(AnalyzedText at) {
        List<Pair<Float, Section>> contentSections = new ArrayList<>();
        Iterator<Section> sections = at.getSections();
        while (sections.hasNext()) {
            Section section = sections.next();
            Value<SectionTag> sectionTag = section.getValue(NlpAnnotations.SECTION_ANNOTATION);
            Value<SectionStats> sectionStatsValue = section.getValue(NlpAnnotations.SECTION_STATS_ANNOTATION);
            SectionStats sectionStats = sectionStatsValue == null ? null : sectionStatsValue.value();
            if (LOG.isTraceEnabled()) {
                LOG.trace(" - {}: {} {} - {}", section, sectionTag, sectionStats, StringUtils.abbreviate(section.getSpan(), 60));
            }
            //assume all sections with >= 80 chars and headings as content sections
            Boolean contentSection = null;
            if (sectionTag != null) {
                switch (sectionTag.value().getSection()) {
                    case heading: //use all headlines for language detection
                        contentSection = Boolean.TRUE;
                        break;
                    case page: //pages are divided in smaller sections so they can be ignored
                        contentSection = Boolean.FALSE;
                        break;
                    default: //no op

                }
            }
            if (contentSection == null) {
                if (sectionStats == null) {
                    sectionStats = calcStats(section.getSpan());
                }
                contentSection = isContentSection(sectionStats);
            }
            section.addAnnotation(SECTION_CLASSIFICATION_CONTENT_SECTION, contentSection);
            if (contentSection) {
                LOG.trace("  ... content section ({})", sectionStats);
                contentSections.add(new ImmutablePair<Float, Section>(contentRank(sectionStats), section));
            }
        }
        return contentSections;
    }

    private SectionStats calcStats(String section) {
        int count = section.length();
        int alpha = 0;
        int digit = 0;
        int ws = 0;
        for (int i = 0; i < count; i++) {
            int c = section.codePointAt(i);
            if (Character.isWhitespace(c)) {
                ws++;
            } else if (Character.isDigit(c)) {
                digit++;
            } else if (Character.isAlphabetic(c)) {
                alpha++;
            }
        }
        return new SectionStats(count, alpha, digit, ws);
    }

    /**
     * Calculates the ranking of the content based on the fractions of
     * alpha and numeric with whitespace chars. Also deceases rankings
     * for sections with less as {@link #DEF_CONTENT_LENGTH} chars and
     * a higher as {@link #DEF_WHITESPACE_FRACTION} whitespace char
     * fraction
     *
     * @param stats
     */
    private float contentRank(SectionStats stats) {
        float noneWs = stats.getNumChars() - stats.getNumWhitespace();
        if (noneWs <= 0f) {
            return 0f;
        }
        float fraction = stats.getNumAlpha() / noneWs;
        fraction = fraction * stats.getNumAlphaAndDigit() / noneWs;
        if (stats.getFractionWhitespace() > DEF_CONTENT_LENGTH) {
            fraction = fraction * (1f - stats.getFractionWhitespace());
        }
        if (stats.getNumAlpha() < DEF_CONTENT_LENGTH) {
            fraction = fraction * (stats.getNumAlpha() / (float) DEF_CONTENT_LENGTH);
        }
        return fraction;
    }

    /**
     * Checks if a section can be considered as natural language content and is
     * therefore be used for language detection
     *
     * @param stats the statistics for the section
     * @return the state
     */
    private boolean isContentSection(SectionStats stats) {
        return stats != null && stats.getNumAlpha() >= MIN_CONTENT_LENGTH &&
                stats.getFractionWhitespace() <= MAX_WHITESPACE_FRACTION &&
                stats.getFractionAlpha() >= MIN_ALPHA_FRACTION;
    }

    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return DEFAULT_CONFIG;
    }

}
