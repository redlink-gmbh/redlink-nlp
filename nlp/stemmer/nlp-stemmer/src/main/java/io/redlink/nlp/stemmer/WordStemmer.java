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

package io.redlink.nlp.stemmer;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.util.NlpUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.redlink.nlp.model.NlpAnnotations.STEM_ANNOTATION;

@Component
public class WordStemmer extends Processor {

    private StemmerRegistry registry;

    @Autowired
    public WordStemmer(StemmerRegistry registry) {
        super("stem", "Stemmer", Phase.stem);
        this.registry = registry;
    }

    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return Collections.emptyMap();
    }

    @Override
    protected void init() throws Exception {

    }

    @Override
    protected void doProcessing(ProcessingData processingData) {
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(processingData);
        if (at.isPresent()) {
            String lang = processingData.getLanguage();
            if (lang != null) {
                StemmerModel model = registry.getStemmerModel(lang);
                if (model != null) {
                    log.trace("process {} (lang: {}) with {}", processingData, lang, model.getClass().getSimpleName());
                    process(at.get(), model);
                } else {
                    log.trace("no stemmer model for language {} (data: {})", lang, processingData);
                }
            } else {
                log.trace("no language annotation present for {}", processingData);
            }
        }
    }

    private void process(AnalyzedText at, StemmerModel model) {
        Iterator<Token> tokens = at.getTokens();
        while (tokens.hasNext()) {
            Token token = tokens.next();
            String span = token.getSpan();
            String stem = model.stemToken(token.getSpan());
            if (!StringUtils.equals(span, stem)) {
                token.setAnnotation(STEM_ANNOTATION, stem);
            }
        }
    }

}
