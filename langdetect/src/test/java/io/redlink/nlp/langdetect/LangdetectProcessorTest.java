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

package io.redlink.nlp.langdetect;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.content.StringContent;
import io.redlink.nlp.api.model.Value;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybozu.labs.langdetect.LangDetectException;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.json.AnalyzedTextParser;
import io.redlink.nlp.model.json.valuetype.ValueTypeParserRegistry;
import io.redlink.nlp.model.json.valuetype.impl.SectionTagSupport;

public class LangdetectProcessorTest {

    private final static Logger log = LoggerFactory.getLogger(LangdetectProcessorTest.class);
    
    private final static Charset UTF8 = Charset.forName("UTF-8");

    private static final Map<String, Collection<TestCase>> testFileByLang = new HashMap<>();
    
    private static AnalyzedTextParser atParser;
    
    private static LanguageIdentifier langId;
    
    private LangdetectProcessor langdetect;

    @BeforeClass
    public static void initClass() throws LangDetectException {
        //first init the test files
        String baseDir = System.getProperty("basedir");
        if(baseDir == null){
            baseDir = System.getProperty("user.dir");
        }
        File testFilesFolder = new File(baseDir,"src/test/resources/testfiles");
        log.info("testfile folder: {}", testFilesFolder.getAbsolutePath());
        Assume.assumeTrue(testFilesFolder.isDirectory());
        log.info("Init test files");
        for(File langDir : testFilesFolder.listFiles()){
            if(langDir.isDirectory()){
                String lang = langDir.getName();
                if("none".equals(lang)){
                    lang = null;
                }
                log.info(" > language: {}", lang);
                Map<String, MutablePair<File, File>> langFiles = new HashMap<>();
                Collection<TestCase> testFiles = new LinkedList<>();
                for(File testFile : langDir.listFiles()){
                    if(testFile.isFile() && !testFile.isHidden() &&
                            !testFile.getName().startsWith("ignore-")){
                        String name = FilenameUtils.getBaseName(testFile.getName());
                        String ext = FilenameUtils.getExtension(testFile.getName());
                        MutablePair<File, File> pair = langFiles.get(name);
                        if(pair == null){
                            pair = new MutablePair<>();
                            langFiles.put(name, pair);
                        }
                        if("cnt".equalsIgnoreCase(ext)){
                            pair.setLeft(testFile);
                        } else if("json".equalsIgnoreCase(ext)){
                            pair.setRight(testFile);
                        }
                    }
                }
                for(Entry<String,MutablePair<File, File>> entry : langFiles.entrySet()){
                    if(entry.getValue().left != null && entry.getValue().right != null){
                        log.info("   - {}", entry.getKey());
                        testFiles.add(new TestCase(entry.getValue().left,entry.getValue().right));
                    }
                }
                if(!testFiles.isEmpty()){
                    testFileByLang.put(lang, testFiles);
                }
            }
        }
        atParser = new AnalyzedTextParser(new ValueTypeParserRegistry(Arrays.asList(
                new SectionTagSupport()
                )));
        
        // load the language profiles
        langId = new LanguageIdentifier();
    }
    
    @Before
    public void init() {
        langdetect = new LangdetectProcessor(langId);
    }
    
    @After
    public void clear(){
        langdetect = null;
    }

    @AfterClass
    public static void clearClass(){
        //nothing to do
    }
    
    /**
     * This tests that no language is detected for the given list of test files
     */
    @Test
    public void testNone() throws IOException, ProcessingException {
        testLang(null);
    }
    
    @Test
    public void testDe() throws IOException, ProcessingException {
       testLang("de");
    }

    @Test
    public void testEn() throws IOException, ProcessingException {
       testLang("en");
    }

    @Test
    public void testEs() throws IOException, ProcessingException {
       testLang("es");
    }
    
    @Test
    public void testIt() throws IOException, ProcessingException {
        testLang("it");
    }
    
    @Test
    public void testJa() throws IOException, ProcessingException {
        testLang("ja");
    }

    /**
     * Processes test cases of the parsed languages.
     * @param lang
     */
    private void testLang(String lang) throws IOException, ProcessingException {
        testLang(lang, Collections.emptyMap());
    }
    /**
     * Processes test cases of the parsed languages.
     * @param lang
     */
    private void testLang(String lang, Map<String,Object> config) throws IOException, ProcessingException {
        //check if we do have test cases for those language
        Assume.assumeTrue(testFileByLang.containsKey(lang));
        Collection<TestCase> testCases = testFileByLang.get(lang);
        Assume.assumeNotNull(testCases);
        Assume.assumeFalse(testCases.isEmpty());
        log.info("> test {} documents for language {}",testCases.size(), lang);
        //now assert that the correct language is detected for those
        for(TestCase testCase : testCases) {
            log.info(" - read '{}'",FilenameUtils.getBaseName(testCase.getAtFile().getName()));
            String content = FileUtils.readFileToString(testCase.getCntFile(), UTF8);
            AnalyzedText at = atParser.parse(FileUtils.openInputStream(testCase.getAtFile()), UTF8, content);

            //process the test case first with
            ProcessingData pd = new ProcessingData(new StringContent(at.getText()), config);
            pd.addAnnotation(AnalyzedText.ANNOTATION, at);
            langdetect.process(pd);
            Value<String> langAnno = pd.getValue(Annotations.LANGUAGE);
            if(lang == null) {
                Assert.assertNull(langAnno);
            } else {
                Assert.assertNotNull("No language detected for '"
                        +at.getText()+"' (expected: '"+lang+"')",langAnno);
                Assert.assertEquals(lang, langAnno.value());
            }
        }

    }

}
