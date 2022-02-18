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

package io.redlink.nlp.regex.ner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.regex.ner.vocab.VocabularyDetector;
import io.redlink.nlp.regex.ner.vocab.VocabularyEntry;

/**
 * Simple detector with entries to check the {@link CaseSensitivity#smart} functionality
 * @author westei
 *
 */
public class SmartCaseTestDetector extends VocabularyDetector {

    public SmartCaseTestDetector() {
        super("Smart Case Detector", new NerTag("test", NerTag.NAMED_ENTITY_MISC), Locale.GERMAN, CaseSensitivity.smart);
        try {
            init(); //call postConstruct method so that we can use this for unit testing
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected Collection<VocabularyEntry> loadEntries() throws IOException {
        VocabularyEntry ist = new VocabularyEntry("Informations System Technologie");
        ist.addSynonym("IST");

        VocabularyEntry das = new VocabularyEntry("Digital Analog System");
        das.addSynonym("DAS");

        VocabularyEntry oft = new VocabularyEntry("Online Funktions Taste");
        oft.addSynonym("OFT");
        
        return Arrays.asList(ist, das, oft);
    }

}
