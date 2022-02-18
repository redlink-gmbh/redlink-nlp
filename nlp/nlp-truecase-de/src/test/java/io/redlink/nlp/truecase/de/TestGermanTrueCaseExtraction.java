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

package io.redlink.nlp.truecase.de;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.content.StringContent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.AnalyzedText.AnalyzedTextBuilder;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Span;
import io.redlink.nlp.model.Span.SpanTypeEnum;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.util.NlpUtils;

public class TestGermanTrueCaseExtraction {
    
    private static final Logger log = LoggerFactory.getLogger(TestGermanTrueCaseExtraction.class);

    private static List<Pair<String[],Set<String>>> CONTENTS = new ArrayList<>();

    private static List<Processor> REQUIRED_PREPERATORS = Collections.emptyList();

    private GermanTrueCaseExtractor processor;
    
    
    @BeforeClass
    public static void initClass() throws IOException {
        CONTENTS.add(new ImmutablePair<>(
                new String[]{
                        "Ist der ICE 1526 von münchen heute verspätet?"},
                new HashSet<>(Arrays.asList("München"))));
        CONTENTS.add(new ImmutablePair<>(
                new String[]{
                        "Brauche einen zug von darmstadt nach ilmenau. Muss spätestens um 16:00 ankommen."},
                new HashSet<>(Arrays.asList("Zug","Darmstadt","Ilmenau"))));
        CONTENTS.add(new ImmutablePair<>(
                new String[]{
                        "brauche am Freitag ein zimmer in salzburg-lehen!"},
                new HashSet<>(Arrays.asList("Brauche","Zimmer","Salzburg-Lehen"))));
        CONTENTS.add(new ImmutablePair<>(
                new String[]{
                        "kann mir wer sage wie lange der ICE-123 noch stehen wird. habe in 10 minuten einen anschluss in nürnberg!!"},
                new HashSet<>(Arrays.asList("Kann","Habe","Minuten","Anschluss","Nürnberg"))));
        CONTENTS.add(new ImmutablePair<>(
                new String[]{
                        "Ist der ICE 1526 von München heute verspätet?"},
                Collections.emptySet()));
    }
    
    private static final ProcessingData initTestData(int index) {
        return initTestData(index, new HashMap<>());
    }    
    
    private static final ProcessingData initTestData(int index, Map<String,Object> config) {
        AnalyzedTextBuilder atb = AnalyzedText.build();
        for(String section : CONTENTS.get(index).getLeft()){
            atb.appendSection(null, section, "\n");
        }
        AnalyzedText at = atb.create();
        //this text is German
        ProcessingData pd = new ProcessingData(new StringContent(at.getText()),config);
        pd.addAnnotation(Annotations.LANGUAGE, "de");
        pd.addAnnotation(AnalyzedText.ANNOTATION, at);
        return pd;
    }

