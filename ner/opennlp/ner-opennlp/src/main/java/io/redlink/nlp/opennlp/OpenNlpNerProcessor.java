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

package io.redlink.nlp.opennlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.Sentence;
import io.redlink.nlp.model.Span.SpanTypeEnum;
import io.redlink.nlp.model.SpanCollection;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.util.NlpUtils;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.util.Span;

/**
 * The Named Entity {@link Preprocessor} used for extracting named entities
 * from processed documents.
 * 
 * @author rupert.westenthaler@redlink.co
 */
@Component
public class OpenNlpNerProcessor extends Processor {

    private static final Logger LOG = LoggerFactory.getLogger(OpenNlpNerProcessor.class);

    private static final int CONTENT_INTERRUPTION = 80;

    private final List<OpenNlpNerModel> nerModels;

    private final Map<String, OpenNlpNerModel> lang2NerModel;
    

    @Autowired
    public OpenNlpNerProcessor(List<OpenNlpNerModel> nerModels) {
        super("opennlp.ner", "OpenNLP Named Entity Recognition", Phase.ner);
        LOG.debug("Create {} (with {} NER Models)", getClass().getSimpleName(), nerModels.size());
        this.nerModels = nerModels;
        lang2NerModel = new HashMap<>();
    }

    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return Collections.emptyMap();
    }
    
    @Override
    protected void init() {
        LOG.debug("Initializing {} NER Models", nerModels.size());
        for(OpenNlpNerModel nerModel : nerModels){
            if(lang2NerModel.containsKey(nerModel.getLanguage())){
                LOG.warn("Multiple NER Models for Language {} (in-use: {} | ignored: {})",
                        nerModel.getLanguage(), lang2NerModel.get(nerModel.getLanguage()).getName(), nerModel.getName());
            } else {
                LOG.debug("  {}: {}", nerModel.getLanguage(), nerModel.getName());
                try {
                    nerModel.activate();
                    lang2NerModel.put(nerModel.getLanguage(), nerModel);
                } catch (IOException e) {
                    LOG.warn("Unable to activate NER Model for Language " + nerModel.getLanguage()
                            + " (name: " + nerModel.getName() + ")!", e);
                }
            }
        }
        LOG.debug("NER Models loaded: {}", lang2NerModel.keySet());
    }


    @PreDestroy
    protected void destroyNerModels(){
        lang2NerModel.clear();
        for(OpenNlpNerModel model : nerModels){
            if(model.isActive()){
                model.deactivate();
            }
        }
        
    }

    /**
     * Getter for the NER Model for the requested language
     * @param language the language (lower case)
     * @return the activated {@link OpenNlpNerModel} or <code>null</code> if none
     * is available for the requested language
     */
    private OpenNlpNerModel getModel(String language){
        OpenNlpNerModel model = lang2NerModel.get(language);
        return model != null && model.isActive() ? model : null;
    }
    

    @Override
    protected void doProcessing(ProcessingData processingData) {

        LOG.debug("> process {} with {}", processingData, getClass().getSimpleName());
        
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(processingData);
        if(!at.isPresent()) {
            LOG.warn("Unable to preprocess conversation {} because no AnalyzedText is present "
                    + "and this QueryPreperator requires Tokens and Sentences!",
                    processingData);
            return;
        }
        String language = processingData.getLanguage();
        LOG.debug(" - language: {}", language);
        if(language == null || language.length() < 2){
            LOG.warn("Unable to process {} because missing/invalid language {}", processingData, language);
            return;
        }

        OpenNlpNerModel model = getModel(language);

        if(model == null){
            LOG.debug("Unable to preprocess conversation {} because language {} "
                    + "is not supported", processingData, language);
            return;
        }
        //get the sections of the content
        Iterator<? extends SpanCollection> contentSections = at.get().getSections();
        if(!contentSections.hasNext()){ //fallback use the AnalyzedText as a whole
            contentSections = Collections.singleton(at.get()).iterator();
        }
        //try to get sentences from content sections
        Collection<SpanCollection> sentences = new LinkedList<>();
        while(contentSections.hasNext()){
            SpanCollection section = contentSections.next();
            Iterator<io.redlink.nlp.model.Span> sents = section.getEnclosed(EnumSet.of(SpanTypeEnum.Sentence));
            if(sents.hasNext()){
                while(sents.hasNext()){
                    sentences.add((Sentence)sents.next());
                }
            } else {
                sentences.add(section); //process the whole section as a single sentence
            }
        }
        extractNamedEntities(model, sentences);
    }

    private void extractNamedEntities(OpenNlpNerModel langNerModel, Iterable<SpanCollection> sentences) {
        AnalyzedText at = null;
        int lastEnd = 0; //the end of the last processed sentence (used to track if we need to reset adaptive data in the NameFinder)
        LOG.trace("> extract Named Entities");
        try {
            nextSentence : for(SpanCollection sentence : sentences){
                if(at == null){ //init the Analyzed Text field
                    at = sentence.getContext(); //with the first processed sentence
                }
                int offset = sentence.getStart();
                Iterator<io.redlink.nlp.model.Token> tokenIt = sentence.getTokens();
                if(!tokenIt.hasNext()){
                    LOG.warn("{} {} has not Tokens. Will not extract Named Entities",
                            sentence, StringUtils.abbreviate(sentence.getSpan(), 40));
                    continue nextSentence;
                }
                List<io.redlink.nlp.model.Token> tokens = new ArrayList<>();
                while(tokenIt.hasNext()){
                    tokens.add(tokenIt.next());
                }
                String[] spans = new String[tokens.size()];
                for(int i=0;i < spans.length;i++){
                    //use the case sensitive state to get the correct token
                    spans[i] = langNerModel.isCaseSensitive() ? 
                            NlpUtils.toTrueCase(tokens.get(i)) : //this uses true case annotations (if present)
                                tokens.get(i).getSpan().toLowerCase(langNerModel.getLocale()); //to lower case
                }
                if((offset - lastEnd) > CONTENT_INTERRUPTION){ //reset statistics
                    LOG.trace(" - content interuption (clear adaptive data of NER models)");
                    for(NameFinderModel model : langNerModel.getNameFinders()){
                        NameFinderME nameFinder = model.getNameFinder();
                        if(nameFinder != null){ //might be null if deactivating
                            nameFinder.clearAdaptiveData();
                        }
                    }
                }
                if(LOG.isTraceEnabled()){
                    LOG.trace("> sentence: {}: {}", sentence, Arrays.toString(spans));
                }
                for(NameFinderModel model : langNerModel.getNameFinders()){
                    NameFinderME nameFinder = model.getNameFinder();
                    if(nameFinder != null){ //might be null if deactivating
                        Span[] entitySpans = nameFinder.find(spans);
                        if(entitySpans != null){
                            double[] probs = nameFinder.probs();
                            for(int i = 0; i< entitySpans.length; i++){
                                Span entitySpan = entitySpans[i];
                                String tag = entitySpan.getType();
                                String type = model.getType(tag);
                                if(type == null){
                                    LOG.warn("Unmapped Type '{}' for OpenNLP Name Finder "
                                            + "Model '{}' (lang: {}). Setting type to '{}'",
                                            entitySpan.getType(), model,
                                            langNerModel.getLanguage(), NerTag.NAMED_ENTITY_MISC);
                                    type = NerTag.NAMED_ENTITY_MISC;
                                }
                                double prob = probs[i];
                                int start = tokens.get(entitySpan.getStart()).getStart();
                                int end = tokens.get(entitySpan.getEnd()-1).getEnd();
                                Chunk chunk = at.addChunk(start, end); // add a chunk for the Named Entity
                                chunk.addValue(NlpAnnotations.NER_ANNOTATION, Value.value(new NerTag(tag, type),prob));
                                String name = at.getText().subSequence(start, end).toString();
                                LOG.debug(" - Named Entity [{},{} | prob: {}, tag: {}, type: {}] {}",
                                        start, end, prob, tag, type, name);
                            } //end for all extracted named entities
                        } //else no entities extracted
                    } //else no NameFinder available for this model 
                } //end for all models of the contents language
                
                //set the lastEnd to the end of the current sentence
                lastEnd = sentence.getEnd(); 
            
            } //end for all content Sentences to process
        } finally { //finally we want to clear adaptive data from the use NameFinder
            //otherwise results of the previous Document might affect those of the next
            for(NameFinderModel model : langNerModel.getNameFinders()){
                NameFinderME nameFinder = model.getNameFinder();
                if(nameFinder != null){
                    nameFinder.clearAdaptiveData();
                }
            }
        }
    }
}
