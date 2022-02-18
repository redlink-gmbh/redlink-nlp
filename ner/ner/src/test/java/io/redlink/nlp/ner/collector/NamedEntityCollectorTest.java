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

package io.redlink.nlp.ner.collector;

import com.github.ferstl.junit.testgroups.TestGroup;
import com.github.ferstl.junit.testgroups.TestGroupRule;
import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.annotation.NamedEntity;
import io.redlink.nlp.api.content.StringContent;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.AnalyzedText.AnalyzedTextBuilder;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.util.NlpUtils;
import io.redlink.nlp.opennlp.OpenNlpNerProcessor;
import io.redlink.nlp.opennlp.de.NerGerman;
import io.redlink.nlp.regex.ner.RegexNerProcessor;
import io.redlink.nlp.regex.ner.TrainDetector;
import io.redlink.nlp.stanfordnlp.StanfordNlpProcessor;
import io.redlink.nlp.stanfordnlp.de.LanguageGerman;
import io.redlink.nlp.stanfordnlp.de.LanguageGermanConfiguration;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.redlink.nlp.model.ner.NerTag.NAMED_ENTITY_LOCATION;
import static io.redlink.nlp.model.ner.NerTag.NAMED_ENTITY_MISC;
import static io.redlink.nlp.model.ner.NerTag.NAMED_ENTITY_ORGANIZATION;

/**
 * Tests collecting {@link Token}s for {@link Annotations#NER_ANNOTATION}s
 * present in the {@link AnalyzedText}. In addition this also tests that the
 * {@link NegationHandler} also marks Named Entity Tokens as {@link Hint#negated}
 * if they are in a text section that is marked as {@link ProcessingData#NEGATION_ANNOTATION}.
 *
 * @author Rupert Westenthaler
 */
@TestGroup("high-memory") //this test needs more as 2g heap
public class NamedEntityCollectorTest {

    @ClassRule
    public static TestGroupRule rule = new TestGroupRule();

    private static final Logger log = LoggerFactory.getLogger(NamedEntityCollectorTest.class);

    /**
     * Test contents List of sections - List of expected results;
     */
    private static List<Triple<String[], List<Triple<String, String, Integer>>, List<Triple<String, String, String>>>> CONTENTS = new ArrayList<>();

    private static List<Processor> REQUIRED_PREPERATORS;

    private NamedEntityCollector nerCollector;


