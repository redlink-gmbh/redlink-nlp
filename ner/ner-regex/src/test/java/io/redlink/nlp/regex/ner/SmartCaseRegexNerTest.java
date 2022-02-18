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

import static io.redlink.nlp.regex.ner.ExpectedNer.assertNlpProcessingResults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.annotation.Annotations;

import io.redlink.nlp.api.content.StringContent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.AnalyzedText.AnalyzedTextBuilder;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Span;
import io.redlink.nlp.model.Span.SpanTypeEnum;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.util.NlpUtils;

public class SmartCaseRegexNerTest {

    private static Logger log = LoggerFactory.getLogger(SmartCaseRegexNerTest.class);
    
    private static List<Pair<String[],List<ExpectedNer>>> CONTENTS = new ArrayList<>();

    private RegexNerProcessor regexNer;

    @BeforeClass
    public static void initClass() throws IOException {
        CONTENTS.add(new ImmutablePair<>(new String[]{
                "Das ist nicht oft der Fall das mit IST ein soziales Problem gelöst werden kann."},
                Arrays.asList(
                        new ExpectedNer("test", NerTag.NAMED_ENTITY_MISC,"IST", "Informations System Technologie"))));
        CONTENTS.add(new ImmutablePair<>(new String[]{
                "Mit einem DAS braucht man keinen Analog Digital Converter"},
                Arrays.asList(
                        new ExpectedNer("test", NerTag.NAMED_ENTITY_MISC,"DAS", "Digital Analog System"))));
        CONTENTS.add(new ImmutablePair<>(new String[]{
                "Um das Gerät auf den neuersten Stand zu bringen bitte zuerst die OFT drücken",
                "Diese ist direkt under der grünen Menü Taste."},
                Arrays.asList(
                        new ExpectedNer("test", NerTag.NAMED_ENTITY_MISC,"OFT", "Online Funktions Taste"))));
    }
    
   
    @Before
    public void init() throws IOException {
        SmartCaseTestDetector detector = new SmartCaseTestDetector();
        regexNer = new RegexNerProcessor(Arrays.asList(detector));
    }
    
    @Test
    public void testExtraction() throws ProcessingException {
        for(int idx = 0; idx < CONTENTS.size(); idx++){
            ProcessingData pd = initTestData(idx);
            log.debug(" - using {} (idx: {})", pd, idx);
            long start = System.currentTimeMillis();
            regexNer.process(pd);
            log.debug(" - processing time: {}",System.currentTimeMillis()-start);
            assertNlpProcessingResults(pd, CONTENTS.get(idx).getRight());
        }
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


}
