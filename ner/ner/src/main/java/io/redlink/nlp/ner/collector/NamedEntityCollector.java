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

package io.redlink.nlp.ner.collector;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.annotation.NamedEntity;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Span;
import io.redlink.nlp.model.Span.SpanTypeEnum;
import io.redlink.nlp.model.SpanCollection;
import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.util.NlpUtils;

/**
 * This class collects Named Entity Annotations created (by possible
 * multiple NER components) in the {@link AnalyzedText} and <ol>
 * <li> summarizes {@link NerTag}s on {@link Chunk}s to a list of 
 * {@link NamedEntity}. This list is added to the {@link ProcessingData} 
 * as {@link Annotations#NAMED_ENTITY} annotation
 * <li> deletes all {@link NerTag}s and re-adds summarized versions.
 * </ol>
 * Summarization cares about multiple NER annotations over the same span,
 * partly overlapping NER annotations with the same type. NER annotations
 * fully contained within an other NER annotation regardless of the type of
 * the enclosed one.<p>
 * Confidence values are also adapted in the case of multiple NER annotations
 * and also in case the same Entity is marked multiple times in the same document.
 * <p>
 * NOTS: This component DOES NOT extract Named Entities by itself it only processes
 * NER results of previous processors.
 * 
 * @author Rupert Westenthaler
 *
 */
@Component
public class NamedEntityCollector extends Processor {

    private static final Logger LOG = LoggerFactory.getLogger(NamedEntityCollector.class);

    private static final float DEFAULT_PROB = 0.8f;
    
