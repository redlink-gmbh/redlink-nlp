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

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.content.StringContent;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.AnalyzedText.AnalyzedTextBuilder;
import io.redlink.nlp.model.ner.NerTag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.redlink.nlp.regex.ner.ExpectedNer.assertNlpProcessingResults;

public class TestRegexNerProcessor {

    private static Logger log = LoggerFactory.getLogger(TestRegexNerProcessor.class);

    private static List<Pair<String[], List<ExpectedNer>>> CONTENTS = new ArrayList<>();

    private RegexNerProcessor regexNer;

    @BeforeClass
    public static void initClass() throws IOException {
        CONTENTS.add(new ImmutablePair<>(new String[]{
                "Ist der ICE 1526 von Münchner Hbf heute verspätet?"},
                Arrays.asList(
                        new ExpectedNer("train", "train", "ICE 1526"),
                        new ExpectedNer("station", NerTag.NAMED_ENTITY_LOCATION, "Hbf", "Hauptbahnhof"))));
        CONTENTS.add(new ImmutablePair<>(new String[]{
                "Brauche einen Zug von Darmstadt nach Ilmenau. Muss um 16:00 in Ilmenau sein."},
                Collections.emptyList())); //Test an empty result 
        CONTENTS.add(new ImmutablePair<>(new String[]{
                "Brauche für morgen Nachmittag einen Zug von Hamburg nach Berlin",
                "Ich würde Dir den ICE-1234 um 14:35 empfehlen. Brauchst Du ein Hotel in Berlin?",
                "Ja. Bitte eines in der nähe des Hbf. Wenn möglich um weniger als 150 Euro."},
                Arrays.asList(
                        new ExpectedNer("train", "train", "ICE-1234", "ICE 1234"),
                        new ExpectedNer("station", NerTag.NAMED_ENTITY_LOCATION, "Hbf", "Hauptbahnhof"))));
        CONTENTS.add(new ImmutablePair<>(new String[]{
                "Warte schon 10 Minuten auf the ICE1234. Keine Information am Bhf!",
                "der ICE 1234 ist im Moment 25 Minuten verspätet. Tut mir leid, dass am Bahnhof keine Informationen ausgerufen werden.",
                "Kannst Du mir auch noch sagen ob ich den ICE 2345 im Hamburger Hbf erreiche."},
                Arrays.asList(
                        new ExpectedNer("train", "train", "ICE1234", "ICE 1234"),
                        new ExpectedNer("station", NerTag.NAMED_ENTITY_LOCATION, "Bhf", "Bahnhof"),
                        new ExpectedNer("train", "train", "ICE 1234"),
                        new ExpectedNer("station", NerTag.NAMED_ENTITY_LOCATION, "Bahnhof"),
                        new ExpectedNer("train", "train", "ICE 2345"),
                        new ExpectedNer("station", NerTag.NAMED_ENTITY_LOCATION, "Hbf", "Hauptbahnhof"))));
    }


    @Before
    public void init() throws IOException {
        TrainDetector trainDetector = new TrainDetector();
        BahnhofDetector bahnhofDetector = new BahnhofDetector();
        regexNer = new RegexNerProcessor(Arrays.asList(trainDetector, bahnhofDetector));
    }

    @Test
    public void testExtraction() throws ProcessingException {
        for (int idx = 0; idx < CONTENTS.size(); idx++) {
            ProcessingData pd = initTestData(idx);
            log.debug(" - using {} (idx: {})", pd, idx);
            long start = System.currentTimeMillis();
            regexNer.process(pd);
            log.debug(" - processing time: {}", System.currentTimeMillis() - start);
            assertNlpProcessingResults(pd, CONTENTS.get(idx).getRight());
        }
    }

    private static final ProcessingData initTestData(int index) {
        return initTestData(index, new HashMap<>());
    }

    private static final ProcessingData initTestData(int index, Map<String, Object> config) {
        AnalyzedTextBuilder atb = AnalyzedText.build();
        for (String section : CONTENTS.get(index).getLeft()) {
            atb.appendSection(null, section, "\n");
        }
        AnalyzedText at = atb.create();
        //this text is German
        ProcessingData pd = new ProcessingData(new StringContent(at.getText()), config);
        pd.addAnnotation(Annotations.LANGUAGE, "de");
        pd.addAnnotation(AnalyzedText.ANNOTATION, at);
        return pd;
    }


}
