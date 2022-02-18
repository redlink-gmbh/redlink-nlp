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

package io.redlink.nlp.opennlp.en;


import static java.util.Locale.ENGLISH;

import org.springframework.stereotype.Service;

import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.opennlp.NameFinderModel;
import io.redlink.nlp.opennlp.OpenNlpNerModel;


/**
 * Implementation of English-specific tools for natural language processing.
 * For now this uses the <code>CoNLL 2003 light clusters model: F1 90.27</code>
 * model of the 1.5 release of <a herf="https://github.com/ixa-ehu/ixa-pipe-nerc/">ixa-pipe-nerc</a>
 *
 * @author rupert.westenthaler@redlink.co
 */
@Service
public class NerEnglish extends OpenNlpNerModel {
    
    public NerEnglish() {
        super(ENGLISH,
                new NameFinderModel(//"en-brown-conll03.bin", //model without clusters
                        "en-light-clusters-conll03.bin", //model with clusters adds ~600MByte heap
                        "PER", NerTag.NAMED_ENTITY_PERSON, 
                        "ORG", NerTag.NAMED_ENTITY_ORGANIZATION,
                        "LOC", NerTag.NAMED_ENTITY_LOCATION, 
                        "MISC",NerTag.NAMED_ENTITY_MISC));
        //can be used to set a custom tokenizer
        //setCustomTokenizer("none");
    }

}
