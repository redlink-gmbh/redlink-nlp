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

package io.redlink.nlp.stanfordnlp;

import static edu.stanford.nlp.pipeline.Annotator.*;
import static io.redlink.nlp.stanfordnlp.annotators.RedlinkAnnotator.REDLINK_AT_SECTION;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.AnnotatorImplementations;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.Lazy;
import io.redlink.nlp.model.dep.RelTag;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.phrase.PhraseTag;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.tag.TagSet;
import io.redlink.nlp.stanfordnlp.annotators.AnalyzedTextSectionAnnotator;

/**
 * A language specific configuration for OpenNLP based NER recognitions.
 * Subclasses need to register them self as {@link Service}s so that they can
 * get injected to the {@link StanfordNlpProcessor}
 * 
 * @author Rupert Westenthaler
 *
 */
public abstract class StanfordNlpPipeline {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    //for now we are happy with the defaults
    private AnnotatorImplementations annotatorImplementation = new AnnotatorImplementations();
    
    private final String name;
    private final Locale locale;

    private boolean activated;
    private Properties properties;
    
    
    private AnnotationPipeline pipeline;
    private AnnotatorPool pool;

    /**
     * Stores {@link TagSet} for string POS tags returned by the pipeline
     * but but mapped in the {@link #posTagset}.
     */
    private final Map<String,PosTag> adhocPosTags = new HashMap<>();


    /**
     * Stores {@link TagSet} for string NER tags returned by the pipeline
     * but but mapped in the {@link #nerTagset}.
     */
    private final Map<String,NerTag> adhocNerTags = new HashMap<>();
    
    /**
     * Stores a set of unmapped {@link RelTag}s as encountered
     * during parsing but not mapped in the {@link #relTagset}
     */
    private final Map<String,RelTag> adhocRelTags = new HashMap<>();

    /**
     * Stores a set of unmapped {@link PhraseTag}s as encountered
     * during parsing but not mapped in the {@link #relTagset}
     */
    private Map<String, PhraseTag> adhocPhraseTags = new HashMap<>();

    private List<String> annotators;

    private boolean caseSensitive = true;

    protected StanfordNlpPipeline(String name, Locale locale){
        assert StringUtils.isNotBlank(name);
        this.name = name;
        assert locale != null;
        this.locale = locale;
    }

    /**
     * Getter for the {@link TagSet} of known {@link PosTag}s.
     * @return the {@link PosTag} set or <code>null</code> if none is available.
     */
    protected abstract TagSet<PosTag> getPosTagset();
    
    /**
     * Getter for the {@link TagSet} of known {@link NerTag}s.
     * @return the {@link NerTag} set or <code>null</code> if none is available.
     */
    protected abstract TagSet<NerTag> getNerTagset();
    
    /**
     * Getter for the {@link TagSet} of known {@link PhraseTag}s.
     * @return the {@link PhraseTag} set or <code>null</code> if none is available.
     */
    protected abstract TagSet<PhraseTag> getPhraseTagset();

    /**
     * Getter for the {@link TagSet} of known {@link RelTag}s.
     * @return the {@link RelTag} set or <code>null</code> if none is available.
     */
    protected abstract TagSet<RelTag> getRelTagset();
    
    /**
     * The Treebank LanguagePack
     * @return The treebank LanguagePack or <code>null</code> if none available
     */
    public abstract TreebankLanguagePack getLanguagePack();
    
    /**
     * The properties used for {@link #activate()}. This MUST BE set before activating the component
     * @param properties the properties
     */
    protected final void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Setter for the caseSensitive state. If this pipeline uses caseless models this
     * MUST BE set to <code>false</code> so that the extractor does parse a lower case
     * version of the text
     * @param caseSensitive the case sensitive state
     */
    protected void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    
    /**
     * Getter for the properties holding the Stanford NLP configuration used by this component
     * @return the properties or <code>null</code> if not set.
     */
    public Properties getProperties() {
        return properties;
    }
    
    /**
     * If this pipeline uses case sensitive models
     * @return
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    /**
     * Read only list of the annotators used by the configured pipeline.
     * Only available after {@link #activate() activation}
     * @return the list of annotator (names) used in the pipeline
     */
    public List<String> getAnnotators() {
        return annotators;
    }
    
    /**
     * Getter for the {@link Annotator}. Only available after {@link #activate() activation}
     * @param name the name of the annotator as configured in the pipeline.
     * Valid strings are member of the {@link #getAnnotators()} list
     * @return the Annotator
     * @throws IllegalStateException if the parsed name is not part of the
     * {@link #getAnnotators()} list
     */
    public Annotator getAnnotator(String name){
        return pool.get(name);
    }
    
