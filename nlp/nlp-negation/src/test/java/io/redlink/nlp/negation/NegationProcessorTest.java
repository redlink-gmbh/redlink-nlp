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

package io.redlink.nlp.negation;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.content.StringContent;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.AnalyzedText.AnalyzedTextBuilder;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.util.NlpUtils;
import io.redlink.nlp.negation.de.GermanNegationRule;
import io.redlink.nlp.opennlp.de.LanguageGerman;
import io.redlink.nlp.opennlp.pos.OpenNlpPosProcessor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NegationProcessorTest {

    private static final Logger log = LoggerFactory.getLogger(NegationProcessorTest.class);

    private static List<Pair<String[], String[]>> CONTENTS = new ArrayList<>();

    private static List<Processor> REQUIRED_PRE_PROCESSORS;

    private NegationProcessor negation;


    @BeforeClass
    public static void initClass() throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        CONTENTS.add(new ImmutablePair<String[], String[]>(
                new String[]{
                        "Ich brauche einen Zug von München über Nürnberg nach Hamburg. Am besten über Nürnberg."},
                new String[]{}));
        CONTENTS.add(new ImmutablePair<String[], String[]>(
                new String[]{
                        "Hi Riesebuddy, könnt Ihr mir für heute Abend ein Restaurant in Berlin"
                                + "entpfhelen. Bitte keine Döner Bude oder Pizzaria."},
                new String[]{"keine Döner Bude oder Pizzaria"}));
        CONTENTS.add(new ImmutablePair<String[], String[]>(
                new String[]{
                        "Hi Riesebuddy, könnt Ihr mir für heute Abend ein Restaurant in Berlin"
                                + "entpfhelen. Eine Pizzaria bitte nicht."},
                new String[]{"Pizzaria bitte nicht"}));
        CONTENTS.add(new ImmutablePair<String[], String[]>(
                new String[]{
                        "Bruache ein Zimmer in München. Preis nicht über 150 Euro.",
                        "Muss es zentrumsnahe sein?",
                        "Nein, kann auch ein wenig Außerhalb sein. Sollte aber mit öffentlichen erreichbar sein."},
                new String[]{"nicht über 150 Euro"}));
        CONTENTS.add(new ImmutablePair<String[], String[]>(
                new String[]{
                        "Hat der ICE 1234 echt keine Verspätung? "},
                new String[]{"keine Verspätung"}));

        REQUIRED_PRE_PROCESSORS = Arrays.asList(
                new OpenNlpPosProcessor(Collections.singleton(new LanguageGerman())));

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


    @Before
    public void init() {
        negation = new NegationProcessor(Arrays.asList(new GermanNegationRule()));
    }

    private static final void prepairTestCase(ProcessingData pd) throws ProcessingException {
        for (Processor p : REQUIRED_PRE_PROCESSORS) {
            p.process(pd);
        }
    }


    @Test
    public void testSingle() throws ProcessingException {
        int idx = Math.round((float) Math.random() * (CONTENTS.size() - 1));
        //idx = 0;
        ProcessingData processingData = initTestData(idx);
        processTestCase(processingData);
        assertNegations(processingData, CONTENTS.get(idx).getRight());
    }

    void processTestCase(ProcessingData processingData) throws ProcessingException {
        log.trace(" - preprocess {}", processingData);
        prepairTestCase(processingData);
        log.trace(" - start OpenNLP NER extraction");
        long start = System.currentTimeMillis();
        negation.process(processingData);
        log.trace(" - processing time: {}", System.currentTimeMillis() - start);
    }


    //@Test
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
            assertNegations(cp.getProcessingData(), CONTENTS.get(cp.idx).getRight());
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

    private void assertNegations(ProcessingData processingData, String[] expected) {
        log.debug(" - expected Negations: {}", Arrays.toString(expected));
        Set<String> negSet = new HashSet<>(Arrays.asList(expected));
        AnalyzedText at = NlpUtils.getAnalyzedText(processingData).get();
        Iterator<Chunk> chunks = at.getChunks();
        while (chunks.hasNext()) {
            Chunk chunk = chunks.next();
            Value<Boolean> negation = chunk.getValue(NlpAnnotations.NEGATION_ANNOTATION);
            if (negation != null) {
                log.debug(" - negation {}: {}", chunk, chunk.getSpan());
                Assert.assertTrue("unexpected Negation:", negSet.remove(chunk.getSpan()));
            }
        }
        Assert.assertTrue("not detected negations: " + negSet, negSet.isEmpty());
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
            processTestCase(processingData);
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
