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

import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.tag.TagSet;
import io.redlink.nlp.opennlp.pos.impl.RegexSentenceSplitter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PreDestroy;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Sequence;
import opennlp.tools.util.Span;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core implementation of a language model for OpenNLP. This is to prevent code
 * duplication in language specific models.
 *
 * @author rupert.westenthaler@redlink.co
 */
public abstract class OpenNlpLanguageModel implements Comparable<OpenNlpLanguageModel> {

    private final Logger log = LoggerFactory.getLogger(OpenNlpLanguageModel.class);

    private final static Set<Integer> ADD_TOKEN_CODE_POINTS = new HashSet<>(
            Arrays.asList("\"".codePointAt(0), "'".codePointAt(0), "`".codePointAt(0),
                    "´".codePointAt(0), "„".codePointAt(0), "“".codePointAt(0),
                    "”".codePointAt(0), "’".codePointAt(0), "‚".codePointAt(0),
                    "«".codePointAt(0), "»".codePointAt(0), "‹".codePointAt(0)
                    , "›".codePointAt(0)));

    private static final double DEFAULT_MIN_AVRG_POS_SCORE = 0.667;

    private static final Pattern PARAGRAPH_SPLIT = Pattern.compile("\\n\\s*\\n");

    private boolean activated = false;
    private SentenceModel splitterModel = null;
    private POSModel taggerModel = null;
    private TokenizerModel tokenModel = null;

    private ThreadLocal<SentenceDetector> splitter;
    private ThreadLocal<Tokenizer> tokenizer;
    private ThreadLocal<POSTaggerME> tagger;

    private boolean caseSensitive = true;

    private final Locale language;

    private double minAvrgPosScore = DEFAULT_MIN_AVRG_POS_SCORE;

    private final TagSet<PosTag> tagset;
    /**
     * Stores {@link TagSet} for string tags returned by the {@link #taggerModel}
     * but but mapped in the {@link #tagset}.
     */
    private final Map<String, PosTag> adhocTags = new HashMap<>();

    private final String sentModelResource;
    private final String tokenModelResource;
    private final String posModelResource;

    /**
     * Creates a new OpenNLP Language Model for the parsed language and POS tag set
     *
     * @param language           the language
     * @param tagset             the POS tag set
     * @param sentModelResource  Required sentence model resource (loaded via classpath)
     * @param tokenModelResource optional tokenizer model resource (loaded via classpath).
     *                           If <code>null</code> the {@link SimpleTokenizer} will be used.
     * @param posModelResource   Required POS tagging model resource (loaded via classpath)
     */
    protected OpenNlpLanguageModel(Locale language, TagSet<PosTag> tagset,
                                   String sentModelResource, String tokenModelResource,
                                   String posModelResource) {
        this.language = language;
        assert tagset != null || posModelResource == null;
        this.tagset = tagset;
        ClassLoader cl = getClass().getClassLoader();
        assert sentModelResource == null || cl.getResource(sentModelResource) != null;
        this.sentModelResource = sentModelResource;
        assert tokenModelResource == null || cl.getResourceAsStream(tokenModelResource) != null;
        this.tokenModelResource = tokenModelResource;
        assert posModelResource == null || cl.getResource(posModelResource) != null;
        this.posModelResource = posModelResource;
    }

    /**
     * If the POS tagger model is case sensitive of not
     *
     * @param caseSensitive the case sensitive state
     */
    protected void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * If the Language model is case sensitive
     *
     * @return the case sensitive state
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Models with a higher ranking will get preference over others. Only the
     * model with the highest ranking will be considered for a given language.
     * Models with a ranking &lt; 0 are considered fallback models and only
     * be used if the document does not have any tokens (<code>
     * doc.getAnalyzedText().getTokens().hasNext() == false)</code>.
     *
     * @return This base implementation returns <code>0</code>.
     */
    public int getModelRanking() {
        return 0;
    }

    public final String getName() {
        return (language == null ? "Default" : language.getDisplayLanguage(Locale.ENGLISH))
                + " Language Model (OpenNLP" + (getModelRanking() < 0 ? ", fallback" : "") + ")";
    }

