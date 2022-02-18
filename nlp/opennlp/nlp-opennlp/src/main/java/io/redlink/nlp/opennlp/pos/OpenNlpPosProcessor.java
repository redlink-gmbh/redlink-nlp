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

package io.redlink.nlp.opennlp.pos;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Sentence;
import io.redlink.nlp.model.SpanCollection;
import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.util.NlpUtils;
import opennlp.tools.util.Span;

@Component
public class OpenNlpPosProcessor extends Processor {

    private static final Logger LOG = LoggerFactory.getLogger(OpenNlpPosProcessor.class);

    /**
     * Map holding language to language model mappings. Built up dynamically based
     * on languages parsed to {@link #lookupModel(String)}
     */
    private final Map<String, OpenNlpLanguageModel> _languageModels;
    private final ReadWriteLock languageModelLock;
    private final Collection<OpenNlpLanguageModel> models;
    
    
    @Autowired
    public OpenNlpPosProcessor(Collection<OpenNlpLanguageModel> models){
        super("opennlp.pos", "OpenNLP POS", Phase.pos);
        OpenNlpLanguageModel[] modelArray = models.toArray(new OpenNlpLanguageModel[models.size()]);
        //we need to sort based on their priority
        Arrays.sort(modelArray);
        this.models = Arrays.asList(modelArray);
        _languageModels = new HashMap<>();
        languageModelLock = new ReentrantReadWriteLock();
    }
    
    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return Collections.emptyMap();
    }
    
    @Override
    protected void init() throws Exception {
        int modelCount = models.size();
        Set<String> initLangs = new TreeSet<>();
        for(Iterator<OpenNlpLanguageModel> it = models.iterator(); it.hasNext();){
            OpenNlpLanguageModel model = it.next();
            try {
                model.activate();
                initLangs.add(String.valueOf(model.getLocale()));
            } catch(IOException e){
                LOG.warn("Unable to activate OpenNLP model for language {} ({}: {})",
                        model.getLocale(), e.getClass().getSimpleName(), e.getMessage());
                LOG.debug("STACKTRACE", e);
                it.remove();
            }
        }
        LOG.info("initialized {}/{} models (lang: {})", models.size(), modelCount, initLangs);
    }
    
    @Override
    protected void doProcessing(ProcessingData processingData) {
        String language = processingData.getLanguage();
        AnalyzedText at = NlpUtils.getOrInitAnalyzedText(processingData);
        OpenNlpLanguageModel model = lookupModel(language);
        
        if(model == null || !model.supports(language)){
            LOG.debug("No Model for Language '{}' available. Unable to POS tag {}", language, processingData);
            return;
        }
        
        Iterator<? extends SpanCollection> contentSections = at.getSections();
        if(!contentSections.hasNext()){ //no content sections available
            contentSections = Collections.singleton(at).iterator(); //fall back to the text as a whole
        }
        SpanCollection prevSection = null;
        while(contentSections.hasNext()){
            SpanCollection section = contentSections.next();
            if(prevSection != null && section.getStart() < prevSection.getStart()){ //overlapping sections
                LOG.warn("will ignore overlapping Section in Document {} (prev: {} | overlapping: {})", prevSection, section);
                continue;
            }
            process(model, section);
        }
    }
    /**
     * searches for a suiting model for the parsed language
     * @param language the language
     * return
     */
    private OpenNlpLanguageModel lookupModel(String language) {
        languageModelLock.readLock().lock();
        OpenNlpLanguageModel model = null;
        try {
            if(_languageModels.containsKey(language)){ //language in the cache
                return _languageModels.get(language); //return the cached value
            }
        } finally {
            languageModelLock.readLock().unlock();
        }
        languageModelLock.writeLock().lock();
        try {
            if(_languageModels.containsKey(language)){ //language in the cache
                return _languageModels.get(language); //return the cached value
            }
            String normLanguage = language == null ? null : Locale.forLanguageTag(language).getLanguage();
            for(OpenNlpLanguageModel m : models){
                if(m.supports(normLanguage)){
                    LOG.debug(" - will use Model {} for language {}", m.getName(), language);
                    model = m;
                    break;
                }
            }
            if(model == null && normLanguage != null){ //still not found a language.
                //maybe we can find a model for the root language
                String[] normLangParts = normLanguage.split("-_");
                if(normLangParts.length > 1){
                    for(OpenNlpLanguageModel m : models){
                        if(m.supports(normLangParts[0])){
                            LOG.debug(" - will use Model {} for language {}", m.getName(), language);
                            model = m;
                            break;
                        }
                    }
                }
            }
            if(LOG.isDebugEnabled() && model == null){
                LOG.debug(" - no suiting Model registered for language {}", language);
            }
            _languageModels.put(language, model); // NOTE: this will also add NULL models for unsupported languages
            return model;
        } finally {
            languageModelLock.writeLock().unlock();
        }
    }
    
    private void process(OpenNlpLanguageModel model, SpanCollection section) {
        AnalyzedText at = section.getContext();
        int offset = section.getStart();
        String sectionText = model.isCaseSensitive() ? NlpUtils.toTrueCase(section) : 
            section.getSpan().toLowerCase(model.getLocale());
        Span[] sentSpans = model.split(sectionText);
        String[] sentStrings = Span.spansToStrings(sentSpans, sectionText);
        for(int sidx=0;sidx<sentSpans.length;sidx++){
            Span sentSpan = sentSpans[sidx];
            String sentString = sentStrings[sidx];
            Sentence sentence = at.addSentence(offset + sentSpan.getStart(), offset + sentSpan.getEnd());
            //save guard that asserts that we use the same offsets as OpenNLP
            assert sentString.equals(sentence.getSpan());
            Span[] tokenSpans = model.tokenize(sentString);
            String[] sentTokens = Span.spansToStrings(tokenSpans, sentString);
            List<Value<PosTag>>[] posTags = model.tag(sentTokens);
            for(int tidx=0; tidx < tokenSpans.length; tidx++){
                Span tokenSpan = tokenSpans[tidx];
                Token token = sentence.addToken(tokenSpan.getStart(), tokenSpan.getEnd());
                //save guard that asserts that we use the same offsets as OpenNLP
                assert tokenSpan.getCoveredText(sentString).toString().equals(token.getSpan());
                if(posTags != null){ //POS tagging is supported by the model
                    token.addValues(NlpAnnotations.POS_ANNOTATION, posTags[tidx]);
                }
            }
        }
    }

}
