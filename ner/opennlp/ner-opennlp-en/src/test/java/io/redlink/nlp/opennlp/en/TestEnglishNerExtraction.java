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

package io.redlink.nlp.opennlp.en;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.content.StringContent;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.langdetect.LangdetectProcessor;
import io.redlink.nlp.langdetect.LanguageIdentifier;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.util.NlpUtils;
import io.redlink.nlp.opennlp.OpenNlpNerModel;
import io.redlink.nlp.opennlp.OpenNlpNerProcessor;
import io.redlink.nlp.opennlp.pos.OpenNlpPosProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class TestEnglishNerExtraction {

    private static final Logger log = LoggerFactory.getLogger(TestEnglishNerExtraction.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static final List<String> FILES = unmodifiableList(asList(
            "docs/bbc-israeli_PM_Netanyahu.txt",
            "docs/bbc-Man_Utd_3-2_Bayern_Munich.txt",
            "docs/bbc-Poles_president_crash.txt",
            "docs/Egypt-protests_wikinews-org.txt",
            "docs/obama-oil-drilling.txt"));

    private static List<String> CONTENTS = new ArrayList<>(FILES.size());

    private static List<Processor> REQUIRED_PRE_PROCESSORS;

    private static LanguageEnglish nlpModel;
    private static OpenNlpNerModel nerModel;

    private static OpenNlpNerProcessor nerProcessor;


    @BeforeClass
    public static void initClass() throws IOException {
        ClassLoader cl = TestEnglishNerExtraction.class.getClassLoader();
        //We need to store the contents in the ContentService. For that we need to generate ObjectIDs
        for (String file : FILES) {
            InputStream in = cl.getResourceAsStream(file);
            Assert.assertNotNull("Missing test resource '" + file + "'!", in);
            String content = IOUtils.toString(in, UTF8);
            CONTENTS.add(content);
        }
        nlpModel = new LanguageEnglish();
        nerModel = new NerEnglish();
        nerModel.activate(); //normally called by the OpenNlpNerPreprocessor's @PostConstruct

        REQUIRED_PRE_PROCESSORS = Arrays.asList(
                new LangdetectProcessor(new LanguageIdentifier()),
                new OpenNlpPosProcessor(Collections.singletonList(nlpModel)));

        nerProcessor = new OpenNlpNerProcessor(Collections.singletonList(nerModel));
    }

    private static final ProcessingData initTestData(int index) {
        return initTestData(index, new HashMap<>());
    }

    private static final ProcessingData initTestData(int index, Map<String, Object> config) {
        Assert.assertTrue(index >= 0 && index < FILES.size());
        AnalyzedText at = new AnalyzedText(CONTENTS.get(index));
        //we just put here any language (no)
        ProcessingData pd = new ProcessingData(new StringContent(at.getText()), config);
        pd.addAnnotation(Annotations.LANGUAGE, "en");
        pd.addAnnotation(AnalyzedText.ANNOTATION, at);
        return pd;
    }

    private static final void prepairTestCase(ProcessingData pd) throws ProcessingException {
        for (Processor preprocessor : REQUIRED_PRE_PROCESSORS) {
            preprocessor.process(pd);
        }
    }

    @Test
    public void testSingle() throws ProcessingException {
        int idx = Math.round((float) Math.random() * (FILES.size() - 1));
        ProcessingData pd = initTestData(idx);
        processTestCase(pd);
        assertNerProcessingResults(pd);
    }

    void processTestCase(ProcessingData pd) throws ProcessingException {
        log.trace(" - prepairing document {}", pd);
        prepairTestCase(pd);
        log.trace(" - start OpenNLP NER extraction");
        long start = System.currentTimeMillis();
        nerProcessor.process(pd);
        log.trace(" - processing time: {}", System.currentTimeMillis() - start);
    }


    @Test
    public void testMultiple() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        int numDoc = 100;
        int numWarmup = Math.max(FILES.size(), 20);
        log.info("> warnup ({} calls + assertion of results)", numWarmup);
        List<Future<TestCaseProcessor>> tasks = new LinkedList<>();
        for (int i = 0; i < numWarmup; i++) {
            int idx = i % FILES.size();
            tasks.add(executor.submit(new TestCaseProcessor(idx)));
        }
        while (!tasks.isEmpty()) { //wait for all the tasks to complete
            //during warmup we assert the NLP results
            assertNerProcessingResults(tasks.remove(0).get().getProcessingData());
        }
        log.info("   ... done");
        log.info("> processing {} documents ...", numDoc);
        long min = Integer.MAX_VALUE;
        long max = Integer.MIN_VALUE;
        long sum = 0;
        for (int i = 0; i < numDoc; i++) {
            int idx = i % FILES.size();
            tasks.add(executor.submit(new TestCaseProcessor(idx)));
        }
        int i = 0;
        while (!tasks.isEmpty()) { //wait for all the tasks to complete
            TestCaseProcessor completed = tasks.remove(0).get();
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

    private void assertNerProcessingResults(ProcessingData pd) {
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(pd);
        Assert.assertNotNull(at);
        Iterator<Chunk> chunks = at.get().getChunks();
        int numNerAnno = 0;
        while (chunks.hasNext()) {
            Chunk chunk = chunks.next();
            List<Value<NerTag>> nerAnnotations = chunk.getValues(NlpAnnotations.NER_ANNOTATION);
            Assert.assertFalse(nerAnnotations.isEmpty());
            for (Value<NerTag> nerAnno : nerAnnotations) {
                Assert.assertTrue(nerAnno.probability() > 0 && nerAnno.probability() <= 1);
                NerTag nerTag = nerAnno.value();
                Assert.assertNotNull(nerTag.getTag());
                Assert.assertNotNull(nerTag.getType());
                numNerAnno++;
            }
        }
        Assert.assertTrue(numNerAnno > 0);
    }

    private class TestCaseProcessor implements Callable<TestCaseProcessor> {

        private final ProcessingData processingData;
        private int duration;

        TestCaseProcessor(int idx) {
            this.processingData = initTestData(idx);
        }


        @Override
        public TestCaseProcessor call() throws Exception {
            long start = System.currentTimeMillis();
            processTestCase(processingData);
            duration = (int) (System.currentTimeMillis() - start);
            return this;
        }

        public ProcessingData getProcessingData() {
            return processingData;
        }

        public int getDuration() {
            return duration;
        }
    }

}