    @Before
    public void init() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
        processor = new GermanTrueCaseExtractor();
        //call private postConstruct()
        Method init = GermanTrueCaseExtractor.class.getSuperclass().getDeclaredMethod("postConstruct");
        init.setAccessible(true);
        init.invoke(processor);
    }
    
    private static final void prepairTestCase(ProcessingData pd) throws ProcessingException {
        for(Processor p : REQUIRED_PREPERATORS){
            p.process(pd);
        }
    }

    
    @Test
    public void testSingle() throws ProcessingException {
        int idx = Math.round((float)Math.random()*(CONTENTS.size()-1));
        ProcessingData processingData = initTestData(idx);
        processTestCase(processingData);
        assertProcessingResults(processingData,CONTENTS.get(idx).getRight());
    }

    void processTestCase(ProcessingData processingData) throws ProcessingException {
        log.trace(" - preprocess {}", processingData);
        prepairTestCase(processingData);
        log.trace(" - start OpenNLP NER extraction");
        long start = System.currentTimeMillis();
        processor.process(processingData);
        log.trace(" - processing time: {}",System.currentTimeMillis()-start);
    }

    
    @Test
    public void testMultiple() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        int numDoc = 100;
        int numWarmup = Math.max(CONTENTS.size(), 20);
        log.info("> warnup ({} calls + assertion of results)", numWarmup);
        List<Future<TestCaseProcessor>> tasks = new LinkedList<>();
        for(int i = 0; i < numWarmup; i++){
            int idx = i%CONTENTS.size();
            tasks.add(executor.submit(new TestCaseProcessor(idx)));
        }
        while(!tasks.isEmpty()){ //wait for all the tasks to complete
            //during warmup we assert the NLP results
            TestCaseProcessor cp = tasks.remove(0).get();
            assertProcessingResults(cp.getProcessingData(), cp.getExpected()); 
        }
        log.info("   ... done");
        log.info("> processing {} documents ...", numDoc);
        long min = Integer.MAX_VALUE;
        long max = Integer.MIN_VALUE;
        long sum = 0;
        for(int i = 0; i < numDoc; i++){
            int idx = i%CONTENTS.size();
            tasks.add(executor.submit(new TestCaseProcessor(idx)));
        }
        int i = 0;
        while(!tasks.isEmpty()){ //wait for all the tasks to complete
            TestCaseProcessor completed = tasks.remove(0).get();
            i++;
            if(i%10 == 0){
                log.info(" ... {} documents processed",i);
            }
            int dur = completed.getDuration();
            if(dur > max){
                max = dur;
            }
            if(dur < min){
                min = dur;
            }
            sum = sum + dur;
        }
        log.info("Processing Times after {} documents",numDoc);
        log.info(" - average: {}ms",Precision.round(sum/(double)numDoc, 2));
        log.info(" - max: {}ms",max);
        log.info(" - min: {}ms",min);
        executor.shutdown();
    }
    
    private void assertProcessingResults(ProcessingData processingData, Set<String> expected) {
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(processingData);
        Assert.assertTrue(at.isPresent());
        
        //copy the expected so that we can remove
        expected = new HashSet<>(expected);
        
        Iterator<Span> spans = at.get().getEnclosed(EnumSet.allOf(SpanTypeEnum.class));
        boolean sentencePresent = false;
        boolean tokenPresent = false;
        int lastTokenEnd = 0;
        Map<Integer,String> trueCaseChanges = new HashMap<>();
        while(spans.hasNext()){
            Span span = spans.next();
            switch (span.getType()) {
                case Sentence:
                    sentencePresent = true;
                    break;
                case Token:
                    tokenPresent = true;
                    Assert.assertTrue(lastTokenEnd <= span.getStart()); //none overlapping
                    lastTokenEnd = span.getEnd();
                    List<Value<String>> trueCaseAnnos = span.getValues(NlpAnnotations.TRUE_CASE_ANNOTATION);
                    if(!trueCaseAnnos.isEmpty()){
                        Assert.assertEquals(1, trueCaseAnnos.size());
                        Value<String> trueCaseAnno = trueCaseAnnos.get(0);
                        Assert.assertNotNull(trueCaseAnno);
                        Assert.assertTrue((trueCaseAnno.probability() > 0 && trueCaseAnno.probability() <= 1) || trueCaseAnno.probability() == Value.UNKNOWN_PROBABILITY);
                        String trueCase = trueCaseAnno.value();
                        Assert.assertNotNull(trueCase);
                        Assert.assertEquals(span.getSpan().length(), trueCase.length());
                        Assert.assertNotEquals(span.getSpan(), trueCase);
                        Assert.assertTrue("Unexpected TrueCase "+trueCase+" for "+span, expected.remove(trueCase));
                        trueCaseChanges.put(span.getStart(), trueCase);
                    }
                default:
                    break;
            }
        }
        Assert.assertTrue("Missing Expected TrueCase "+expected, expected.isEmpty());
        Assert.assertFalse(sentencePresent);
        Assert.assertTrue(tokenPresent);

        String trueCaseText = NlpUtils.toTrueCase(at.get());
        Assert.assertEquals(at.get().getSpan().length(), trueCaseText.length());
        for(Entry<Integer, String> tcEntry : trueCaseChanges.entrySet()){
            Assert.assertEquals(tcEntry.getValue(), trueCaseText.substring(tcEntry.getKey(), tcEntry.getKey()+tcEntry.getValue().length()));
        }
    }
    
    
    private class TestCaseProcessor implements Callable<TestCaseProcessor> {

        private final ProcessingData processingData;
        private int duration;
        private Set<String> expected;

        TestCaseProcessor(int idx){
            this.processingData = initTestData(idx);
            this.expected = CONTENTS.get(idx).getRight();
        }
        

        public Set<String> getExpected() {
            return expected;
        }


        @Override
        public TestCaseProcessor call() throws Exception {
            long start = System.currentTimeMillis();
            processor.process(processingData);
            duration = (int)(System.currentTimeMillis() - start);
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