    @BeforeClass
    public static void initClass() throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        CONTENTS.add(new ImmutableTriple<>(
                new String[]{"Ist der ICE 1526 von München heute verspätet?"},
                Arrays.asList(
                        new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "München", 1),
                        new ImmutableTriple<>("train", "ICE 1526", 1)),
                Arrays.asList(
                        new ImmutableTriple<>("ICE 1526", "train", "train"),
                        new ImmutableTriple<>("München", NAMED_ENTITY_LOCATION, null))));
        CONTENTS.add(new ImmutableTriple<>(
                new String[]{"Brauche einen Zug von Darmstadt nach Ilmenau. Muss um 16:00 in Ilmenau sein."},
                Arrays.asList(
                        new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "Darmstadt", 1),
                        new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "Ilmenau", 2)),
                Arrays.asList(
                        new ImmutableTriple<>("Darmstadt", NAMED_ENTITY_LOCATION, null),
                        new ImmutableTriple<>("Ilmenau", NAMED_ENTITY_LOCATION, null),
                        new ImmutableTriple<>("Ilmenau", NAMED_ENTITY_LOCATION, null))));
        CONTENTS.add(new ImmutableTriple<>(
                new String[]{"Brauche einen Zug nach Ilmenau. Sollte vor 16:00 ankommen!",
                        "Von wo willst Du wegfahren?",
                        "Darmstadt."},
                Arrays.asList(
                        new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "Ilmenau", 1),
                        new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "Darmstadt", 1)),
                Arrays.asList(
                        new ImmutableTriple<>("Ilmenau", NAMED_ENTITY_LOCATION, null),
                        new ImmutableTriple<>("Darmstadt", NAMED_ENTITY_LOCATION, null))));
        CONTENTS.add(new ImmutableTriple<>(
                new String[]{"Brauche für morgen Nachmittag einen Zug von Hamburg nach Berlin",
                        "Ich würde Dir den ICE 1234 um 14:35 empfehlen. Brauchst Du ein Hotel in Berlin?",
                        "Ja. Bitte eines in der nähe vom Messezentrum. Wenn möglich um weniger als 150 Euro."},
                Arrays.asList(
                        new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "Hamburg", 1),
                        new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "Berlin", 2),
                        //new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "Messezentrum", 1),
                        new ImmutableTriple<>(NAMED_ENTITY_MISC, "Euro", 1),
                        new ImmutableTriple<>(NAMED_ENTITY_ORGANIZATION, "Euro", 1),
                        new ImmutableTriple<>("train", "ICE 1234", 1)),
                Arrays.asList(
                        new ImmutableTriple<>("Hamburg", NAMED_ENTITY_LOCATION, null),
                        new ImmutableTriple<>("Berlin", NAMED_ENTITY_LOCATION, null),
                        new ImmutableTriple<>("ICE 1234", "train", "train"),
                        new ImmutableTriple<>("Berlin", NAMED_ENTITY_LOCATION, null),
                        new ImmutableTriple<>("Euro", NAMED_ENTITY_MISC, null),
                        new ImmutableTriple<>("Euro", NAMED_ENTITY_ORGANIZATION, null))));
        CONTENTS.add(new ImmutableTriple<>(
                new String[]{"Warte schon 10 Minuten auf the ICE 1234. Keine Information am Bahnhof!",
                        "Der ICE 1234 ist im Moment 25 Minuten verspätet. Tut mir leid, dass am Bahnhof keine Informationen ausgerufen werden.",
                        "Kannst Du mir auch noch sagen ob ich den ICE 2345 in Hamburg erreiche."},
                Arrays.asList(
                        new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "Hamburg", 1),
                        new ImmutableTriple<>("train", "ICE 1234", 2),
                        new ImmutableTriple<>("train", "ICE 2345", 1)),
                Arrays.asList(
                        new ImmutableTriple<>("ICE 1234", "train", "train"),
                        new ImmutableTriple<>("ICE 1234", "train", "train"),
                        new ImmutableTriple<>("ICE 2345", "train", "train"),
                        new ImmutableTriple<>("Hamburg", NAMED_ENTITY_LOCATION, null))));
        CONTENTS.add(new ImmutableTriple<>(
                new String[]{"Brauche einen Zug von München nach Hamburg, aber bitte nicht über Nürnberg."},
                Arrays.asList(
                        new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "München", 1),
                        new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "Hamburg", 1),
                        new ImmutableTriple<>(NAMED_ENTITY_LOCATION, "Nürnberg", 1)),
                Arrays.asList(
                        new ImmutableTriple<>("München", NAMED_ENTITY_LOCATION, null),
                        new ImmutableTriple<>("Hamburg", NAMED_ENTITY_LOCATION, null),
                        new ImmutableTriple<>("Nürnberg", NAMED_ENTITY_LOCATION, null))));

        //we need some different NER processors for testing the NER Collector
        StanfordNlpProcessor stanfordnlp = new StanfordNlpProcessor(Arrays.asList(new LanguageGerman(new LanguageGermanConfiguration())));
        OpenNlpNerProcessor opennlpNer = new OpenNlpNerProcessor(Arrays.asList(new NerGerman()));
        RegexNerProcessor regexNer = new RegexNerProcessor(Arrays.asList(new TrainDetector()));
        //TODO: Negation support test
        //NegationDetector negation = new NegationDetector(Collections.singleton(new GermanNegationRule()));

        REQUIRED_PREPERATORS = Arrays.asList(stanfordnlp, opennlpNer, regexNer);//, negation);

    }

    private static final ProcessingData initTestData(int index) {
        return initTestData(index, new HashMap<>());
    }

    private static final ProcessingData initTestData(int index, Map<String, Object> config) {
        String[] content = CONTENTS.get(index).getLeft();
        AnalyzedTextBuilder atb = AnalyzedText.build();
        for (String section : content) {
            atb.appendSection(null, section, "\n");
        }
        AnalyzedText at = atb.create();
        ProcessingData pd = new ProcessingData(new StringContent(at.getText()), config);
        pd.addAnnotation(at);
        pd.addAnnotation(Annotations.LANGUAGE, "de"); //we have no language detection in the pipeline
        return pd;
    }


    private static final void prepairDocument(ProcessingData pd) throws ProcessingException {
        for (Processor processor : REQUIRED_PREPERATORS) {
            processor.process(pd);
        }
    }

    @Before
    public void init() {
        nerCollector = new NamedEntityCollector();
    }

    @Test
    public void testSingle() throws ProcessingException {
        int idx = Math.round((float) Math.random() * (CONTENTS.size() - 1));
        //idx=5;
        ProcessingData pd = initTestData(idx);
        processDocument(pd);
        assertNerProcessingResults(pd, CONTENTS.get(idx).getMiddle(), CONTENTS.get(idx).getRight());
    }

    void processDocument(ProcessingData processingData) throws ProcessingException {
        log.trace(" - preprocess Document {}", processingData);
        prepairDocument(processingData);
        log.trace(" - start processing");
        long start = System.currentTimeMillis();
        nerCollector.process(processingData);
        log.trace(" - processing time: {}", System.currentTimeMillis() - start);
    }


    @Test
    public void testMultiple() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        int numDoc = 100;
        int numWarmup = Math.max(CONTENTS.size(), 20);
        log.info("> warnup ({} calls + assertion of results)", numWarmup);
        List<Future<ConversationProcessor>> tasks = new LinkedList<>();
        for (int i = 0; i < numWarmup; i++) {
            int idx = i % CONTENTS.size();
            tasks.add(executor.submit(new ConversationProcessor(idx)));
        }
        while (!tasks.isEmpty()) { //wait for all the tasks to complete
            //during warmup we assert the NLP results
            ConversationProcessor cp = tasks.remove(0).get();
            assertNerProcessingResults(cp.getProcessingData(), CONTENTS.get(cp.getIdx()).getMiddle(), CONTENTS.get(cp.getIdx()).getRight());
        }
        log.info("   ... done");
        log.info("> processing {} documents ...", numDoc);
        long min = Integer.MAX_VALUE;
        long max = Integer.MIN_VALUE;
        long sum = 0;
        for (int i = 0; i < numDoc; i++) {
            int idx = i % CONTENTS.size();
            tasks.add(executor.submit(new ConversationProcessor(idx)));
        }
        int i = 0;
        while (!tasks.isEmpty()) { //wait for all the tasks to complete
            ConversationProcessor completed = tasks.remove(0).get();
            i++;
            if (i % 10 == 0) {
                log.info(" ... {} documents processed", i);
            }
            int dur = completed.getDuration();
            if (dur > max) {
                max = dur;
            }
            if (dur < min) {
                min = dur;
            }
            sum = sum + dur;
        }
        log.info("Processing Times after {} documents", numDoc);
        log.info(" - average: {}ms", Precision.round(sum / (double) numDoc, 2));
        log.info(" - max: {}ms", max);
        log.info(" - min: {}ms", min);
        executor.shutdown();
    }

    private void assertNerProcessingResults(ProcessingData processingData, List<Triple<String, String, Integer>> expectedNe,
                                            List<Triple<String, String, String>> expectedTags) {
        //(1) assert NamedEntities
        expectedNe = new LinkedList<>(expectedNe); //copy so we can remove
        List<Value<NamedEntity>> namedEntities = processingData.getValues(Annotations.NAMED_ENTITY);
        //Assert.assertEquals(expected.size(), namedEntities.size());
        double lastProb = 1d;
        for (Value<NamedEntity> neAnno : namedEntities) {
            log.debug("NamedEntity: {}", neAnno);
            Assert.assertTrue(lastProb >= neAnno.probability());
            lastProb = neAnno.probability();
            NamedEntity ne = neAnno.value();
            Assert.assertTrue(ne.getCount() >= 1);
            Assert.assertFalse(StringUtils.isBlank(ne.getName()));
            Triple<String, String, Integer> neTriple = new ImmutableTriple<>(ne.getType(), ne.getName(), ne.getCount());
            Assert.assertTrue("Unexpected Named Entity: " + ne, expectedNe.remove(neTriple));
        }
        Assert.assertTrue("Missing expected Named Entities: " + expectedNe, expectedNe.isEmpty());
        //Assert NerTag
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(processingData);
        Assert.assertTrue(at.isPresent());
        int idx = 0;
        for (Iterator<Chunk> it = at.get().getChunks(); it.hasNext(); ) {
            Chunk c = it.next();
            List<NerTag> tags = c.getAnnotations(NlpAnnotations.NER_ANNOTATION);
            for (NerTag tag : tags) {
                log.debug("assert {}: {} - {}", c, tag, c.getSpan());
                Assert.assertTrue(expectedTags.size() > idx);
                Triple<String, String, String> expTag = expectedTags.get(idx);
                Assert.assertEquals(expTag.getLeft(), c.getSpan());
                Assert.assertEquals(expTag.getMiddle(), tag.getType());
                if (expTag.getRight() != null) {
                    Assert.assertEquals(expTag.getRight(), tag.getTag());
                }
                idx++;
            }
        }
        Assert.assertEquals(expectedTags.size(), idx);
    }


    private class ConversationProcessor implements Callable<ConversationProcessor> {

        private final int idx;
        private final ProcessingData processingData;
        private int duration;

        ConversationProcessor(int idx) {
            this.idx = idx;
            this.processingData = initTestData(idx);
        }


        @Override
        public ConversationProcessor call() throws Exception {
            long start = System.currentTimeMillis();
            processDocument(processingData);
            duration = (int) (System.currentTimeMillis() - start);
            return this;
        }

        public int getIdx() {
            return idx;
        }

        public ProcessingData getProcessingData() {
            return processingData;
        }

        public int getDuration() {
            return duration;
        }
    }

}
