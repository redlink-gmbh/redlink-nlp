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

package io.redlink.nlp.regex.ner;

import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.regex.ner.vocab.VocabularyDetector;
import io.redlink.nlp.regex.ner.vocab.VocabularyEntry;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public class BahnhofDetector extends VocabularyDetector {

    public BahnhofDetector() {
        super("Bahnhof Detector", new NerTag("station", NerTag.NAMED_ENTITY_LOCATION), Locale.GERMAN, CaseSensitivity.smart);
        try {
            init(); //call postConstruct method so that we can use this for unit testing
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected Collection<VocabularyEntry> loadEntries() throws IOException {
        VocabularyEntry bahnhof = new VocabularyEntry("Bahnhof");
        bahnhof.addSynonym("Bhf.");
        bahnhof.addSynonym("Bhf");
        bahnhof.addSynonym("Bf.");
        bahnhof.addSynonym("Bf");

        VocabularyEntry hbf = new VocabularyEntry("Hauptbahnhof");
        hbf.addSynonym("hbf.");
        hbf.addSynonym("hbf");

        VocabularyEntry ostBf = new VocabularyEntry("Ostbahnhof");
        ostBf.addSynonym("Ostbf.");
        ostBf.addSynonym("Ostbf");
        ostBf.addSynonym("Ostbhf.");
        ostBf.addSynonym("Ostbhf");

        VocabularyEntry westBf = new VocabularyEntry("Westbahnhof");
        westBf.addSynonym("Westbf.");
        westBf.addSynonym("Westbf");
        westBf.addSynonym("Westbhf.");
        westBf.addSynonym("Westbhf");

        VocabularyEntry suedBf = new VocabularyEntry("Südbahnhof");
        suedBf.addSynonym("Südbf.");
        suedBf.addSynonym("Südbf");
        suedBf.addSynonym("Südbhf.");
        suedBf.addSynonym("Südbhf");

        VocabularyEntry nordBf = new VocabularyEntry("Nordbahnhof");
        nordBf.addSynonym("Nordbf.");
        nordBf.addSynonym("Nordbf");
        nordBf.addSynonym("Nordbhf.");
        nordBf.addSynonym("Nordbhf");

        return Arrays.asList(bahnhof, hbf, ostBf, westBf, suedBf, nordBf);
    }

}