    /**
     * Sets a custom {@link AnnotatorImplementations} class. Needs to be called before
     * {@link #activate() activation} (typically as part of the constructor of the sub-class)
     * @param annotatorImplementation
     */
    protected final void setAnnotatorImplementation(AnnotatorImplementations annotatorImplementation) {
        this.annotatorImplementation = annotatorImplementation;
    }
    
    /**
     * Activates the component based on its configuration defined in the #
     * @throws IOException
     */
    public final void activate() throws IOException {
        log.info("> activating {} (locale: {})",getClass().getSimpleName(),locale);
        if(properties == null){
            throw new IllegalStateException("The properties MUST BE set before activation!");
        }
        annotators = Collections.unmodifiableList(
                Arrays.asList(properties.getProperty("annotators","").split("[, \t]+")));
        assert !annotators.isEmpty();
        initAnnotatorPool(properties);
        pipeline = new AnnotationPipeline();
        for(String annotatorName : annotators){
            Annotator annotator = pool.get(annotatorName);
            if(annotator == null){
                throw new IllegalStateException("Unable to instantiate Annotator '"
                        + annotatorName + "' used by Pipeline " + annotators + "!");
            }
            pipeline.addAnnotator(annotator);
        }
        doActivate();
        activated = true;
    }
    
    
    /**
     * activation hook
     */
    protected void doActivate() {}

