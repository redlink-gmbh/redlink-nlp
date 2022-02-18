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
package io.redlink.nlp.time.duckling;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.redlink.nlp.api.content.StringContent;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.AnalyzedText.AnalyzedTextBuilder;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Span;
import io.redlink.nlp.model.json.AnalyzedTextParser;
import io.redlink.nlp.model.json.AnalyzedTextSerializer;
import io.redlink.nlp.model.temporal.DateTimeValue;
import io.redlink.nlp.model.temporal.Temporal;
import io.redlink.nlp.model.temporal.Temporal.Grain;
import io.redlink.nlp.model.util.NlpUtils;

/**
 */
public class DucklingTimeProcessorTest {

    private static final Logger log = LoggerFactory.getLogger(DucklingTimeProcessorTest.class);

    
    private static DucklingTimeProcessor processor;

    private final Calendar refereceCal = new GregorianCalendar(2016,Calendar.APRIL,1,8,0);//Calendar.getInstance();
    private final Date referenceDate = refereceCal.getTime();
    private final Instant referenceInstant = referenceDate.toInstant();
    
    private static boolean defaultLatent;

    @Rule
    public TestWatcher logTestMethod = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            log.debug("Starting {}", description.getMethodName());
        }

        @Override
        protected void finished(Description description) {
            log.debug("Finished {}", description.getMethodName());
        }
    };

    @BeforeClass
    public static void initClass() throws IOException {
        processor = new DucklingTimeProcessor();
    }

    public void testConversation(){
        //TODO: conversion parsing (not reseting the context for every section)
        //is not yet implemented
        String simpleQuestion = "Ist der ICE 1526 von München heute verspätet?";
        String twoSentenceQuestion = "Brauche einen Zug von Darmstadt nach Ilmenau. Muss um 16:00 in Ilmenau sein.";
        String[] movingContextInConversation = new String[]{
                "Brauche für morgen Nachmittag einen Zug von Hamburg nach Berlin",
                "Ich würde Dir den ICE 1234 um 14:35 empfehlen. Brauchst Du ein Hotel in Berlin?",
                "Ja. Bitte eines in der nähe des Hbf. Werde erst nach 23:00 einchecken."};
        String[] durations = new String[]{
                "Warte schon 10 Minuten auf the ICE 1234. Keine Information am Bahnhof!",
                "Der ICE 1234 ist im Moment 25 Minuten verspätet."};
    }

    
    @AfterClass
    public static void tearDownClass() throws Exception {
        processor = null;
    }

    @Test
    public void testSimpleDateDe() throws Exception {
        ProcessingData pd = initTestData("de", referenceDate, "Morgen um 9 Uhr");
        processor.process(pd);
        Date expected = DateUtils.addDays(referenceDate, 1);
        expected = DateUtils.setHours(expected, 9);
        assertDateTime(pd, Arrays.asList(new DateTimeValue(new Temporal(expected, Grain.hour))));
    }

    @Test
    public void testSimpleDateEn() throws Exception {
        ProcessingData pd = initTestData("en", referenceDate, "Tomorrow at 9am");
        processor.process(pd);
        Date expected = DateUtils.addDays(referenceDate, 1);
        expected = DateUtils.setHours(expected, 9);
        assertDateTime(pd, Arrays.asList(new DateTimeValue(new Temporal(expected, Grain.hour))));
    }

    @Test
    public void testMultipleContexts() throws Exception {
        ProcessingData pd;
        //define vars so that I can easily comment tests
        pd = initTestData("de", referenceDate, "Hallo, ich will am 27. Mai, gegen 18 "
                + "Uhr mit dem ICE von Köln nach Berlin fahren und am 29. Abends zurück nach Bonn");

        processor.process(pd);
        
        Date expected1 = DateUtils.setMonths(referenceDate, Calendar.MAY);
        expected1 = DateUtils.setDays(expected1, 27);
        expected1 = DateUtils.setHours(expected1, 18);
        expected1 = DateUtils.setMinutes(expected1, 0);
        
        Date expected2Start = DateUtils.setDays(expected1, 29);
        expected2Start = DateUtils.setHours(expected2Start, 17);
        Date expected2End = DateUtils.setHours(expected2Start, 22);
        
        assertDateTime(pd, Arrays.asList(
                new DateTimeValue(new Temporal(expected1,Grain.hour)),
                new DateTimeValue(new Temporal(expected2Start, Grain.hour), new Temporal(expected2End,Grain.hour))));
        
        pd = initTestData("de", referenceDate, 
                "Hallo, ich brauche übermorgen ein Hotelzimmer in Hamburg ab 10 Uhr für zwei "
                + "Nächte. Bitte in der Nähe des Hbf in Hamburg.");
        
        processor.process(pd);

        Date expected1Start = DateUtils.addDays(referenceDate, 2);
        expected1Start = DateUtils.setHours(expected1Start, 10);
        Date expected1End = DateUtils.addDays(expected1Start, 2);
        
        assertDateTime(pd, Arrays.asList(
                new DateTimeValue(new Temporal(expected1Start,Grain.second), new Temporal(expected1End,Grain.second))));
        
        pd = initTestData("de", referenceDate, 
                "Hallo, am Samstag 28.5. Komme ich um 12h im HBF "
                + "Magdeburg an. Was könnte ich bis 16h Unternehmen");
        
        processor.process(pd);

        expected1Start = DateUtils.setMonths(referenceDate, Calendar.MAY);
        expected1Start = DateUtils.setDays(expected1Start, 28);
        expected1Start = DateUtils.setHours(expected1Start, 12);
        expected1End = DateUtils.setHours(expected1Start, 16);
        
        assertDateTime(pd, Arrays.asList(
                new DateTimeValue(new Temporal(expected1Start,Grain.hour), new Temporal(expected1End,Grain.hour))));
    }

    @Test
    public void testInterval() throws Exception {
        ProcessingData pd = initTestData("de", referenceDate, "Such am Wochenende ein Hotel in Berlin");

        processor.process(pd);
        
        Calendar cal = DateUtils.toCalendar(referenceDate);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        cal.set(Calendar.HOUR_OF_DAY, 18);
        Date expectedStart = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 2); //Fri to Sun
        cal.set(Calendar.HOUR_OF_DAY, 22);
        Date expectedEnd = cal.getTime();

        assertDateTime(pd, Arrays.asList(
                new DateTimeValue(new Temporal(expectedStart, Grain.hour), new Temporal(expectedEnd,Grain.hour))));
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(pd);
        Assert.assertTrue(at.isPresent());
        AnalyzedTextSerializer.getDefaultInstance().serialize(at.get(), out, Charset.forName("UTF-8"));
        String json = new String(out.toByteArray(),"UTF-8");
        log.info("JSON: \n{}", json);
        AnalyzedText parsed = AnalyzedTextParser.getDefaultInstance().parse(json ,at.get().getSpan());
        assertDateTime(parsed, Arrays.asList(
                new DateTimeValue(new Temporal(expectedStart, Grain.hour), new Temporal(expectedEnd,Grain.hour))));
    }
    /**
     * Same test as {@link #testInterval()} but it serializes and parses the {@link DateTimeValue}
     * annotations before asserting their values
     * @throws Exception
     */
    @Test
    public void testIntervalSerializationAndParsing() throws Exception {
        ProcessingData pd = initTestData("de", referenceDate, "Such am Wochenende ein Hotel in Berlin");

        processor.process(pd);
        
        Calendar cal = DateUtils.toCalendar(referenceDate);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        cal.set(Calendar.HOUR_OF_DAY, 18);
        Date expectedStart = cal.getTime();
        cal = DateUtils.toCalendar(referenceDate);
        cal.add(Calendar.DAY_OF_YEAR, 2); //Fri to Sun
        cal.set(Calendar.HOUR_OF_DAY, 22);
        Date expectedEnd = cal.getTime();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(pd);
        Assert.assertTrue(at.isPresent());
        AnalyzedTextSerializer.getDefaultInstance().serialize(at.get(), out, Charset.forName("UTF-8"));
        String json = new String(out.toByteArray(),"UTF-8");
        log.trace("JSON: \n{}", json);
        AnalyzedText parsed = AnalyzedTextParser.getDefaultInstance().parse(json ,at.get().getSpan());
        assertDateTime(parsed, Arrays.asList(
                new DateTimeValue(new Temporal(expectedStart, Grain.hour), new Temporal(expectedEnd,Grain.hour))));
    }
    
    @Test
    public void testOpenInterval() throws Exception {
        ProcessingData pd = initTestData("de", referenceDate, "spätestens um 11:00");

        processor.process(pd);

        Date expectedEnd = DateUtils.setHours(referenceDate, 11);
        
        assertDateTime(pd, Arrays.asList(
                new DateTimeValue(null, new Temporal(expectedEnd,Grain.minute))));
    }

    
    @Test
    public void testRB112() throws Exception {
        Calendar refCal = new GregorianCalendar(2016, 5, 5);
        Date referenceDate = refCal.getTime();
        ProcessingData pd = initTestData("de", referenceDate, 
                Collections.singletonMap(DucklingTimeProcessor.INCLUDE_LATENT, false),
                "Ich brauche ein Hotel vom 1.8-3.8.2016 in Hamburg.");
        
        processor.process(pd);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(pd);
        Assert.assertTrue(at.isPresent());
        AnalyzedTextSerializer.getDefaultInstance().serialize(at.get(), out, Charset.forName("UTF-8"));
        String json = new String(out.toByteArray(),"UTF-8");
        log.trace("JSON: \n{}", json);
        
        Calendar cal = DateUtils.toCalendar(referenceDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, Calendar.AUGUST);
        Date expectedStart = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 3); //end exclusive
        Date expectedEnd = cal.getTime();

        assertDateTime(pd, Arrays.asList(
                new DateTimeValue(new Temporal(expectedStart, Grain.day), new Temporal(expectedEnd,Grain.day))));

    }
    
    @Test
    public void testWeirdDate() throws Exception {
        ProcessingData pd = initTestData("de", referenceDate, 
                Collections.singletonMap(DucklingTimeProcessor.INCLUDE_LATENT, true),
                "Morgen um 9:00 im ICE 599");
        
        processor.process(pd);
        
        Date expected = DateUtils.addDays(referenceDate, 1);
        expected = DateUtils.setHours(expected, 9);
        Date trainNumber = new GregorianCalendar(599, Calendar.JANUARY, 1, 0, 0, 0).getTime();
        
        assertDateTime(pd, Arrays.asList(
                new DateTimeValue(new Temporal(expected,Grain.minute)),
                new DateTimeValue(new Temporal(trainNumber, Grain.year))));
        
        pd = initTestData("de", referenceDate, 
                Collections.singletonMap(DucklingTimeProcessor.INCLUDE_LATENT, false),
                "Morgen um 9:00 im ICE 599");
        
        processor.process(pd);

        assertDateTime(pd, Arrays.asList(
                new DateTimeValue(new Temporal(expected,Grain.minute))));

    }
    private static final ProcessingData initTestData(String lang, Date context, String...contents) {
        return initTestData(lang,context,Collections.emptyMap(),contents);
    }    
    private static final ProcessingData initTestData(String lang, Date context, Map<String,Object> config, String...contents) {
        Assert.assertTrue(contents.length > 0);
        AnalyzedText at;
        if(contents.length > 1){
            AnalyzedTextBuilder atb = AnalyzedText.build();
            for(String section : contents){
                Assert.assertNotNull(section);
                atb.appendSection(null, section, "\n");
            }
            at = atb.create();
        } else {
            Assert.assertNotNull(contents[0]);
            at = new AnalyzedText(contents[0]);
        }
        if(context != null){
            at.addAnnotation(NlpAnnotations.TEMPORAL_CONTEXT, context);
        }
        
        ProcessingData pd = new ProcessingData(new StringContent(at.getText()),config);
        pd.addAnnotation(AnalyzedText.ANNOTATION, at);
        if(lang != null){
            pd.addAnnotation(Annotations.LANGUAGE, lang);
        }
        return pd;
    }
    
    private void assertDateTime(ProcessingData pd, List<DateTimeValue> expected) {
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(pd);
        Assert.assertTrue(at.isPresent());
        assertDateTime(at.get(), expected);
    }
    private void assertDateTime(AnalyzedText at, List<DateTimeValue> expected) {
        int idx = 0;
        for(Iterator<Span> spans = at.iterator(); spans.hasNext();){
            Span span = spans.next();
            Value<DateTimeValue> dtAnno = span.getValue(NlpAnnotations.TEMPORAL_ANNOTATION);
            if(dtAnno != null){
                idx++;
                log.debug(" - {}. '{}' > {}", idx, span.getSpan(), dtAnno);
                if(expected.size() < idx){
                    Assert.fail("Expected " + expected.size() + "temporal annotations but found more");
                }
                Assert.assertEquals(expected.get(idx - 1), dtAnno.value());
            }
        }
        if(expected.size() != idx){
            Assert.fail("Expected " + expected.size() + "temporal annotations but found only " + idx);
        }
    }}