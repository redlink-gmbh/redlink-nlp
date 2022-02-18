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

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.util.NlpUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Extract Tokens based on Regex
 */
@Component
public class RegexNerProcessor extends Processor {

    private List<RegexNamedEntityFactory> tokenFactories = Collections.emptyList();

    public RegexNerProcessor() {
        this(Collections.emptyList());
    }

    @Autowired(required = false)
    public RegexNerProcessor(List<RegexNamedEntityFactory> tokenFactories) {
        super("ner.regex", "Regex NER Processor", Phase.ner);
        this.tokenFactories = tokenFactories;
    }

    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return Collections.emptyMap();
    }

    @Override
    protected void init() {
        log.debug("Initializing with {} token-factories", tokenFactories.size());
    }

    @Override
    protected void doProcessing(ProcessingData processingData) {
        final AnalyzedText at = NlpUtils.getOrInitAnalyzedText(processingData);
        if (at == null) {
            log.debug("unable to process {} because no plain/text content is present");
            return;
        }
        final String lang = processingData.getLanguage();

        final List<NamedEntity> nes = new ArrayList<>();
        tokenFactories.forEach(
                regexNamedEntityFactory -> regexNamedEntityFactory.process(at, lang, nes)
        );
        //sort the tokens
        Collections.sort(nes);
        //filter for tokens contained in an other token with the same type
        Map<String, NamedEntity> activeNamedEntities = new HashMap<>();
        for (NamedEntity ne : nes) {
            NamedEntity active = activeNamedEntities.get(ne.getTypeId());
            if (active == null || ne.getEnd() > active.getEnd()) {
                Chunk chunk = at.addChunk(ne.getStart(), ne.getEnd());
                chunk.addValue(NlpAnnotations.NER_ANNOTATION, Value.value(ne.getTag(), ne.getConfidence()));
                //if the Named Entity provides a Lemma we also need to set a lemma annotation
                if (ne.getLemma() != null && !chunk.getSpan().equals(ne.getLemma())) {
                    chunk.addAnnotation(NlpAnnotations.LEMMA_ANNOTATION, ne.getLemma());
                }
                activeNamedEntities.put(ne.getTypeId(), ne);
            } else {
                log.debug("filter Named Entity {} contained in {}", ne, active);
            }
        }
    }

    public static final class NamedEntity implements Comparable<NamedEntity> {

        private int offset;
        private final int start;
        private final int end;
        private final NerTag tag;
        private double confidence = Value.UNKNOWN_PROBABILITY;
        private String lemma;

        public NamedEntity(int start, int end, NerTag tag) {
            assert start >= 0;
            this.start = start;
            assert end > start;
            this.end = end;
            assert tag != null && tag.getTag() != null;
            this.tag = tag;
        }

        /**
         * Setter for the offset to the start of the whole content.
         * Named Entities are typically constructed for sub-sections
         * (e.g. sentences) of the whole content. Because of that
         * start/end offsets as returned by the Regex Patterns are
         * relative to the start of the section and not to the
         * start of the content. So the {@link RegexNamedEntityFactory}
         * does set the offset to ensure that {@link #getStart()} and
         * {@link #getEnd()} are relative to the content as a whole
         *
         * @param offset the offset of the analyzed section to the
         *               content as a whole
         */
        protected final void setOffset(int offset) {
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }

        public int getStart() {
            return start + offset;
        }

        public int getEnd() {
            return end + offset;
        }

        public NerTag getTag() {
            return tag;
        }

        public void setConfidence(double confidnece) {
            this.confidence = confidnece;
        }

        public double getConfidence() {
            return confidence;
        }

        /**
         * Internal used to group named entities based on their type
         */
        String getTypeId() {
            return tag.getType().equals(NerTag.NAMED_ENTITY_UNKOWN) ?
                    new StringBuilder(NerTag.NAMED_ENTITY_UNKOWN).append(':').append(tag.getTag()).toString() : tag.getType();
        }

        public void setLemma(String lemma) {
            this.lemma = lemma;
        }

        public String getLemma() {
            return lemma;
        }

        @Override
        public int compareTo(NamedEntity o) {
            int c = Integer.compare(getStart(), o.getStart());
            return c == 0 ? Integer.compare(o.getEnd(), getEnd()) : c;
        }

        @Override
        public String toString() {
            return "NamedEntity [span=[" + getStart() + "," + getEnd() + "], type=" + tag.getType() + ", tag=" + tag.getTag() + "]";
        }

    }

}
