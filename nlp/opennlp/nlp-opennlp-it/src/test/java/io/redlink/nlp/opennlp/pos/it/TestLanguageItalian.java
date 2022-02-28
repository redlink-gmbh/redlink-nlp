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

package io.redlink.nlp.opennlp.pos.it;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.content.StringContent;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Span;
import io.redlink.nlp.model.Span.SpanTypeEnum;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.util.NlpUtils;
import io.redlink.nlp.opennlp.pos.OpenNlpLanguageModel;
import io.redlink.nlp.opennlp.pos.OpenNlpPosProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * Test for Spanish keyword extraction
 *
 * @author rupert.westenthaler@redlink.co
 */
public class TestLanguageItalian {

    private static final Logger log = LoggerFactory.getLogger(TestLanguageItalian.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static final List<String> FILES = unmodifiableList(asList(
            "docs/Formula_1_Gp_d’Austria:_vince_Rosberg.txt",
            "docs/Il_paese_che_riaffiora_dalle_acque.txt",
            "docs/Grecia_le_Borse_credono_all’accordo_L’Europa_vola_Atene_sale_del_7%.txt",
            "docs/Legna,_carbone_e_letame.txt",
            "docs/La_stampante_3D_arriva_sugli_scaffali_dell’ipermercato.txt"));

    private static List<String> CONTENTS = new ArrayList<>(FILES.size());


    private static LanguageItalian model;

    private OpenNlpPosProcessor processor;

    @BeforeClass
    public static void initClass() throws IOException {
        ClassLoader cl = TestLanguageItalian.class.getClassLoader();
        for (String file : FILES) {
            InputStream in = cl.getResourceAsStream(file);
            Assert.assertNotNull("Missing test resource '" + file + "'!", in);
            String content = IOUtils.toString(in, UTF8);
            CONTENTS.add(content);
        }
        model = new LanguageItalian();
    }


    private static final ProcessingData initTestData(int index) {
        return initTestData(index, new HashMap<>());
    }

    private static final ProcessingData initTestData(int index, Map<String, Object> config) {
        Assert.assertTrue(index >= 0 && index < FILES.size());
        AnalyzedText at = new AnalyzedText(CONTENTS.get(index));
        //we just put here any language (no)
        ProcessingData pd = new ProcessingData(new StringContent(at.getText()), config);
        pd.addAnnotation(Annotations.LANGUAGE, "it");
        pd.addAnnotation(AnalyzedText.ANNOTATION, at);
        return pd;
    }


    @Before
    public void init() {
        processor = new OpenNlpPosProcessor(Collections.<OpenNlpLanguageModel>singleton(model));
    }

    @Test
    public void testSingle() throws ProcessingException {
        int idx = Math.round((float) Math.random() * (FILES.size() - 1));
        log.debug(" - using file {} (idx: {})", FILES.get(idx), idx);
        long start = System.currentTimeMillis();
        ProcessingData pd = initTestData(idx);
        processor.process(pd);
        log.debug(" - processing time: {}", System.currentTimeMillis() - start);
        assertNlpProcessingResults(pd);
    }

    private void assertNlpProcessingResults(ProcessingData pd) {
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(pd);
        Assert.assertTrue(at.isPresent());
        Iterator<Span> spans = at.get().getEnclosed(EnumSet.allOf(SpanTypeEnum.class));
        boolean sentencePresent = false;
        boolean tokenPresent = false;
        int lastSentEnd = 0;
        int lastTokenEnd = 0;
        while (spans.hasNext()) {
            Span span = spans.next();
            switch (span.getType()) {
                case Sentence:
                    sentencePresent = true;
                    Assert.assertTrue(lastSentEnd <= span.getStart());  //none overlapping
                    lastSentEnd = span.getEnd();
                    break;
                case Token:
                    tokenPresent = true;
                    Assert.assertTrue(lastTokenEnd <= span.getStart()); //none overlapping
                    lastTokenEnd = span.getEnd();
                    List<Value<PosTag>> posAnnos = span.getValues(NlpAnnotations.POS_ANNOTATION);
                    Assert.assertFalse(posAnnos.isEmpty());
                    for (Value<PosTag> posAnno : posAnnos) {
                        Assert.assertTrue(posAnno.probability() != Value.UNKNOWN_PROBABILITY);
                        Assert.assertTrue(posAnno.probability() <= 1);
                        PosTag posTag = posAnno.value();
                        Assert.assertNotNull(posTag.getTag());
                        Assert.assertFalse("PosTag " + posTag + " is not mapped!", posTag.getCategories().isEmpty());
                    }
                default:
                    break;
            }
        }

        Assert.assertTrue(sentencePresent);
        Assert.assertTrue(tokenPresent);
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
            assertNlpProcessingResults(tasks.remove(0).get().getProcessingData());
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

    private class TestCaseProcessor implements Callable<TestCaseProcessor> {

        private ProcessingData pd;
        private int duration;

        TestCaseProcessor(int idx) {
            this.pd = initTestData(idx);
        }


        @Override
        public TestCaseProcessor call() throws Exception {
            long start = System.currentTimeMillis();
            processor.process(pd);
            duration = (int) (System.currentTimeMillis() - start);
            return this;
        }

        public ProcessingData getProcessingData() {
            return pd;
        }

        public int getDuration() {
            return duration;
        }
    }

}
