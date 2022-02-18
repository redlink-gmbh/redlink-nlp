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

package io.redlink.nlp.stanfordnlp.de;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.content.StringContent;
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
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.Section;
import io.redlink.nlp.model.Span;
import io.redlink.nlp.model.Span.SpanTypeEnum;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.util.NlpUtils;
import io.redlink.nlp.stanfordnlp.StanfordNlpProcessor;

public class TestLanguageGerman {
    
    private static final Logger log = LoggerFactory.getLogger(TestLanguageGerman.class);

    private static List<String[]> CONTENTS = new ArrayList<>();

    private static List<Processor> REQUIRED_PROCESSORS = Collections.emptyList();

    private static LanguageGerman nlpModel;
    
    private StanfordNlpProcessor processor;
    
    
    @BeforeClass
    public static void initClass() throws IOException {
        CONTENTS.add(new String[]{
                "Hallo Reisebuddy! Ich brauche einen Zug um 18:30 nach Hamburg Hbf",
                "Von wo möchtest Du wegfahren?",
                "Leck mich am Arsch"});
        CONTENTS.add(new String[]{
                "Der ICE 12345 steht schon 15 Minuten auf der Strecke zwischen München und Nürnberg uns es gibt"
                + "noch keine Durchsage."});
        CONTENTS.add(new String[]{
                "Essen in Linz."});
        CONTENTS.add(new String[]{
                "Hallo Reisebuddy! Ich brauche einen Zug um 18:30 von Berlin nach Hamburg Hbf",
                "Hallo Reisebuddy! Ich brauche einen Zug um 18:30 von Berlin nach Hamburg Hauptbahnhof",
                "Hallo Reisebuddy! Wie komme ich vom Hamburg Hauptbahnhof zur Fillerstarasse 25a 13756 Berlin?"});
        CONTENTS.add(new String[]{
                "Kaufmännische Kenntnisse",
                "Kenntnisse betriebswirtschaftlicher Zusammenhänge von Finanzierung, Einkauf, Controlling und Buchhaltung",
                "Kenntnisse über Budgetplanung und Steuerung, sowie die Beurteilung kaufmännischer Kennzahlen",
                "Verständnis der Wertorientierung und des Kostenbeitrags zum  Unternehmensergebnis",
                "Ableiten und Anwenden sinnvoller Steuerungsgrößen",
                "Kenntnis zur Steuerung und Verknüpfung von Prozesskosten und Kapitalkosten"});
        CONTENTS.add(new String[]{
                "Abgeschlossene Berufsausbildung als Eisenbahner im Betriebsdienst",
                "EFF Klasse 3",
                "einschlägige Berufserfahrung als Lokrangierführer",
                "nachweispflichtige Qualifikationen: Bremsprobenberechtigter, Wagenprüfer G, Zugvorbereiter, Rangierbegleiter",
                "PVG-/CDD-Kenntnisse und Erfahrungen im Zugbildungsbereich der DB Schenker",
                "Betriebsdienst-, Rangierdienst- und Wechseldiensttauglichkeit gemäß KoRil 107 erforderlich",
                "Durchsetzungsvermögen und Eigeninitiative",
                "Hohe Belastbarkeit und ausgeprägte Teamfähigkeit",
                "Wirtschaftliches Denken und Handeln."});
        CONTENTS.add(new String[]{
                "Ist der ICE 1526 von München heute verspätet?"});
        CONTENTS.add(new String[]{
                "Brauche einen Zug von Darmstadt nach Ilmenau. Muss um 16:00 in Ilmenau sein."});
        CONTENTS.add(new String[]{
                "Brauche einen Zug nach Ilmenau. Muss vor 16:00 ankommen!",
                "Von wo willst Du wegfahren?",
                "Darmstadt."});
        CONTENTS.add(new String[]{
                "Brauche um 16:00 einen Zug nach Ilmenau",
                "Von wo willst Du wegfahren?",
                "Darmstadt",
                "Du kannst den ICE-123 um 16:15 von Darmstadt nehmen. Dann bist Du um 20:10 in Ilmenau."});
        CONTENTS.add(new String[]{
                "Brauche für morgen Nachmittag einen Zug von Hamburg nach Berlin",
                "Ich würde Dir den ICE 1234 um 14:35 empfehlen. Brauchst Du ein Hotel in Berlin?",
                "Ja. Bitte eines in der nähe des Hbf. Wenn möglich um weniger als 150 Euro."});
        CONTENTS.add(new String[]{
                "Warte schon 10 Minuten auf the ICE 1234. Keine Information am Bahnhof!",
                "der ICE 1234 ist im Moment 25 Minuten verspätet. Tut mir leid, dass am Bahnhof keine Informationen ausgerufen werden.",
                "Kannst Du mir auch noch sagen ob ich den ICE 2345 in Hamburg erreiche."});
        nlpModel = new LanguageGerman(new LanguageGermanConfiguration());

    }
    
