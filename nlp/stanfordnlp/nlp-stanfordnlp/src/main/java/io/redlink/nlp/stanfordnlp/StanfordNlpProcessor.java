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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Filters;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Sentence;
import io.redlink.nlp.model.Span;
import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.dep.RelTag;
import io.redlink.nlp.model.dep.Relation;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.phrase.PhraseTag;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.util.NlpUtils;
import io.redlink.nlp.stanfordnlp.annotators.AnalyzedTextSectionAnnotator;
import io.redlink.nlp.stanfordnlp.sentiment.LinearSentimentClassMapping;
import io.redlink.nlp.stanfordnlp.sentiment.SentimentClassMapping;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.ejml.simple.SimpleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * The Named Entity {@link Preprocessor} used for extracting named entities
 * from processed documents.
 *
 * @author rupert.westenthaler@redlink.co
 */
@Component
@ConditionalOnClass(AnnotationPipeline.class)
public class StanfordNlpProcessor extends Processor {

    private static final Logger LOG = LoggerFactory.getLogger(StanfordNlpProcessor.class);

    private static final int CONTENT_INTERRUPTION = 80;

    private final List<StanfordNlpPipeline> pipelines;

    private final Map<String, StanfordNlpPipeline> lang2Pipeline;

    /**
     * TODO: Make configurable if dependent dependency relations are written
     */
    private boolean writeDependent = true;


