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

package io.redlink.nlp.stopword;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.pos.PosSet;
import io.redlink.nlp.model.util.NlpUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import static io.redlink.nlp.stopword.StopwordExtractorConfiguration.PROP_CASE_SENSITIVE;
import static io.redlink.nlp.stopword.StopwordExtractorConfiguration.PROP_USE_POS;

@Component
@EnableConfigurationProperties(StopwordExtractorConfiguration.class)
public class StopwordExtractor extends Processor {


    /**
     * Never tag Nouns and Verbs as Stopwords
     */
    private static final PosSet NO_STOPWORD_LEX_CAT = PosSet.union(PosSet.NOUNS, PosSet.ADJECTIVES, PosSet.VERBS);

    private StopwordListRegistry registry;

    private final StopwordExtractorConfiguration config;

    @Autowired
    public StopwordExtractor(StopwordExtractorConfiguration config, StopwordListRegistry registry) {
        super("stopword", "Stopword", Phase.stopword);
        this.config = config;
        this.registry = registry;
    }

    @Override
    protected void init() throws Exception {

    }

    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return Collections.emptyMap();
    }

    @Override
    public void doProcessing(ProcessingData processingData) {
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(processingData);
        if (at.isPresent()) {
            boolean caseSensitive = processingData.getConfiguration(PROP_CASE_SENSITIVE, config.isCaseSensitive());
            boolean usePos = processingData.getConfiguration(PROP_USE_POS, config.isUsePos());
            String lang = processingData.getLanguage();
            Set<String> stopwords = registry.getStopwords(lang == null ? null : Locale.forLanguageTag(lang), caseSensitive);
            if (stopwords != null) {
                process(at.get(), stopwords, usePos);
                log.trace("mark stopwords for {} (language: {})", processingData, lang);
            } else {
                log.trace("no stopword list available for language {} (processingData: {})", lang, processingData);
            }
        }

    }

    private void process(AnalyzedText at, Set<String> stopwords, boolean usePos) {
        Iterator<Token> tokens = at.getTokens();
        while (tokens.hasNext()) {
            Token token = tokens.next();
            if (stopwords.contains(token.getSpan())) { //this might be a stop word
                if (!usePos || !NlpUtils.isOfPos(token, NO_STOPWORD_LEX_CAT)) {
                    token.setAnnotation(NlpAnnotations.STOPWORD_ANNOTATION, Boolean.TRUE);
                }//else even that it matches the stop word list we do not mark this as stop word
            } //else not a stop word
        }
    }

}