    /**
     * Initializes the Annotators as referenced by the '<code>annotators</code>' field of the parsed properties.
     * <p>
     * NOTE: This will only initialize {@link AnnotatorFactories} that are actually used in the configured
     * pipeline (the {@link #getAnnotators()} list)
     * @param properties the properties 
     * @param annotatorImplementation annotator impl instance
     */
    protected void initAnnotatorPool(final Properties properties) {
        log.debug("init pipeline with {}", properties);
        Set<String> annotators = new HashSet<>(this.annotators); //copy as we want to delete found
        pool = new AnnotatorPool();
        // if the pool already exists reuse!
        log.debug("Initializing Annotator Pool");
        if(annotators.remove(STANFORD_TOKENIZE)){
            pool.register(STANFORD_TOKENIZE, properties, Lazy.cache(() -> annotatorImplementation.tokenizer(properties)));
        }
        if(annotators.remove(STANFORD_CLEAN_XML)){
            pool.register(STANFORD_CLEAN_XML, properties, Lazy.cache(() -> annotatorImplementation.cleanXML(properties)));
        }
        if(annotators.remove(STANFORD_SSPLIT)){
            pool.register(STANFORD_SSPLIT, properties, Lazy.cache(() -> annotatorImplementation.wordToSentences(properties)));
        }
        if(annotators.remove(STANFORD_POS)){
            pool.register(STANFORD_POS, properties, Lazy.cache(() -> annotatorImplementation.posTagger(properties)));
        }
        if(annotators.remove(STANFORD_LEMMA)){
            pool.register(STANFORD_LEMMA, properties, Lazy.cache(() -> annotatorImplementation.morpha(properties,false)));
        }
        if(annotators.remove(STANFORD_NER)){
            pool.register(STANFORD_NER, properties, Lazy.cache(() -> annotatorImplementation.ner(properties)));
        }
        if(annotators.remove(STANFORD_TOKENSREGEX)){
            pool.register(STANFORD_TOKENSREGEX, properties, Lazy.cache(() -> annotatorImplementation.tokensregex(properties, STANFORD_TOKENSREGEX)));
        }
        if(annotators.remove(STANFORD_REGEXNER)){
            pool.register(STANFORD_REGEXNER, properties, Lazy.cache(() -> annotatorImplementation.tokensRegexNER(properties, STANFORD_REGEXNER)));
        }
        if(annotators.remove(STANFORD_ENTITY_MENTIONS)){
            pool.register(STANFORD_ENTITY_MENTIONS, properties, Lazy.cache(() -> annotatorImplementation.entityMentions(properties, STANFORD_ENTITY_MENTIONS)));
        }
        if(annotators.remove(STANFORD_GENDER)){
            pool.register(STANFORD_GENDER, properties, Lazy.cache(() -> annotatorImplementation.gender(properties, false)));
        }
        if(annotators.remove(STANFORD_TRUECASE)){
            pool.register(STANFORD_TRUECASE, properties, Lazy.cache(() -> annotatorImplementation.trueCase(properties)));
        }
        if(annotators.remove(STANFORD_PARSE)){
            pool.register(STANFORD_PARSE, properties, Lazy.cache(() -> annotatorImplementation.parse(properties)));
        }
        if(annotators.remove(STANFORD_MENTION)){
            pool.register(STANFORD_MENTION, properties, Lazy.cache(() -> annotatorImplementation.mention(properties)));
        }
        if(annotators.contains(STANFORD_DETERMINISTIC_COREF)){
            pool.register(STANFORD_DETERMINISTIC_COREF, properties, Lazy.cache(() -> annotatorImplementation.dcoref(properties)));
        }
        if(annotators.remove(STANFORD_COREF)){
            pool.register(STANFORD_COREF, properties, Lazy.cache(() -> annotatorImplementation.coref(properties)));
        }
        if(annotators.remove(STANFORD_RELATION)){
            pool.register(STANFORD_RELATION, properties, Lazy.cache(() -> annotatorImplementation.relations(properties)));
        }
        if(annotators.remove(STANFORD_SENTIMENT)){
            pool.register(STANFORD_SENTIMENT, properties, Lazy.cache(() -> annotatorImplementation.sentiment(properties, STANFORD_SENTIMENT)));
        }
        if(annotators.remove(STANFORD_COLUMN_DATA_CLASSIFIER)){
            pool.register(STANFORD_COLUMN_DATA_CLASSIFIER, properties, Lazy.cache(() -> annotatorImplementation.columnData(properties)));
        }
        if(annotators.remove(STANFORD_DEPENDENCIES)){
            pool.register(STANFORD_DEPENDENCIES, properties, Lazy.cache(() -> annotatorImplementation.dependencies(properties)));
        }
        if(annotators.remove(STANFORD_NATLOG)){
            pool.register(STANFORD_NATLOG, properties, Lazy.cache(() -> annotatorImplementation.natlog(properties)));
        }
        if(annotators.remove(STANFORD_OPENIE)){
            pool.register(STANFORD_OPENIE, properties, Lazy.cache(() -> annotatorImplementation.openie(properties)));
        }
        if(annotators.remove(STANFORD_QUOTE)){
            pool.register(STANFORD_QUOTE, properties, Lazy.cache(() -> annotatorImplementation.quote(properties)));
        }
        if(annotators.remove(STANFORD_UD_FEATURES)){
            pool.register(STANFORD_UD_FEATURES, properties, Lazy.cache(() -> annotatorImplementation.udfeats(properties)));
        }
        
        // Redlink specific Annotator
        if(annotators.remove(REDLINK_AT_SECTION)){
            pool.register(REDLINK_AT_SECTION, properties, Lazy.cache(() -> new AnalyzedTextSectionAnnotator()));
        }

        // add annotators loaded via reflection from class names specified
        // in the properties
        for (String property : properties.stringPropertyNames()) {
            if (property.startsWith(StanfordCoreNLP.CUSTOM_ANNOTATOR_PREFIX)) {
                final String customName = property.substring(StanfordCoreNLP.CUSTOM_ANNOTATOR_PREFIX.length());
                final String customClassName = properties.getProperty(property);
                if(annotators.remove(customName)){
                    log.debug("Registering annotator " + customName + " with class " + customClassName);
                    pool.register(customName, properties, Lazy.cache(() -> annotatorImplementation.custom(properties, property)));
                }
            }
        }
        if(!annotators.isEmpty()){
            log.error("Unable to initialize Stanford NLP Pipeline for {} becuse of {} missing Annotator(s) {}", 
                    locale, annotators.size(), annotators);
            throw new IllegalStateException("Missing Required Annotators "+ annotators);
        }
    }

    public final boolean isActive(){
        return activated;
    }
    
    public final Locale getLocale() {
        return locale;
    }
    
    /**
     * The language supported by the NerModel. This method
     * can be called before {@link #activate() activation}
     * @return the ISO 639-1 language code (e.g. "en" for English)
     */
    public final String getLanguage(){
        Locale l = getLocale();
        return l == null ? null : l.getLanguage();
    }

    public final String getName() {
        return name;
    }
    
    public final AnnotationPipeline getPipeline() {
        return pipeline;
    }
  