    private static final ProcessingData initTestData(int index) {
        return initTestData(index, new HashMap<>());
    }    
    
    private static final ProcessingData initTestData(int index, Map<String,Object> config) {
        AnalyzedTextBuilder atb = AnalyzedText.build();
        for(String section : CONTENTS.get(index)){
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
    public void init() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
        processor = new StanfordNlpProcessor(Collections.singletonList(nlpModel));
        //call private postConstruct()
        Method init = StanfordNlpProcessor.class.getSuperclass().getDeclaredMethod("postConstruct");
        init.setAccessible(true);
        init.invoke(processor);
    }
    
    private static final void prepairTestCase(ProcessingData pd) throws ProcessingException {
        for(Processor qp : REQUIRED_PROCESSORS){
            qp.process(pd);
        }
    }

    
    @Test
    public void testSingle() throws ProcessingException {
        int idx = Math.round((float)Math.random()*(CONTENTS.size()-1));
        idx = 0;
        ProcessingData processingData = initTestData(idx);
        processTestCase(processingData);
        assertNerProcessingResults(processingData);
    }

    void processTestCase(ProcessingData processingData) throws ProcessingException {
        log.trace(" - preprocess {}", processingData);
        prepairTestCase(processingData);
        log.trace(" - start Stanford NER extraction");
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
            assertNerProcessingResults(tasks.remove(0).get().getProcessingData()); 
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
    
    private void assertNerProcessingResults(ProcessingData processingData) {
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(processingData);
        Assert.assertTrue(at.isPresent());
        
        //(1) assert the Sentences, Tokens and POS annotatons
        Iterator<Span> spans = at.get().getEnclosed(EnumSet.allOf(SpanTypeEnum.class));
        boolean sentencePresent = false;
        boolean tokenPresent = false;
        int lastSentEnd = 0;
        int lastTokenEnd = 0;
        while(spans.hasNext()){
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
                    for(Value<PosTag> posAnno : posAnnos){
                        Assert.assertTrue((posAnno.probability() > 0 && posAnno.probability() <= 1) || posAnno.probability() == Value.UNKNOWN_PROBABILITY);
                        PosTag posTag = posAnno.value();
                        Assert.assertNotNull(posTag.getTag());
                        Assert.assertFalse("PosTag "+ posTag + " is not mapped!", posTag.getCategories().isEmpty());
                    }
                default:
                    break;
            }
        }
        
        Assert.assertTrue(sentencePresent);
        Assert.assertTrue(tokenPresent);

        //(2) Assert the NER annotations
        
        Iterator<Section> sections = at.get().getSections();
        int numNerAnno = 0;
        while(sections.hasNext()){
            Section section = sections.next();
            Iterator<Chunk> chunks = section.getChunks();
            while(chunks.hasNext()){
                Chunk chunk = chunks.next();
                List<Value<NerTag>> nerAnnotations = chunk.getValues(NlpAnnotations.NER_ANNOTATION);
                //Assert.assertFalse(nerAnnotations.isEmpty());
                for(Value<NerTag> nerAnno : nerAnnotations){
                    Assert.assertTrue((nerAnno.probability() > 0 && nerAnno.probability() <= 1) || nerAnno.probability() == Value.UNKNOWN_PROBABILITY);
                    NerTag nerTag = nerAnno.value();
                    Assert.assertNotNull(nerTag.getTag());
                    Assert.assertNotNull(nerTag.getType());
                    numNerAnno++;
                    log.debug(" - [{},{}] {} (type:{})",chunk.getStart()-section.getStart(), chunk.getEnd()-section.getStart(), chunk.getSpan(), nerTag.getType());
                }
            }
            Assert.assertTrue(numNerAnno >= 0);
        }
    }
    
    
    private class TestCaseProcessor implements Callable<TestCaseProcessor> {

        private final ProcessingData processingData;
        private int duration;

        TestCaseProcessor(int idx){
            this.processingData = initTestData(idx);
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
