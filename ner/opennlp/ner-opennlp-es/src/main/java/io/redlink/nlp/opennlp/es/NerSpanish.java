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

package io.redlink.nlp.opennlp.es;


import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.opennlp.NameFinderModel;
import io.redlink.nlp.opennlp.OpenNlpNerModel;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Implementation of Spanish-specific tools for natural language processing.
 * For now this uses the <code>CoNLL 2002 clusters + dict: F1 84.30</code>
 * model of the 1.5 release of <a herf="https://github.com/ixa-ehu/ixa-pipe-nerc/">ixa-pipe-nerc</a>
 *
 * @author rupert.westenthaler@redlink.co
 */
@Service
public class NerSpanish extends OpenNlpNerModel {

    public NerSpanish() {
        super(Locale.forLanguageTag("es"),
                new NameFinderModel("es-clusters-conll02.bin",
                        "PER", NerTag.NAMED_ENTITY_PERSON,
                        "ORG", NerTag.NAMED_ENTITY_ORGANIZATION,
                        "LOC", NerTag.NAMED_ENTITY_LOCATION,
                        "MISC", NerTag.NAMED_ENTITY_MISC));
        //can be used to set a custom tokenizer
        //setCustomTokenizer("none");
    }

}