    public NamedEntityCollector(){
        super("ner.collector","Named Entity Collector", Phase.post, 100); //run late in the post processing phase
    }
    
    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return Collections.emptyMap();
    }

    @Override
    protected void init() throws Exception {
        //no op
    }

    @Override
    protected void doProcessing(io.redlink.nlp.api.ProcessingData processingData) throws ProcessingException {
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(processingData);
        if(at.isPresent()){
            Iterator<? extends SpanCollection> sections = at.get().getSections();
            if(!sections.hasNext()){
                sections = Collections.singleton(at.get()).iterator();
            }
            LOG.trace("collect NamedEntity mentions:");
            Map<String, List<NamedEntityData>> neMap = new HashMap<>();
            while(sections.hasNext()){
                SpanCollection section = sections.next();
                collectNamedEntityMentions(section, neMap);
            }
            for(Entry<String,List<NamedEntityData>> neds : neMap.entrySet()){
                Map<String, NamedEntityData> byType = new HashMap<>();
                LOG.debug("{}", neds.getKey());
                //merge by type
                for(NamedEntityData ned : neds.getValue()){
                    LOG.debug("  {}", ned);
                    NamedEntityData tned = byType.get(ned.type);
                    if(tned == null){
                        byType.put(ned.type, ned);
                    } else {
                        tned.addMention(ned);
                    }
                }
                for(NamedEntityData ned : byType.values()){
                    NamedEntity ne = new NamedEntity(ned.getLemma(), ned.type);
                    ne.setCount(ned.numMentions());
                    processingData.addValue(Annotations.NAMED_ENTITY, Value.value(ne,ned.conf));
                    Chunk chunk = at.get().addChunk(ned.start, ned.end);
                    chunk.addValue(NlpAnnotations.NER_ANNOTATION, Value.value(ned.tag,ned.conf));
                    for(NamedEntityData nedm : ned.mentions){
                        chunk = at.get().addChunk(nedm.start, nedm.end);
                        //NOTE: it is intentional that we use ned.conf for nedm!!
                        chunk.addValue(NlpAnnotations.NER_ANNOTATION, Value.value(nedm.tag, ned.conf));
                    }
                }
            }
        }
    }
    /**
     * Collects NamedEntity mentions in the parsed section and adds them to the neMap
     * @param section
     * @param neMap
     */
    private void collectNamedEntityMentions(SpanCollection section, Map<String, List<NamedEntityData>> neMap) {
        Iterator<Span> chunks = section.getEnclosed(EnumSet.of(SpanTypeEnum.Chunk));
        //we might encounter multiple overlapping Named Entities of the same Type.
        //so we use this map to lookup them and build a token covering them all
        Map<String, NamedEntityData> activeTokens = new HashMap<>();
        while(chunks.hasNext()){
            Chunk chunk = (Chunk)chunks.next();
            List<Value<NerTag>> nerAnnotations = chunk.getValues(NlpAnnotations.NER_ANNOTATION);
            for(Value<NerTag> nerAnno : nerAnnotations){
                NerTag nerTag = nerAnno.value();
                String type = getType(nerTag);
                LOG.trace(" - [{},{}] {} (type:{})", chunk.getStart(), chunk.getEnd(), chunk.getSpan(), type);
                NamedEntityData ned = activeTokens.get(type);
                if(ned != null){
                    if(ned.end <= chunk.getStart()){ //none overlapping start
                        //remove the previous active and add it to the neMap
                        String name = ned.getLemma();
                        List<NamedEntityData> nes = neMap.get(name);
                        if(nes == null){ //first mention of this NamedEntity in the document
                            nes = new LinkedList<>();
                            neMap.put(name, nes); //add
                        }
                        nes.add(ned);
                    } else { //overlapping
                        LOG.trace("    merge {}: {} with {}", chunk, nerAnno, ned);
                        ned.merge(chunk, nerAnno);
                        continue; //processed this one
                    }
                }
                boolean contained = false;
                for(NamedEntityData active : activeTokens.values()){
                    boolean c = active.contained(chunk, nerAnno);
                    if(c){
                        contained = true;
                        LOG.trace("    {}: {} contained in {}", chunk, nerAnno, ned);
                    }
                }
                if(!contained){
                    LOG.trace("    created {} for {}:{}", ned, chunk, nerAnno);
                    activeTokens.put(type, new NamedEntityData(chunk, nerAnno));
                }
            }
            //remove the old NER annotations (will add the collected later on)
            chunk.setAnnotations(NlpAnnotations.NER_ANNOTATION, null);
        }
        //add all remaining NamedEntityData to the neMap
        for(NamedEntityData ned : activeTokens.values()){
            String name = ned.getLemma();
            List<NamedEntityData> nes = neMap.get(name);
            if(nes == null){ //first mention of this NamedEntity in the document
                nes = new LinkedList<>();
                neMap.put(name, nes); //add
            }
            nes.add(ned);
        }
    }

    
    private double sumProbability(double prob1, double prob2){
        if(prob1 == Value.UNKNOWN_PROBABILITY && prob2 == Value.UNKNOWN_PROBABILITY){
            return prob1;
        }
        if(prob1 == Value.UNKNOWN_PROBABILITY){
            prob1 = DEFAULT_PROB;
        }
        if(prob2 == Value.UNKNOWN_PROBABILITY){
            prob2 = DEFAULT_PROB;
        }
        return (prob1 + prob2)/(1 + (prob1*prob2));
    }

    String getType(NerTag tag) {
        if(tag.getType() == null || 
                NerTag.NAMED_ENTITY_UNKOWN.equals(tag.getType())){
            return tag.getTag() == null ? NerTag.NAMED_ENTITY_UNKOWN : tag.getTag();
        } else {
            return tag.getType();
        }
    }
    

    
    private class NamedEntityData implements Comparable<NamedEntityData>{

        final CharSequence context;
        final NerTag tag;
        final String type;
        int start;
        int end;
        double conf;
        List<NamedEntityData> mentions = new LinkedList<>();
        SortedSet<Token> tokens = new TreeSet<>();
        
        NamedEntityData(Chunk chunk, Value<NerTag> tag){
            this.context = chunk.getContext().getText();
            this.tag = tag.value();
            this.type = getType(tag.value());
            this.start = chunk.getStart();
            this.end = chunk.getEnd();
            this.conf = tag.probability();
            for(Iterator<Token> ts = chunk.getTokens(); ts.hasNext();tokens.add(ts.next()));
        }

        public boolean contained(Chunk chunk, Value<NerTag> tag) {
            if(start <= chunk.getStart() && end >= chunk.getEnd() && chunk.getEnd() - chunk.getStart() < end - start){
                //contained and not the same
                if(type.equals(getType(tag.value()))){
                    conf = sumProbability(conf, tag.probability());
                } //else different type ... no change in confidence
                return true;
            } else {
                return false;
            }
            
        }

        public void addMention(NamedEntityData ned) {
            this.conf = sumProbability(conf, ned.conf);
            this.mentions.add(ned);
        }
        
        public int numMentions(){
            return mentions.size() + 1; //add the original mention
        }

        void merge(Chunk chunk, Value<NerTag> tag){
            this.start = Math.min(start, chunk.getStart());
            this.end = Math.max(end, chunk.getEnd());
            this.conf = sumProbability(conf, tag.probability());
            for(Iterator<Token> ts = chunk.getTokens(); ts.hasNext();tokens.add(ts.next()));
        }
        
        /**
         * Looks for Lemmas of all {@link Token}s contained in the parsed {@link Chunk}
         * @param chunk
         * @return
         */
        public String getLemma(){
            StringBuilder sb = new StringBuilder(end - start + 5);
            int lastEnd = start;
            for(Token token : tokens){
                if(token.getStart() > lastEnd){
                    sb.append(context, lastEnd, token.getStart());
                }
                String lemma = NlpUtils.getLemma(token);
                sb.append(lemma == null ? token.getSpan() : lemma);
                lastEnd = token.getEnd();
            }
            if(lastEnd < end){
                sb.append(context,lastEnd,end);
            }
            return sb.toString();
        }

        @Override
        public int compareTo(NamedEntityData o) {
            int c = Integer.compare(start, o.start);
            if(c == 0){
                c = Integer.compare(o.end, end);
            }
            return c;
        }

        @Override
        public String toString() {
            return "NamedEntityData [" + start + "," + end + "| name=" + getLemma() + ", type=" + type + ", conf=" + conf + "]";
        }
        
        
        

    }
    
}