    /**
     * Initializes the OpenNLP language model based on the parsed parameters
     *
     * @throws LanguageModelException if the initialization fails
     */
    protected final void activate() throws IOException {
        if (!activated) {
            synchronized (this) {
                if (!activated) {
                    log.info("> activating {}", getName());
                    ClassLoader cl = getClass().getClassLoader();
                    log.info("  ... loading Sentence Splitter Model");
                    if (sentModelResource != null) {
                        splitterModel = new SentenceModel(cl.getResourceAsStream(sentModelResource));
                    } else {
                        splitterModel = null;
                    }
                    splitter = new ThreadLocal<SentenceDetector>() {
                        @Override
                        protected SentenceDetector initialValue() {
                            return splitterModel == null ? RegexSentenceSplitter.getInstance() :
                                    new SentenceDetectorME(splitterModel);
                        }
                    };
                    log.info("  ... loading Tokenizer Model");
                    if (tokenModelResource != null) {
                        tokenModel = new TokenizerModel(cl.getResourceAsStream(tokenModelResource));
                    } else {
                        tokenModel = null;
                    }
                    tokenizer = new ThreadLocal<Tokenizer>() {
                        @Override
                        protected Tokenizer initialValue() {
                            return tokenModel == null ? SimpleTokenizer.INSTANCE :
                                    new TokenizerME(tokenModel);
                        }
                    };
                    if (posModelResource != null) {
                        log.info("  ... loading PoS Tagger Model");
                        taggerModel = new POSModel(cl.getResourceAsStream(posModelResource));
                        tagger = new ThreadLocal<POSTaggerME>() {
                            @Override
                            protected POSTaggerME initialValue() {
                                return new POSTaggerME(taggerModel);
                            }
                        };
                        log.info("> inspect supported POS tags:");
                        POSTaggerME tagger = new POSTaggerME(taggerModel);
                        for (String tag : tagger.getAllPosTags()) {
                            PosTag posTag = tagset.getTag(tag);
                            if (posTag == null) {
                                log.warn(" - unmapped Tag {}", tag);
                            } else {
                                log.debug(" - mapped Tag {}", posTag);
                            }
                        }
                    } else {
                        log.info("  ... no PoS Tagger Model present");
                    }
                    activated = true;
                }
            }
        }

    }

    @PreDestroy //TODO: remove this when we have proper deactivation in Preprocessors
    public void deactivate() {
        synchronized (this) {
            log.info("> deactivate {}", getName());
            activated = false;
            splitterModel = null;
            splitter = null;
            tokenModel = null;
            tokenizer = null;
            tagger = null;
            tagger = null;
        }
    }

    /**
     * Allows to set the minimum average POS score for a Sentence to be
     * expected to be well formed. If the average POS score is lower as this the
     * {@link #tag(String[])} method will return <code>null</code>
     *
     * @param minAvrgPosScore
     */
    protected void setMinAvrgPosScore(double minAvrgPosScore) {
        this.minAvrgPosScore = minAvrgPosScore;
    }

    /**
     * Getter for the minumum average POS score for a Sentence to be
     * expected to be well formed. If the average POS score is lower as this the
     * {@link #tag(String[])} method will return <code>null</code>
     */
    public double getMinAvrgPosScore() {
        return minAvrgPosScore;
    }

    public Locale getLocale() {
        return language;
    }

    /**
     * Checks if the parsed language is supported based on the Locale parsed
     * in the constructor.
     *
     * @param lang the language to check
     */
    public boolean supports(String lang) {
        if (lang == null) {
            return false;
        } else {
            return Locale.forLanguageTag(lang).getLanguage().equals(language.getLanguage());
        }
    }

    /**
     * Split sentences within the paragraph text.
     */
    public final Span[] split(String text) {
        text = preprocessText(text);
        //double line breaks do indicate paragraphs.
        Matcher m = PARAGRAPH_SPLIT.matcher(text);
        List<Span> sentences = new LinkedList<>();
        int index = 0;
        while (m.find()) {
            if (index < m.start()) {
                String paragraph = text.substring(index, m.start());
                if (log.isDebugEnabled()) {
                    log.debug("> process paragrpah: [{},{}] {}", index, m.start(), StringUtils.abbreviate(paragraph, 40));
                }
                splitParagrpah(index, m.start(), paragraph, sentences);
            } //else ignore empty paragraph
            index = m.end();
        }
        if (index < text.length()) {
            splitParagrpah(index, text.length(), text.substring(index), sentences);
        }
        return sentences.toArray(new Span[sentences.size()]);
    }

    /**
     * This removed '<code>\r</code>', line breaks within words '<code>-\n</code>'
     * and also tries to improve sentence detection by replacing all
     * <code>2+</code> linebreaks with <code>.\n</code>.
     *
     * @param text the text (typically as parsed ot {@link #split(String)}
     * @return the processed text
     */
    protected String preprocessText(String text) {
        //remove all '\r'
        text = StringUtils.replace(text, StringUtils.CR, "");
        //remove linebreaks within words
        text = StringUtils.replace(text, "-\n", "");
        return text;
    }

    /**
     * Splits paragraphs to sentences.
     *
     * @param text
     * @param sentList
     */
    private void splitParagrpah(int start, int end, String text, List<Span> sentList) {
        Span[] sentPos = splitter.get().sentPosDetect(text);
        if (sentPos.length > 1) {
            Span prevSent = sentPos[0];
            for (int i = 1; i < sentPos.length; i++) {
                Span sent = sentPos[i];
                int cp = text.codePointAt(sent.getStart());
                //we merge sentences if the next one starts with a lower case letter
                if (Character.isLetter(cp) && Character.isLowerCase(cp)) {
                    if (log.isDebugEnabled()) {
                        log.debug("> merge Sentences: ");
                        log.debug("  1. {}", prevSent.getCoveredText(text));
                        log.debug("  2. {}", sent.getCoveredText(text));
                    }
                    prevSent = new Span(prevSent.getStart(), sent.getEnd());
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("  - add sentence: {}", prevSent.getCoveredText(text));
                    }
                    sentList.add(new Span(start + prevSent.getStart(), start + prevSent.getEnd(), prevSent.getProb()));
                    prevSent = sent;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("  - add sentence: {}", prevSent.getCoveredText(text));
            }
            sentList.add(new Span(start + prevSent.getStart(), start + prevSent.getEnd(), prevSent.getProb()));
        } else if (sentPos.length > 0) {
            log.debug("  - single sentence paragrpah ...");
            sentList.add(new Span(start, end));
        } //else no sentence to add
    }

