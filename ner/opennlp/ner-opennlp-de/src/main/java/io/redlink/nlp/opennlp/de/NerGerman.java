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

package io.redlink.nlp.opennlp.de;


import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.opennlp.NameFinderModel;
import io.redlink.nlp.opennlp.OpenNlpNerModel;
import org.springframework.stereotype.Component;

import static java.util.Locale.GERMAN;

/**
 * Implementation of English-specific tools for natural language processing.
 *
 * @author rupert.westenthaler@redlink.co
 */
@Component
public class NerGerman extends OpenNlpNerModel {

    public NerGerman() {
        super(GERMAN,
                new NameFinderModel("model/ner/opennlp/de-4-class-outer-clusters-germEval2014.bin",
                        "PER", NerTag.NAMED_ENTITY_PERSON,
                        "ORG", NerTag.NAMED_ENTITY_ORGANIZATION,
                        "LOC", NerTag.NAMED_ENTITY_LOCATION,
                        "MISC", NerTag.NAMED_ENTITY_MISC,
                        "OTH", NerTag.NAMED_ENTITY_MISC)); //Other
        setCaseSensitive(true); //this model is case sensitive
        //can be used to set a custom tokenizer
        //setCustomTokenizer("none");
    }

}
