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

package io.redlink.nlp.negation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.management.VMOption.Origin;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.Sentence;
import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.pos.Pos;
import io.redlink.nlp.model.pos.PosSet;
import io.redlink.nlp.model.util.NlpUtils;

/**
 * This class collects Named Entity Annotations created (by possible
 * multiple NER components) in the {@link AnalyzedText} and creates 
 * {@link Token}s in the {@link Conversation} for those contained
 * in {@link Message}s with {@link Origin#User} and an index greater
 * as {@link ConversationMeta#getLastMessageAnalyzed()}
 * <p>
 * This {@link QueryPreparator} DOES NOT extract Named Entities by itself!
 * 
 * @author Rupert Westenthaler
 *
 */
@Component
public class NegationProcessor extends Processor {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * All tokens indicating a new section within an sentence
     */
    public static final PosSet MEDIAL_PUNCTATION = PosSet.of(Pos.SentenceMedialPunctuation);
    
    /**
     * All Coordinating Conjunction
     */
    public static final PosSet COORD_CONJUNCTION = PosSet.of(Pos.CoordinatingConjunction);

    /**
     * Nouns and ProNouns and Numbers
     */
    public static final PosSet NOUN_PRONOUN_OR_NUMBER = PosSet.of(Pos.Pronoun).add(PosSet.NOUNS).add(Pos.Numeral);
    /**
     * Tokens that can be negated
     */
    public static final PosSet NEGATEABLE = PosSet.union(NOUN_PRONOUN_OR_NUMBER, PosSet.VERBS, PosSet.ADJECTIVES);
    
    private static final int NEGATION_CONTEXT = 2;
    private static final int CONJUCTION_CONTEXT = 1;


    private final Map<String,Collection<NegationRule>> negationRules = new HashMap<>();
    
    @Autowired
    public NegationProcessor(Collection<NegationRule> rules){
        super("negation", "Negetaion Detector", Phase.negation);
        for(NegationRule nr : rules){
            addNegationRule(nr);
        }
    }
    
    @Override
    protected void init() throws Exception {
    }
    
    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return Collections.emptyMap();
    }
    
    private void addNegationRule(NegationRule nr){
        Collection<NegationRule> langRules = negationRules.get(nr.getLanguage());
        if(langRules == null){
            langRules = new LinkedList<>();
            negationRules.put(nr.getLanguage(), langRules);
        }
        langRules.add(nr);
    }
    
    private Collection<NegationRule> getNegationRules(String lang) {
        Collection<NegationRule> negationRules = this.negationRules.get(lang);
        if(negationRules == null){
            log.trace("no specific negation rules for language {} (using default ruleset)", lang);
            negationRules = Collections.singleton(DefaultNegationRule.INSTANCE);
        }
        return negationRules;
    }
    
    private boolean isNegation(Collection<NegationRule> rules, Token token){
        Iterator<NegationRule> it = rules.iterator();
        boolean isNegation = false;
        while(!isNegation && it.hasNext()){
            isNegation = it.next().isNegation(token);
        }
        return isNegation;
    }

    
    @Override
    protected void doProcessing(ProcessingData processingData) {
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(processingData);
        if(at.isPresent()){
            String lang = processingData.getLanguage();
            Collection<NegationRule> negationRules = getNegationRules(lang);
            Iterator<Sentence> sentences = at.get().getSentences();
            while(sentences.hasNext()){
                markNegations(negationRules, sentences.next());
            }
        } else {
            log.trace("Unable to process {} because no AnalyzedText is present");
        }
    }

    private void markNegations(Collection<NegationRule> negationRules, Sentence sentence) {
        if(log.isTraceEnabled()){
            log.trace("Sentence  [{},{}]: {}", sentence.getStart(), sentence.getEnd(), sentence.getSpan());
        }
        Iterator<Token> tokens = sentence.getTokens();
        NavigableMap<Integer,Token> negations = new TreeMap<Integer,Token>();
        NavigableMap<Integer,Token> negateable = new TreeMap<Integer,Token>();
        //NavigableMap<Integer,Token> verbs = new TreeMap<Integer,Token>();
        //NavigableMap<Integer,Token> adjectives = new TreeMap<Integer,Token>();
        NavigableMap<Integer,Token> conjuctions = new TreeMap<Integer,Token>();
        NavigableMap<Integer,Token> sectionBorders = new TreeMap<Integer,Token>();
        List<Token> tokenList = new ArrayList<>();
        while(tokens.hasNext()){
            final Token token = tokens.next();
            Integer idx = tokenList.size();
            tokenList.add(token);
            log.trace(" {}. {}: {}", idx, token, token.getValues(NlpAnnotations.POS_ANNOTATION));
            if(isNegation(negationRules,token)){
                negations.put(idx, token);
            }
            if(NlpUtils.isOfPos(token, NEGATEABLE)){
                negateable.put(idx, token);
            }
            if(NlpUtils.isOfPos(token, COORD_CONJUNCTION)){
                conjuctions.put(idx, token);
            }
            if(NlpUtils.isOfPos(token, MEDIAL_PUNCTATION)){
                sectionBorders.put(idx, token);
            }
        }
        Integer[] searchSpan = new Integer[]{-1,-1};
        for(Entry<Integer,Token> negation : negations.entrySet()){
            Integer index = negation.getKey();
            if(index.compareTo(searchSpan[1]) > 0) {
                searchSpan[0] = sectionBorders.floorKey(index);
                if(searchSpan[0] == null) {
                    searchSpan[0] = Integer.valueOf(0);
                }
                searchSpan[1] = sectionBorders.ceilingKey(index);
                if(searchSpan[1] == null) {
                    searchSpan[1] = Integer.valueOf(tokenList.size()-1);
                }
            }
            int notfound = 0;
            Integer start = index;
            Integer end = index;
            for(int i = index + 1;i < searchSpan[1] && notfound < NEGATION_CONTEXT; i++){
                Integer idx = Integer.valueOf(i);
                if(conjuctions.containsKey(idx)){
                    notfound = Math.max(0, notfound - CONJUCTION_CONTEXT);
                } else if(!negateable.containsKey(idx)){
                    notfound ++;
                } else {
                    end = idx;
                }
            }
            if(end == index){
                notfound = 0;
                for(int i = index -1; i >= searchSpan[0] && notfound < NEGATION_CONTEXT; i--){
                    Integer idx = Integer.valueOf(i);
                    if(conjuctions.containsKey(idx)){
                        notfound = Math.max(0, notfound - CONJUCTION_CONTEXT);
                    } else if(!negateable.containsKey(idx)){
                        notfound ++;
                    } else {
                        start = idx;
                    }
                }
            }
            int startChar = tokenList.get(start).getStart();
            int endChar = tokenList.get(end).getEnd();
            if(log.isDebugEnabled()){
                log.debug("  - negation: [{},{} | neg: {}] '{}'", startChar, endChar, negation.getValue(), 
                        sentence.getSpan().substring(startChar - sentence.getStart(), endChar - sentence.getStart()));
            }
            Chunk negatedChunk = sentence.addChunk(startChar - sentence.getStart(), endChar - sentence.getStart());
            negatedChunk.addAnnotation(NlpAnnotations.NEGATION_ANNOTATION, Boolean.TRUE);
        }
        
    }

}
