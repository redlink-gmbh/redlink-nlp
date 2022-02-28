/*******************************************************************************
 * Copyright (c) 2022 Redlink GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package io.redlink.nlp.time.duckling;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.ProcessingData.Configuration;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.SpanCollection;
import io.redlink.nlp.model.section.SectionTag;
import io.redlink.nlp.model.temporal.DateTimeValue;
import io.redlink.nlp.model.util.NlpUtils;
import io.redlink.nlp.time.DateUtils;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Component;

import static io.redlink.nlp.time.duckling.DucklingTimeParser.DATE_FORMAT;

@Component
public class DucklingTimeProcessor extends Processor {

    /**
     * If <code>true</code> (default) the temporal context is reset for every
     * parsed context. Typically you want this <ul>
     * <li>enabled when parsing news articles
     * - as times mentioned in different paragraphs are typically relative to
     * the temporal context of the article.
     * <li>disabled when parsing conversations - as the answer to a question
     * usually uses the context as set by the question
     * </ul>
     */ //TODO: implement
    public final static String RESET_SECTION_CONTEXT = "time.duckling.resetsectioncontext";
    private final static boolean DEFAULT_RESET_SECTION_CONTEXT = true;

    /**
     * If low confidence Dates should be returned (default: disabled)
     */
    public final static String INCLUDE_LATENT = "time.duckling.includelatent";

    @org.springframework.beans.factory.annotation.Value("${time.duckling.includelatent:false}")
    private boolean includeLatent = false;

    private final DucklingTimeParser ducklingParser;

    public DucklingTimeProcessor() {
        super("time.duckling", "Duckling Time", Phase.ner);
        ducklingParser = new DucklingTimeParser();
    }

    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return Collections.singletonMap("time.duckling.includelatent", includeLatent);
    }

    @Override
    protected void init() throws Exception {
        ducklingParser.setIncludeLatent(includeLatent);
        ducklingParser.init();
    }

    @Override
    protected void doProcessing(ProcessingData processingData) {
        AnalyzedText at = NlpUtils.getOrInitAnalyzedText(processingData);
        if (at == null) {
            log.trace("Unable to extract date/time values from {} because no AnalyzedText was present or could be created.", processingData);
            return;
        }
        String lang = processingData.getLanguage();
        if (lang == null) {
            log.trace("Unable to extract date/time values from  {} because the language is unknown", processingData);
            return;
        }
        if (lang != null && lang.length() >= 2) {
            lang = lang.toLowerCase(Locale.ROOT);
        } else {
            log.trace("Inavalid language '{}' annotatoed for {}. Will not extract date/time values", lang, processingData);
            return;
        }
        log.debug(" - language: {}", lang);
        boolean includeLatent = processingData.getConfiguration(INCLUDE_LATENT, this.includeLatent);
        log.trace(" - include latent: {}", includeLatent);
        if (!ducklingParser.isLanguageSupported(lang)) {
            log.trace("language '{}' of {} is not supported by {}. WIll not extract date/tume values", lang, processingData, getName());
            return;
        }
        final Date globalTempContext;
        Value<Date> tempContextAnno = at.getValue(NlpAnnotations.TEMPORAL_CONTEXT);
        if (tempContextAnno != null) {
            globalTempContext = tempContextAnno.value();
        } else { //look for a configured Date
            Date date = DateUtils.toDate(processingData.getConfiguration().get(Configuration.TEMPORAL_CONTEXT));
            globalTempContext = date != null ? date : new Date();
        }
        if (log.isDebugEnabled()) {
            log.debug(" - global temporal context: {}", DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(globalTempContext));
        }
        //TODO: implement
        //boolean resetSectionContext = processingData.getConfiguration(RESET_SECTION_CONTEXT, DEFAULT_RESET_SECTION_CONTEXT);

        Iterator<? extends SpanCollection> sections = at.getSections();
        if (!sections.hasNext()) {
            sections = Collections.singleton(at).iterator();
        }
        int processedUntil = -1;
        while (sections.hasNext()) {
            SpanCollection section = sections.next();
            if (isContentSection(section) && section.getEnd() > processedUntil) {
                String content;
                int offset;
                if (section.getStart() < processedUntil) {
                    log.trace("partly overlapping {} (already precessed until: {}", section, processedUntil);
                    content = at.getText().subSequence(processedUntil, section.getEnd()).toString();
                    offset = processedUntil;
                } else {
                    offset = section.getStart();
                    content = section.getSpan();
                }
                //NOTE: we do support temporal contexts on section levels
                tempContextAnno = section.getValue(NlpAnnotations.TEMPORAL_CONTEXT);
                log.debug("process {}", section);
                Date tempContext = tempContextAnno != null ? tempContextAnno.value() : globalTempContext;
                if (log.isDebugEnabled()) {
                    log.debug(" - temporal context: {}", DATE_FORMAT.get().format(tempContext));
                }
                List<DateToken> dateTokens = null;
                try {
                    dateTokens = ducklingParser.parse(content, lang, tempContext, includeLatent);
                    processedUntil = section.getEnd();
                } catch (RuntimeException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Failed to parse {}: {} with {}[lang: {}] ({} - {}",
                                section, StringUtils.replaceEach(StringUtils.abbreviate(section.getSpan(), 40),
                                        new String[]{"\n", "\t", "\r"}, new String[]{"\\n", "\\t", "\\r"}),
                                ducklingParser.getClass().getSimpleName(), lang,
                                e.getClass().getSimpleName(), e.getMessage());
                    }
                    log.debug("STACKTRACE:", e);
                }
                if (dateTokens != null) {
                    for (DateToken dateToken : dateTokens) {
                        Chunk chunk = at.addChunk(offset + dateToken.getOffsetStart(), offset + dateToken.getOffsetEnd());
                        DateTimeValue dtValue = new DateTimeValue();
                        dtValue.setInstant(dateToken.isInstant());
                        dtValue.setStart(dateToken.getStart());
                        dtValue.setEnd(dateToken.getEnd());
                        chunk.addValue(NlpAnnotations.TEMPORAL_ANNOTATION, Value.value(dtValue, dateToken.getConfidence()));
                    }
                }
            } //else overlapping section ... already processed
        }
    }

    private boolean isContentSection(SpanCollection section) {
        Value<SectionTag> sectionTag = section.getValue(NlpAnnotations.SECTION_ANNOTATION);
        boolean isContentSection = sectionTag == null || sectionTag.value().getSection().isContentSection();
        return isContentSection;
    }

}