    /**
     * Uses the {@link #posTagset} and {@link #adhocPosTags} to return existing instances
     * of {@link PosTag}s. If not present it will create a new one and add it to
     * {@link #adhocPosTags}
     * @param tag the String pos tag as returned by the {@link #tagger}
     * @return the {@link PosTag} or <code>null</code> if the parsed tag was
     * blank AND blank (incl. <code>null</code>) tags are not mapped by the
     * POS {@link TagSet}.
     */
    public final PosTag getPosTag(String tag) {
        TagSet<PosTag> posTagset = getPosTagset();
        PosTag posTag = posTagset == null ? null : posTagset.getTag(tag);
        if(posTag != null){
            return posTag;
        }
        posTag = adhocPosTags.get(tag);
        if(posTag != null){
            return posTag;
        }
        if(StringUtils.isNotBlank(tag)){
            posTag = new PosTag(tag);
            adhocPosTags.put(tag, posTag);
            log.info("Encountered umapped POS tag '{}' for langauge '{}'", tag, locale.getLanguage());
            return posTag;
        } else {
            return null;
        }
    }
    
    
    
    /**
     * Uses the {@link #posTagset} and {@link #adhocPosTags} to return existing instances
     * of {@link PosTag}s. If not present it will create a new one and add it to
     * {@link #adhocPosTags}
     * @param tag the String pos tag as returned by the {@link #tagger}
     * @return the {@link PosTag} guaranteed to be not <code>null</code>
     */
    public final NerTag getNerTag(String tag) {
        TagSet<NerTag> nerTagset = getNerTagset();
        NerTag nerTag = nerTagset == null ? null : nerTagset.getTag(tag);
        if(nerTag != null){
            return nerTag;
        }
        nerTag = adhocNerTags.get(tag);
        if(nerTag != null){
            return nerTag;
        }
        if(StringUtils.isBlank(tag)){ //for NULL or blank use UNKOWN
            nerTag = new NerTag(tag,NerTag.NAMED_ENTITY_UNKOWN);
        } else { //else use other
            nerTag = new NerTag(tag,NerTag.NAMED_ENTITY_MISC);
        }
        adhocNerTags.put(tag, nerTag);
        log.info("Encountered umapped Ner tag '{}' for langauge '{}'", tag, locale.getLanguage());
        return nerTag;
    }

    /**
     * Uses the {@link #posTagset} and {@link #adhocPosTags} to return existing instances
     * of {@link PosTag}s. If not present it will create a new one and add it to
     * {@link #adhocPosTags}
     * @param tag the String pos tag as returned by the {@link #tagger}
     * @return the {@link PosTag} guaranteed to be not <code>null</code>
     */
    public final RelTag getRelationTag(String tag) {
        TagSet<RelTag> relTagset = getRelTagset();
        RelTag relTag = relTagset == null ? null : relTagset.getTag(tag);
        if(relTag != null){
            return relTag;
        }
        relTag = adhocRelTags.get(tag);
        if(relTag != null){
            return relTag;
        }
        relTag = new RelTag(tag);
        adhocRelTags.put(tag, relTag);
        log.info("Encountered umapped gramatical relation tag '{}' for langauge '{}'", tag, locale.getLanguage());
        return relTag;
    }

    /**
     * Uses the {@link #getPhraseTagset()} and {@link #adhocPhraseTags} to return existing instances
     * of {@link PhraseTag}s. If not present it will create a new one and add it to
     * {@link #adhocPhraseTags}
     * @param tag the String phrase tag as returned by the parser
     * @return the {@link PhraseTag} guaranteed to be not <code>null</code>
     */
    public final PhraseTag getPhraseTag(String tag) {
        TagSet<PhraseTag> phraseTagset = getPhraseTagset();
        PhraseTag phraseTag = phraseTagset == null ? null : phraseTagset.getTag(tag);
        if(phraseTag != null){
            return phraseTag;
        }
        phraseTag = adhocPhraseTags.get(tag);
        if(phraseTag != null){
            return phraseTag;
        }
        phraseTag = new PhraseTag(tag);
        adhocPhraseTags.put(tag, phraseTag);
        log.info("Encountered umapped phrase tag '{}' for langauge '{}'", tag, locale.getLanguage());
        return phraseTag;
    }

    
    
    public final void deactivate() {
        activated = false;
        doDeactivate();
        pipeline = null;
        pool = null;
    }

    /**
     * Deactivation hook
     */
    protected void doDeactivate() {}

    @Override
    public String toString() {
        return "StanfordNlpPipeline [name=" + name + ", locale=" + locale + ", activated=" + activated + ", annotators=" + annotators + "]";
    }

    
}