    @Autowired
    public StanfordNlpProcessor(List<StanfordNlpPipeline> pipelines) {
        super("stanfordnlp", "Stanford NLP", Phase.pos); //this does token, sent, pos and ner
        LOG.debug("Create {} (with {} NER Models)", getClass().getSimpleName(), pipelines.size());
        this.pipelines = pipelines;
        lang2Pipeline = new HashMap<>();
    }

    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return new HashMap<>();
    }

    protected void init() {
        //The Annotator Pool is just holding the implementation not any NLP models
        for (StanfordNlpPipeline pipeline : pipelines) {
            String lang = pipeline.getLanguage();
            if (StringUtils.isNotBlank(lang)) {
                lang = lang.toLowerCase(Locale.ROOT);
                if (lang2Pipeline.containsKey(lang)) {
                    LOG.warn("Multiple pipelines defined for Language {} (present: {} | ignored: {}",
                            lang, lang2Pipeline.get(lang).getName(), pipeline.getName());
                } else {
                    try {
                        pipeline.activate();
                        lang2Pipeline.put(lang, pipeline);
                    } catch (IOException e) {
                        LOG.warn("Unable to activate " + pipeline, e);
                    }
                }
            } else {
                LOG.warn("Pipeline '{}' does not define a Language tag", pipeline.getName());
            }
        }

    }


    @PreDestroy
    protected void destroyNerModels() {
        lang2Pipeline.clear();
        for (StanfordNlpPipeline pipeline : pipelines) {
            if (pipeline.isActive()) {
                pipeline.deactivate();
            }
        }

    }

    /**
     * Getter for the NER Model for the requested language
     *
     * @param language the language (lower case)
     * @return the activated {@link StanfordNlpPipeline} or <code>null</code> if none
     * is available for the requested language
     */
    private StanfordNlpPipeline getPipeline(String language) {
        StanfordNlpPipeline model = lang2Pipeline.get(language == null ? null : language.toLowerCase(Locale.ROOT));
        return model != null && model.isActive() ? model : null;
    }

    @Override
    protected void doProcessing(io.redlink.nlp.api.ProcessingData processingData) {
        LOG.debug("> process {} with {}", processingData, getClass().getSimpleName());

        String language = processingData.getLanguage();
        AnalyzedText at = NlpUtils.getOrInitAnalyzedText(processingData);
        LOG.debug(" - language: {}", language);
        if (language == null || language.length() < 2) {
            LOG.warn("Unable to preprocess conversation {} because missing/invalid language {}",
                    processingData, language);
            return;
        }
        Locale locale = Locale.forLanguageTag(language);

        StanfordNlpPipeline pipeline = getPipeline(language);

        if (pipeline == null) {
            LOG.debug("Unable to preprocess conversation {} because language {} "
                    + "is not supported", processingData, language);
            return;
        }
        Annotation document = new Annotation(pipeline.isCaseSensitive() ?
                NlpUtils.toTrueCase(at) : //for case sensitive models get the case corrected version of the parsed text
                at.getSpan().toLowerCase(locale)); //otherwise use the lower case version
        //add the AnalyzedText to the document so that the TextSectionAnnotator can do its work
        document.set(AnalyzedTextSectionAnnotator.AnalyzedTextAnnotation.class, at);

        pipeline.getPipeline().annotate(document);

        //lazily initialized when we need to process sentiment annotations
        SentimentClassMapping sentClassMapping = null;

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            Token sentStart = null;
            Token sentEnd = null;
            Token nerStart = null;
            Token nerEnd = null;
            NerTag nerTag = null;
            List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
            SemanticGraph dependencies = sentence.get(BasicDependenciesAnnotation.class);

            int tokenIdxInSentence = 0;

            for (CoreLabel token : tokens) {
                if (token.beginPosition() >= token.endPosition()) {
                    LOG.warn("Illegal Token start:{}/end:{} values -> ignored", token.beginPosition(), token.endPosition());
                    continue;
                }
                Token t = at.addToken(token.beginPosition(), token.endPosition());
                // This can be used to ensure that the text indexes are correct
//              String word = token.get(OriginalTextAnnotation.class);
//              String span = t.getSpan();
//              if(!word.equals(span)){
//                  log.warn("word: '{}' != span: '{}'",word,span);
//              }
                if (sentStart == null) {
                    sentStart = t;
                }
                sentEnd = t;
                // Process POS annotations
                String pos = token.get(PartOfSpeechAnnotation.class);
                PosTag posTag = pipeline.getPosTag(pos);
                if (posTag != null) {
                    LOG.trace(" > '{}' pos: {}", t.getSpan(), posTag);
                    t.addAnnotation(NlpAnnotations.POS_ANNOTATION, posTag);
                } //no POS Tag assigned
                // Process NER annotations
                String ne = token.get(NamedEntityTagAnnotation.class);
                //NOTE: '0' is used to indicate that the current token is no 
                //      named entities
                NerTag actNerTag;
                if (ne != null && !"O".equals(ne)) {
                    actNerTag = pipeline.getNerTag(ne);
                } else {
                    actNerTag = null;
                }
                if (nerTag != null && !nerTag.equals(actNerTag)) {
                    Chunk nerChunk = at.addChunk(nerStart.getStart(), nerEnd.getEnd());
                    nerChunk.addAnnotation(NlpAnnotations.NER_ANNOTATION, nerTag);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(" - add Named Entity {} | tag: {}", nerChunk.getSpan(), nerTag);
                    }
                    nerTag = null;
                    nerStart = null;
                    nerEnd = null;
                }
                if (actNerTag != null) {
                    if (nerStart == null) {
                        nerStart = t;
                    }
                    nerTag = actNerTag;
                    nerEnd = t;
                }
                //Process the Lemma
                String lemma = token.get(LemmaAnnotation.class);
                if (lemma != null && !lemma.equals(t.getSpan())) {
                    t.addAnnotation(NlpAnnotations.LEMMA_ANNOTATION, lemma);
                }

                //Dependency relation - Part 1 (consumes 'parse' annotator results)
                //NOTE: Root relations are only written after the Sentence was added to the AnalyzedText
                if (dependencies != null) {
                    addDependencyRelations(tokens, t, at, pipeline, dependencies, ++tokenIdxInSentence);
                }
            } //end iterate over tokens in sentence
            //add the Sentence
            Sentence sent = at.addSentence(sentStart.getStart(), sentEnd.getEnd());
            LOG.trace("-- {} {}", sent, sent.getSpan());
            //Dependency relation - Part 2
            if (dependencies != null) {
                //NOTE: Root relations are between the Sentence and the Root Token(s)
                Collection<IndexedWord> roots = dependencies.getRoots();
                RelTag rootRelTag = pipeline.getRelationTag("root");
                for (IndexedWord vertex : roots) {
                    Token root = at.addToken(vertex.beginPosition(), vertex.endPosition());
                    Relation rootRel = new Relation(rootRelTag, false, root);
                    sent.addAnnotation(NlpAnnotations.DEPENDENCY_ANNOTATION, rootRel);
                    LOG.debug(" - {}", rootRel);
                    if (writeDependent) {
                        root.addAnnotation(NlpAnnotations.DEPENDENCY_ANNOTATION, new Relation(rootRelTag, true, sent));
                    }
                }

            }
            //Parse Tree
            Tree tree = sentence.get(TreeAnnotation.class);
            if (tree != null) {
                Predicate<String> punctuationFilter = pipeline.getLanguagePack() != null ?
                        pipeline.getLanguagePack().punctuationTagRejectFilter() : Filters.acceptFilter();
                LOG.debug(" - process ParseTree");
                if (LOG.isDebugEnabled()) {
                    TreePrint tp = new TreePrint("oneline", pipeline.getLanguagePack());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    tp.printTree(tree, pw);
                    pw.flush();
                    LOG.debug(" - tree: {}", sw);
                }
                List<Tree> toProcess = new LinkedList<>();
                toProcess.add(tree);
                while (!toProcess.isEmpty()) {
                    tree = toProcess.remove(0);
                    toProcess.addAll(0, tree.getChildrenAsList());
                    if (tree.isPhrasal() && tree.label() instanceof CoreMap
                            && !"ROOT".equals(tree.value()) && punctuationFilter.test(tree.value())) {
                        CoreMap value = (CoreMap) tree.label();
                        value.get(CoreAnnotations.CategoryAnnotation.class);
                        String tag = tree.value();
                        PhraseTag phraseTag = pipeline.getPhraseTag(tag);
                        int beginTokenIdx = value.get(CoreAnnotations.BeginIndexAnnotation.class);
                        int endTokenIdx = value.get(CoreAnnotations.EndIndexAnnotation.class);
                        Chunk chunk = at.addChunk(tokens.get(beginTokenIdx).beginPosition(), tokens.get(endTokenIdx - 1).endPosition());
                        chunk.addAnnotation(NlpAnnotations.PHRASE_ANNOTATION, phraseTag);
                        LOG.debug("    add {} ({}) - {}", chunk, phraseTag.getTag(), chunk.getSpan());
                    }
                }
            }

            //Sentiment for the Sentence
            String sentimentClass = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            Tree sentimentTree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            if (sentimentTree != null) {
                //we can not use the class as we want a double value
                //int sentiment = RNNCoreAnnotations.getPredictedClass(sentimentTree);
                SimpleMatrix predictions = RNNCoreAnnotations.getPredictions(sentimentTree);
                int size = predictions.getNumElements();
                if (sentClassMapping == null) {
                    LOG.debug(" - {} sentiment classes detected", size);
                    sentClassMapping = new LinearSentimentClassMapping(size);
                }
                if (LOG.isDebugEnabled()) {
                    double[] values = new double[size];
                    for (int i = 0; i < size; i++) {
                        values[i] = predictions.get(i);
                    }
                    LOG.debug(" - sentiment: {}[classes: {}]", new Object[]{
                            sentimentClass, Arrays.toString(values)});
                }
                //sum up the predictions of the different classes
                double sentimentValue = 0;
                for (int idx = 0; idx < size; idx++) {
                    double idxSent = sentClassMapping.getIndexWeight(idx);
                    if (!Double.isNaN(idxSent)) {
                        sentimentValue += predictions.get(idx) * idxSent;
                    } else { //sentiment classes can not be converted to a number
                        sentimentValue = Double.NaN;
                        break;
                    }
                }
                //Annotating sentence with the calculated sentiment value
                if (!Double.isNaN(sentimentValue)) {
                    //TODO: provide a better sentiment annotation where we can
                    //      also parse information about the detected class
                    sent.addAnnotation(NlpAnnotations.SENTIMENT_ANNOTATION, sentimentValue);
                }
            }
            //clean up the sentence
            sentStart = null;
            sentEnd = null;
            //we might have still an open NER annotation
            if (nerTag != null) {
                Chunk nerChunk = at.addChunk(nerStart.getStart(), nerEnd.getEnd());
                nerChunk.addAnnotation(NlpAnnotations.NER_ANNOTATION, nerTag);
            }
        }

    }

    /**
     * Add dependency tree annotations to the current token.
     *
     * @param tokens         - the list of {@link CoreLabel}s in the current sentence.
     * @param currentToken   - the current {@link Token} to which the dependency relations will
     *                       be added.
     * @param at
     * @param relationTagSet - tag set containing {@link RelTag}s.
     * @param dependencies   - the {@link SemanticGraph} containing the dependency tree relations.
     */
    private void addDependencyRelations(List<CoreLabel> tokens, Token currentToken, AnalyzedText at,
                                        StanfordNlpPipeline pipeline, SemanticGraph dependencies, int currentTokenIdx) {
        LOG.debug(" - dependency relations");
        IndexedWord vertex = dependencies.getNodeByIndexSafe(currentTokenIdx);
        if (vertex == null) {
            // Usually the current token is a punctuation mark in this case.
            return;
        }

        List<SemanticGraphEdge> edges = new ArrayList<SemanticGraphEdge>();
        edges.addAll(dependencies.outgoingEdgeList(vertex));
        if (writeDependent) {
            edges.addAll(dependencies.incomingEdgeList(vertex));
        }

        for (SemanticGraphEdge edge : edges) {
            int govIndex = edge.getGovernor().index();
            int depIndex = edge.getDependent().index();
            GrammaticalRelation gramRel = edge.getRelation();
            RelTag relTag = pipeline.getRelationTag(gramRel.getShortName());
            if (relTag != null) {
                boolean isDependent = false;
                Span partner = null;

                if (govIndex == currentTokenIdx) {
                    CoreLabel dependentLabel = tokens.get(depIndex - 1);
                    partner = at.addToken(dependentLabel.beginPosition(), dependentLabel.endPosition());
                } else if (depIndex == currentTokenIdx) {
                    isDependent = true;
                    CoreLabel governorLabel = tokens.get(govIndex - 1);
                    partner = at.addToken(governorLabel.beginPosition(), governorLabel.endPosition());
                }
                if (writeDependent || !isDependent) {
                    Relation relation = new Relation(relTag, isDependent, partner);
                    LOG.debug(" - {}", relation);
                    currentToken.addAnnotation(NlpAnnotations.DEPENDENCY_ANNOTATION, relation);
                }
            } else {
                LOG.warn("Missing GrammaticalRelationTag for {}!", gramRel.getShortName());
            }
        }

    }
}
