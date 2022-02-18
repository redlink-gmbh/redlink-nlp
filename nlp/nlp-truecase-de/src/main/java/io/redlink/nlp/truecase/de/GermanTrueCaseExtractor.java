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

import static edu.stanford.nlp.pipeline.Annotator.STANFORD_POS;
import static edu.stanford.nlp.pipeline.Annotator.STANFORD_SSPLIT;
import static edu.stanford.nlp.pipeline.Annotator.STANFORD_TOKENIZE;
import static io.redlink.nlp.stanfordnlp.annotators.RedlinkAnnotator.REDLINK_AT_SECTION;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.AnnotatorImplementations;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Lazy;
import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.util.NlpUtils;
import io.redlink.nlp.stanfordnlp.annotators.AnalyzedTextSectionAnnotator;

/**
 * The Named Entity {@link Preprocessor} used for extracting named entities
 * from processed documents.
 * 
 * @author rupert.westenthaler@redlink.co
 */
@Component
@ConditionalOnClass(AnnotationPipeline.class)
public class GermanTrueCaseExtractor extends Processor {

    private static final Logger LOG = LoggerFactory.getLogger(GermanTrueCaseExtractor.class);
    
    private AnnotatorPool pool;
    private AnnotationPipeline pipeline;
    private final Properties props;

    public GermanTrueCaseExtractor() {
        super("truecase.de","German True Case",Phase.pos,-10); //this should run before POS tagging
        props = new Properties();
        props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/german/german-fast-caseless.tagger");
        props.put("tokenize.language", "de");
    }

    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return Collections.emptyMap();
    }
    
    @Override
    protected void init(){
        initAnnotatorPool(props);
        pipeline = new AnnotationPipeline(Arrays.asList(
                pool.get("tokenize"),
                pool.get("atSection"),
                pool.get("ssplit"),
                pool.get("pos")));
    }
 

    @PreDestroy
    protected void destroyNerModels(){
        pool = null;
        pipeline = null;
    }

    @Override
    protected void doProcessing(ProcessingData processingData) {
        LOG.debug("> process {} with {}", processingData, getClass().getSimpleName());
        
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(processingData);
        if(at.isEmpty()){ //create a new AnalyzedText
            LOG.warn("Unable to preprocess {} because no AnalyzedText is present "
                    + "and this QueryPreperator requires Tokens and Sentences!",
                    processingData);
            return;
        }
        String language = processingData.getLanguage();
        if(language == null || !"de".equals(language.toLowerCase(Locale.ROOT))){
            LOG.debug("language {} not supported (supported: de)", language);
            return; //language not supported
        }

        Annotation document = new Annotation(at.get().getSpan().toLowerCase(Locale.GERMAN));
        //add the AnalyzedText to the document so that the TextSectionAnnotator can do its work
        document.set(AnalyzedTextSectionAnnotator.AnalyzedTextAnnotation.class, at.get());
        
        pipeline.annotate(document); 

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
            int tokenIdx = -1;
            for (CoreLabel token : tokens) {
                if(token.beginPosition() >= token.endPosition()){
                    LOG.warn("Illegal Token start:{}/end:{} values -> ignored", token.beginPosition(), token.endPosition());
                    continue;
                }
                tokenIdx++;
                Token t = at.get().addToken(token.beginPosition(), token.endPosition());
                // Process POS annotations
                String pos = token.get(PartOfSpeechAnnotation.class);
                boolean isNoun = pos != null && pos.length() > 0 && (pos.charAt(0) == 'N' || pos.equals("FM"));
                String span = t.getSpan();
                if(span.length() > 0){
                    char c = span.charAt(0);
                    if(isNoun || tokenIdx == 0){ //upper case
                        if(Character.isAlphabetic(c) && !Character.isUpperCase(c)){
                            t.addAnnotation(NlpAnnotations.TRUE_CASE_ANNOTATION, 
                                    WordUtils.capitalize(span," -–—".toCharArray()));
                        } //else already upper case
                    } else { //lower case
                        if(Character.isAlphabetic(c) && Character.isUpperCase(c)){
                            boolean otherUcChar = false;
                            for(int i = 1; !otherUcChar && i < span.length(); i++){
                                char ch = span.charAt(i);
                                if(Character.isAlphabetic(ch) && Character.isUpperCase(c)){
                                    otherUcChar = true;
                                }
                            }
                            if(!otherUcChar){
                                t.addAnnotation(NlpAnnotations.TRUE_CASE_ANNOTATION, span.toLowerCase(Locale.GERMAN));
                            }
                        }
                    }
                } //else empty token ... nothing to do
            } //end iterate over tokens in sentence
        }
    }
    
    /**
     * Initializes the Annotators as referenced by the '<code>annotators</code>' field of the parsed properties.
     * <p>
     * NOTE: This will only initialize {@link AnnotatorFactories} that are actually used in the configured
     * pipeline (the {@link #getAnnotators()} list)
     * @param properties the properties 
     * @param annotatorImplementation annotator impl instance
     */
    protected void initAnnotatorPool(final Properties properties) {
        AnnotatorImplementations aImpl = new AnnotatorImplementations();
        pool = new AnnotatorPool();
        // if the pool already exists reuse!
        LOG.debug("Initializing Annotator Pool");
        pool.register(STANFORD_TOKENIZE, properties, Lazy.cache(() -> aImpl.tokenizer(properties)));
        pool.register(STANFORD_SSPLIT, properties, Lazy.cache(() -> aImpl.wordToSentences(properties)));
        pool.register(STANFORD_POS, properties, Lazy.cache(() -> aImpl.posTagger(properties)));
        pool.register(REDLINK_AT_SECTION, properties, Lazy.cache(() -> new AnalyzedTextSectionAnnotator()));
    }
    
}