    /**
     * Tokenize the sentence text into an array of tokens.
     */
    public final Span[] tokenize(final String text) {
        final Span[] tokenSpans = tokenizer.get().tokenizePos(text);
        return correctTokens(text, tokenSpans);
    }

    /**
     * This corrects tokens of the German NLP tokenizer by
     *
     * @param text
     * @param tokenSpans
     */
    private Span[] correctTokens(final String text, final Span[] tokenSpans) {
        final List<Span> tokens = new ArrayList<>(tokenSpans.length + 10);
        for (int sIdx = 0; sIdx < tokenSpans.length; sIdx++) {
            Span span = tokenSpans[sIdx];
            int start = span.getStart();
            int end = span.getEnd();
            if (start >= end) { //empty token
                //nothing to do
            } else if (start + 1 == end) { //single char tokens do not need to be processed
                tokens.add(span);
            } else {
                int codepoint = text.codePointAt(span.getStart());
                if (ADD_TOKEN_CODE_POINTS.contains(codepoint)) {
                    //split token
                    tokens.add(new Span(start++, start, span.getProb()));
                }
                codepoint = text.codePointBefore(end);
                Span endToken = null;
                if (ADD_TOKEN_CODE_POINTS.contains(codepoint)) {
                    endToken = new Span(end - 1, end, span.getProb());
                    end--;
                }
                if (start < end) {
                    tokens.add(new Span(start, end, span.getProb()));
                }
                if (endToken != null) {
                    tokens.add(endToken);
                }
            }
        }
        return tokens.toArray(new Span[tokens.size()]);
    }

    /**
     * Run a part-of-speech tagger on the sentence token list.
     */
    public final List<Value<PosTag>>[] tag(final String[] sentTokens) {
        if (taggerModel == null) { //no PoS tagger present
            return null;
        }
        //get the topK POS tags and props and copy it over to the 2dim Arrays
        Sequence[] posSequences = tagger.get().topKSequences(sentTokens);
        //extract the POS tags and props for the current token from the
        //posSequences.
        //NOTE: Sequence includes always POS tags for all Tokens. If
        //      less then posSequences.length are available it adds the
        //      best match for all followings.
        //      We do not want such copies.
        @SuppressWarnings("unchecked")
        List<Value<PosTag>>[] posValues = new List[sentTokens.length];
        PosTag[] actPos = new PosTag[posSequences.length];
        double[] actProp = new double[posSequences.length];
        for (int i = 0; i < sentTokens.length; i++) {
            boolean done = false;
            int j = 0;
            while (j < posSequences.length && !done) {
                String p = posSequences[j].getOutcomes().get(i);
                done = j > 0 && p.equals(actPos[0].getTag());
                if (!done) {
                    actPos[j] = getPosTag(p);
                    actProp[j] = posSequences[j].getProbs()[i];
                    j++;
                }
            }
            //create the POS values
            posValues[i] = Value.values(actPos, actProp, j);
        }
        return posValues;
    }

    /**
     * Uses the {@link #tagset} and {@link #adhocTags} to return existing instances
     * of {@link PosTag}s. If not present it will create a new one and add it to
     * {@link #adhocTags}
     *
     * @param tag the String pos tag as returned by the {@link #tagger}
     * @return the {@link PosTag} guaranteed to be not <code>null</code>
     */
    private PosTag getPosTag(String tag) {
        PosTag posTag = tagset == null ? null : tagset.getTag(tag);
        if (posTag != null) {
            return posTag;
        }
        posTag = adhocTags.get(tag);
        if (posTag != null) {
            return posTag;
        }
        posTag = new PosTag(tag);
        adhocTags.put(tag, posTag);
        log.info("Encountered umapped POS tag '{}' for langauge '{}'", tag, language.getLanguage());
        return posTag;
    }

    /**
     * Getter for the Sentence Splitter used by this OpenNLP language model
     *
     * @return the Sentence Splitter (one instance per thread)
     */
    public SentenceDetector getSplitter() {
        return splitter.get();
    }

    /**
     * Getter for the Tokenizer used by this OpenNLP language model
     *
     * @return the Tokenizer (one instance per thread)
     */
    public Tokenizer getTokenizer() {
        return tokenizer.get();
    }

    /**
     * Getter for the POS tagger used by this OpenNLP language model
     *
     * @return the Pos tagger (one instance per thread)
     */
    public POSTaggerME getTagger() {
        return tagger.get();
    }

    /**
     * Compares {@link OpenNlpLanguageModel} based on their
     * {@link #getModelRanking()}. The model with the highest ranking goes first.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(OpenNlpLanguageModel o) {
        return Integer.compare(o.getModelRanking(), getModelRanking());
    }
}
