/*******************************************************************************
 * Copyright (c) 2022 Redlink GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package io.redlink.nlp.model.util;

import io.redlink.nlp.api.Content;
import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Section;
import io.redlink.nlp.model.Sentence;
import io.redlink.nlp.model.Span;
import io.redlink.nlp.model.SpanCollection;
import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.morpho.MorphoFeatures;
import io.redlink.nlp.model.ner.NerSet;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.pos.LexicalCategory;
import io.redlink.nlp.model.pos.Pos;
import io.redlink.nlp.model.pos.PosSet;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.section.SectionStats;
import io.redlink.nlp.model.section.SectionTag;
import io.redlink.nlp.model.section.SectionType;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.disjoint;

/**
 * Helper for consuming {@link NlpAnnotations}
 *
 * @author Rupert Westentahler
 */
public final class NlpUtils {

    private final static Logger log = LoggerFactory.getLogger(NlpUtils.class);

    private static final Charset UTF8 = Charset.forName("utf-8");

    private static final Set<Character> HYPHEN_AND_UNDERLINE_AND_APOSTROPHE;

    static {
        Set<Character> chars = new HashSet<>();
        for (char c : "-–—_'´`˚".toCharArray()) {
            chars.add(Character.valueOf(c));
        }
        HYPHEN_AND_UNDERLINE_AND_APOSTROPHE = Collections.unmodifiableSet(chars);
    }

    private static final Pattern CHARSET_PATTERN = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");

    private NlpUtils() {
    }

    /**
     * Retrieves the {@link AnalyzedText} from the parsed ProcessingData
     *
     * @param pd the Processing data
     * @return the {@link AnalyzedText} if present
     * @see #getOrInitAnalyzedText(ProcessingData)
     */
    public static Optional<AnalyzedText> getAnalyzedText(ProcessingData pd) {
        return Optional.ofNullable(pd.getAnnotation(AnalyzedText.ANNOTATION));
    }

    /**
     * First tries to retrieve the {@link AnalyzedText} from the parsed PrecessingData
     * if not present it will create a new AnalyzedText instance based on the first
     * 'text/plain' content present in the {@link ProcessingData}. If no plain text
     * content is present it returns <code>null</code>
     *
     * @param pd the {@link ProcessingData}
     * @return the existing or initialized {@link AnalyzedText} for the parsed {@link ProcessingData}.
     * <code>null</code> if no AnalyzedText was present and it could not be created (e.g. because
     * no 'plain/text' content was present).
     */
    public static AnalyzedText getOrInitAnalyzedText(ProcessingData pd) {
        Optional<AnalyzedText> existingAt = getAnalyzedText(pd);
        if (existingAt.isPresent()) {
            return existingAt.get();
        } else {
            AnalyzedText at;
            Optional<Content> plainText = pd.getContent("text/plain");
            if (plainText.isPresent()) {
                Optional<CharSequence> text = plainText.get().getPlainContent();
                if (text.isPresent()) {
                    at = new AnalyzedText(text.get());
                } else {
                    try {
                        at = new AnalyzedText(IOUtils.toString(plainText.get().getContent(),
                                getCharset(plainText.get().getMimeType(), UTF8)));
                    } catch (IOException e) {
                        log.error("Unable to create AnalyzedText for 'plain/text' content {} ({} - {})", e.getClass().getSimpleName(), e.getMessage());
                        log.debug("STACKTRACE", e);
                        at = null;
                    }
                }
            } else {
                log.trace("Unable to init AnalyzedText for {} (reason: no 'text/plain' content present)", pd);
                at = null;
            }
            if (at != null) { //if we have created the AnalyzedText we need to add the annotation
                pd.addAnnotation(AnalyzedText.ANNOTATION, at);
            }
            return at;
        }
    }

    /**
     * Checks if the parsed token is a {@link LexicalCategory#Adjective adjective}
     *
     * @param token the token
     * @return the state
     * @see #isOfPos(Token, PosSet, float)
     */
    public static boolean isAdjective(Token token) {
        return isOfPos(token, PosSet.ADJECTIVES, -1);
    }


    /**
     * Checks if the parsed token is a {@link LexicalCategory#Verb verb}
     *
     * @param token the token
     * @return the state
     * @see #isOfPos(Token, PosSet, float)
     */
    public static boolean isVerb(Token token) {
        return isOfPos(token, PosSet.VERBS, -1);
    }

    /**
     * Checks if the parsed token is a {@link LexicalCategory#Noun noun},
     * {@link Pos#Foreign foreign} or {@link Pos#Abbreviation} word.
     *
     * @param token the token
     * @return the state
     * @see #isOfPos(Token, PosSet, float)
     */
    public static boolean isNoun(Token token) {
        return isOfPos(token, PosSet.NOUNS, -1);
    }

    /**
     * Returns the {@link LexicalCategory} with the highest probability by
     * summing up all {@link NlpAnnotations#POS_ANNOTATION POS annotations}
     * present for the parsed Token
     *
     * @param token the token
     * @return the pair {@link LexicalCategory}: confidence or <code>null</code> if
     * no POS tags are present or the {@link PosTag}s are not mapped (do not have
     * any assigned {@link PosTag#getCategories()}
     */
    public static Pair<LexicalCategory, Double> getCategory(Token token) {
        List<Value<PosTag>> posAnnotations = token.getValues(NlpAnnotations.POS_ANNOTATION);
        if (posAnnotations.isEmpty()) {
            return null;
        }
        EnumMap<LexicalCategory, double[]> catProb = new EnumMap<>(LexicalCategory.class);
        double maxProb = -1;
        double sumProb = 0;
        LexicalCategory maxLc = null;
        for (Value<PosTag> posAnnotation : posAnnotations) {
            PosTag posTag = posAnnotation.value();
            double prob = posAnnotation.probability() == Value.UNKNOWN_PROBABILITY ? 1d : posAnnotation.probability();
            sumProb += prob;
            for (LexicalCategory lc : posTag.getCategories()) {
                double[] cp = catProb.get(lc);
                double cProb;
                if (cp == null) {
                    cp = new double[]{prob};
                    cProb = prob;
                    catProb.put(lc, cp);
                } else {
                    cProb = cp[0] + prob;
                    cp[0] = cProb;
                }
                if (cProb > maxProb) {
                    maxProb = cProb;
                    maxLc = lc;
                }
            }
        }
        return maxLc == null ? null : new ImmutablePair<LexicalCategory, Double>(maxLc, Double.valueOf(maxProb / sumProb));
    }

    /**
     * Analysis (possible multiple) {@link NlpAnnotations#POS_ANNOTATION} for the
     * parsed {@link LexicalCategory lexical categories}, {@link Pos POS tags} and
     * string tags to determine of the Token can be classified as the union of those
     * categories. A classification requires that the sum of the probabilities of
     * matching {@link PosTag}s is is at least <code>1.5</code> times
     * the probability of all none matching.
     *
     * @param token  the token to check
     * @param posSet the set of allowed {@link LexicalCategory}, {@link PosTag} and
     *               string tags
     * @return <code>true</code> if the Token can be classified as the union of
     * the parsed categories, pos tags and string tags. <code>false</code> if
     * not or no {@link NlpAnnotations#POS_ANNOTATION} information are available.
     */
    public static boolean isOfPos(Token token, PosSet posSet) {
        return isOfPos(token, posSet, -1);
    }

    /**
     * Analysis (possible multiple) {@link NlpAnnotations#POS_ANNOTATION} for the
     * parsed {@link LexicalCategory lexical categories}, {@link Pos POS tags} and
     * string tags to determine of the Token can be classified as the union of those
     * categories. If a <code>minConf</code> is given it checks if the sum probability
     * of all matching POS annotations as greater as the parsed <code>minConf</code>
     * AND if the confidence of matching POS tags is greater as those of the not
     * matching one. if <code>minConf &lt;= 0 </code> is parsed it is checked if
     * the probability of all matching POS tags is at least <code>1.5</code> times
     * the probability of all none matching POS tags.
     *
     * @param token   the token to check
     * @param posSet  the set of allowed {@link LexicalCategory}, {@link PosTag} and
     *                string tags
     * @param minConf the minimum confidence or a value <code>&lt;= 0</code> to
     *                use the default checking if the probability of matching POS tags is
     *                at least <code>1.5</code> times those of none matching one.
     * @return <code>true</code> if the Token can be classified as the union of
     * the parsed categories, pos tags and string tags. <code>false</code> if
     * not or no {@link NlpAnnotations#POS_ANNOTATION} information are available.
     */
    public static boolean isOfPos(Token token, PosSet posSet, float minConf) {
        double[] probs = evalProbs(token, posSet);
        if (minConf > 0) {
            return probs[0] >= minConf && probs[0] > probs[1];
        } else {
            return probs[0] > probs[1] * 1.5;
        }
    }

    /**
     * Returns the normalized probability for the {@link Token} to be be
     * classified as the parsed {@link PosSet}
     *
     * @param token  the Token
     * @param posSet the POS set
     * @return the probability in the range <code>[0..1]</code>
     */
    public static double getProbability(Token token, PosSet posSet) {
        return evalProbs(token, posSet)[0];
    }

    /**
     * Calculates <code>[matching, notMatching]</code> probabilities of
     * {@link NlpAnnotations#POS_ANNOTATION}s for the parsed {@link Token}
     * against the parsed {@link PosSet}
     *
     * @param token  the token to evaluate
     * @param posSet the PosSet to match POS annotations against
     * @return the <code>[matching, notMatching]</code> or <code>[0d,1d]</code>
     * if no POS annotations where found.
     */
    private static double[] evalProbs(Token token, PosSet posSet) {
        List<Value<PosTag>> posAnnotations = token.getValues(NlpAnnotations.POS_ANNOTATION);
        if (posAnnotations.isEmpty()) {
            return new double[]{0d, 1d};
        }
        double maxMatchingProb = -1;
        double matchingProb = 0;
        double maxNotMatchingProb = -1;
        double notMatchingProb = 0;
        double sumProb = 0;
        for (Value<PosTag> posAnnotation : posAnnotations) {
            PosTag posTag = posAnnotation.value();
            double prob = posAnnotation.probability() == Value.UNKNOWN_PROBABILITY ? 1d : posAnnotation.probability();
            if ((!disjoint(posSet.getCategories(), posTag.getCategories())) ||
                    (!disjoint(posSet.getPosTags(), posTag.getPosHierarchy())) ||
                    posSet.getTags().contains(posTag.getTag())) {
                if (maxMatchingProb < prob) {
                    maxMatchingProb = prob;
                }
                matchingProb += prob;
            } else {
                if (maxNotMatchingProb < prob) {
                    maxNotMatchingProb = prob;
                }
                notMatchingProb += prob;
            } // else probability to low for exclusion
            sumProb += prob;
        }
        //normalize probabilities
        sumProb = Math.max(1, sumProb);
        matchingProb = matchingProb / sumProb;
        notMatchingProb = notMatchingProb / sumProb;
        return new double[]{matchingProb, notMatchingProb};
    }

    /**
     * Gets the leamma for the parsed token. First searched the {@link NlpAnnotations#MORPHO_ANNOTATION}
     * and than fall backs to {@link NlpAnnotations#LEMMA_ANNOTATION}.
     *
     * @param span
     * @return the lemma or <code>null</code> if none is defined
     */
    public static String getLemma(final Span span) {
        Value<MorphoFeatures> morpho = span.getValue(NlpAnnotations.MORPHO_ANNOTATION);
        if (morpho == null || morpho.value().getLemma() == null) {
            Value<String> lemma = span.getValue(NlpAnnotations.LEMMA_ANNOTATION);
            return lemma == null ? null : lemma.value();
        } else {
            return morpho.value().getLemma();
        }
    }

    /**
     * Gets the stem for the token. NOTE most Stemmer will not annotate a
     * stem if it is the same as the {@link Token#getSpan()}. So it is reasonable
     * to use the tokens span as a fallback if this method returns <code>null</code>
     *
     * @param span the token
     * @return the stem or <code>null</code> if no stem is present
     */
    public static String getStem(final Span span) {
        Value<String> stem = span.getValue(NlpAnnotations.STEM_ANNOTATION);
        return stem == null ? null : stem.value();
    }

    /**
     * This method returns the normalized token. It first ties to get the
     * {@link #getLemma(Token) lemma}. If none is define it will lookup the
     * {@link #getStem(Token) stem}. If also no stem is defined it will return
     * the {@link Token#getSpan() tokens span}
     *
     * @param span the token to normalize
     * @return the normalized token
     */
    public static String getNormalized(final Span span) {
        String cleaned = getLemma(span);
        if (cleaned == null) {
            cleaned = getStem(span);
        }
        return cleaned == null ? span.getSpan() : cleaned;
    }

    public static boolean isStopword(final Token token) {
        Value<Boolean> stopword = token.getValue(NlpAnnotations.STOPWORD_ANNOTATION);
        return stopword != null && Boolean.TRUE.equals(stopword.value());
    }

    /**
     * Checks is the {@link Span} has an alpha-numeric character.
     */
    public static boolean hasAlphaNumeric(final Span span) {
        if (span == null) {
            return false;
        } else {
            return hasAlphaNumeric(span.getSpan());
        }
    }

    /**
     * Checks is the token has an alpha-numeric character.
     */
    public static boolean hasAlphaNumeric(final String token) {
        if (token == null) {
            return false;
        }
        int sz = token.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isLetterOrDigit(token.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks is the {@link Span} has only alpha, numeric, whitespace, hyphen, apostrophe or underline  character.
     */
    public static boolean isAlphaNumeric(final Span span) {
        if (span == null) {
            return false;
        } else {
            return isAlphaNumeric(span.getSpan());
        }
    }

    /**
     * Checks is the token has only alpha, numeric, whitespace, hyphen, apostrophe or underline  character.
     */
    public static boolean isAlphaNumeric(final String token) {
        if (token == null) {
            return false;
        }
        int sz = token.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isLetterOrDigit(token.codePointAt(i)) &&
                    !Character.isWhitespace(token.codePointAt(i)) &&
                    !HYPHEN_AND_UNDERLINE_AND_APOSTROPHE.contains(token.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks is the {@link Span} has an alphabetic character.
     */
    public static boolean hasAlpha(final Span span) {
        if (span == null) {
            return false;
        } else {
            return hasAlpha(span.getSpan());
        }
    }

    /**
     * Checks is the token has an alphabetic character.
     */
    public static boolean hasAlpha(final String token) {
        if (token == null) {
            return false;
        }
        int sz = token.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isLetter(token.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks is the {@link Span} has only alphabetic, whitespace, hyphen, apostrophe or underline character.
     */
    public static boolean isAlpha(final Span span) {
        if (span == null) {
            return false;
        } else {
            return isAlpha(span.getSpan());
        }
    }

    /**
     * Checks is the token has only alphabetic, whitespace, hyphen, apostrophe or underline characters.
     * .
     */
    public static boolean isAlpha(final String token) {
        if (token == null) {
            return false;
        }
        int sz = token.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isLetter(token.codePointAt(i)) &&
                    !Character.isWhitespace(token.codePointAt(i)) &&
                    !HYPHEN_AND_UNDERLINE_AND_APOSTROPHE.contains(token.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks is the {@link Span} has an alpha-numeric character.
     */
    public static boolean hasNumeric(final Span span) {
        if (span == null) {
            return false;
        } else {
            return hasNumeric(span.getSpan());
        }
    }

    /**
     * Checks is the token has an alpha-numeric character.
     */
    public static boolean hasNumeric(final String token) {
        if (token == null) {
            return false;
        }
        int sz = token.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(token.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks is the {@link Span} has only numeric character.
     */
    public static boolean isNumeric(final Span span) {
        if (span == null) {
            return false;
        } else {
            return isNumeric(span.getSpan());
        }
    }

    /**
     * Checks is the token has only numeric character.
     */
    public static boolean isNumeric(final String token) {
        if (token == null) {
            return false;
        }
        int sz = token.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(token.codePointAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Analysis (possible multiple) {@link NlpAnnotations#NER_ANNOTATION} for the
     * parsed {@link NerSet}  to determine of the Chunk can be classified as the
     * union of those types and tags. A classification requires that the sum of
     * the probabilities of matching {@link NerTag}s is is at least <code>1.5</code> times
     * the probability of all none matching.
     *
     * @param chunk  the token to check
     * @param nerSet the set of allowed types and tags of named entities
     *               string tags
     * @return <code>true</code> if the Token can be classified as the parsed
     * {@link NerSet}. <code>false</code> if not or no {@link NlpAnnotations#NER_ANNOTATION}
     * information are available.
     */
    public static boolean isOfNer(Chunk chunk, NerSet nerSet) {
        return isOfNer(chunk, nerSet, -1);
    }

    /**
     * Analysis (possible multiple) {@link NlpAnnotations#NER_ANNOTATION} for the
     * parsed {@link NerSet}  to determine of the Chunk can be classified as the
     * union of those types and tags. If a <code>minConf</code> is given it
     * checks if the sum probability of all matching NER annotations as greater
     * as the parsed <code>minConf</code> AND if the confidence of matching NER
     * annotation  is greater as those of the not matching one. If
     * <code>minConf &lt;= 0 </code> is parsed it is checked if the probability
     * of all matching NER annotation is at least <code>1.5</code> times
     * the probability of all none matching NER annotation.
     *
     * @param chunk   the token to check
     * @param nerSet  the set of allowed types and tags of named entities
     *                string tags
     * @param minConf the minimum confidence or a value <code>&lt;= 0</code> to
     *                use the default checking if the probability of matching NER annotation is
     *                at least <code>1.5</code> times those of none matching one.
     * @return <code>true</code> if the Token can be classified as the {@link NerSet}.
     * <code>false</code> if not or no {@link NlpAnnotations#NER_ANNOTATION}
     * information are available.
     */
    public static boolean isOfNer(Chunk chunk, NerSet nerSet, float minConf) {
        double[] probs = evalProbs(chunk, nerSet);
        if (minConf > 0) {
            return probs[0] >= minConf && probs[0] > probs[1];
        } else {
            return probs[0] > probs[1] * 1.5;
        }
    }

    /**
     * Calculates <code>[matching, notMatching]</code> probabilities of
     * {@link NlpAnnotations#POS_ANNOTATION}s for the parsed {@link Token}
     * against the parsed {@link NerSet}
     *
     * @param chunk  the token to evaluate
     * @param nerSet the PosSet to match POS annotations against
     * @return the <code>[matching, notMatching]</code> or <code>[0d,1d]</code>
     * if no POS annotations where found.
     */
    private static double[] evalProbs(Chunk chunk, NerSet nerSet) {
        List<Value<NerTag>> nerAnnotations = chunk.getValues(NlpAnnotations.NER_ANNOTATION);
        if (nerAnnotations.isEmpty()) {
            return new double[]{0d, 1d};
        }
        double maxMatchingProb = -1;
        double matchingProb = 0;
        double maxNotMatchingProb = -1;
        double notMatchingProb = 0;
        double sumProb = 0;
        for (Value<NerTag> nerAnnotation : nerAnnotations) {
            NerTag nerTag = nerAnnotation.value();
            double prob = nerAnnotation.probability() == Value.UNKNOWN_PROBABILITY ? 1d : nerAnnotation.probability();
            if ((nerSet.getTypes().contains(nerTag.getType())) ||
                    nerSet.getTags().contains(nerTag.getTag())) {
                if (maxMatchingProb < prob) {
                    maxMatchingProb = prob;
                }
                matchingProb += prob;
            } else {
                if (maxNotMatchingProb < prob) {
                    maxNotMatchingProb = prob;
                }
                notMatchingProb += prob;
            } // else probability to low for exclusion
            sumProb += prob;
        }
        //normalize probabilities
        sumProb = Math.max(1, sumProb);
        matchingProb = matchingProb / sumProb;
        notMatchingProb = notMatchingProb / sumProb;
        return new double[]{matchingProb, notMatchingProb};
    }

    /**
     * Returns the normalized probability for the {@link Token} to be be
     * classified as the parsed {@link PosSet}
     *
     * @param chunk  the Chunk
     * @param nerSet the NER set
     * @return the probability in the range <code>[0..1]</code>
     */
    public static double getProbability(Chunk chunk, NerSet nerSet) {
        return evalProbs(chunk, nerSet)[0];
    }

    /**
     * Default <code>minChars</code> value used for {@link #isContentSection(SpanCollection)}
     */
    public static final int MIN_CONTNET_SECTION_CHARS = 120;
    /**
     * Default <code>minWords</code> value used for {@link #isContentSection(SpanCollection)}
     */
    public static final int MIN_CONTNET_SECTION_WORDS = MIN_CONTNET_SECTION_CHARS / 5;

    /**
     * Checks if the parsed {@link Section} should be considered as natural
     * language content. This will include all {@link SectionType#heading} and
     * use {@link #MIN_CONTNET_SECTION_CHARS} and {@link #MIN_CONTNET_SECTION_WORDS}
     * as parameter for {@link #isContentSection(SpanCollection, boolean, int, int)}
     *
     * @param section the section to check
     * @return if the parsed section contains natural language content
     * @see #isContentSection(SpanCollection, boolean, int, int)
     */
    public static boolean isContentSection(SpanCollection section) {
        return isContentSection(section, true, MIN_CONTNET_SECTION_WORDS, MIN_CONTNET_SECTION_CHARS);
    }

    /**
     * Checks if the parsed {@link Section} should be considered as natural
     * language content.
     *
     * @param section         the section to check. This works with all {@link SpanCollection}
     *                        instance ({@link Sentence}s, {@link Section}s as well as {@link AnalyzedText}).
     *                        It uses {@link NlpAnnotations#SECTION_ANNOTATION} and
     *                        {@link NlpAnnotations#SECTION_STATS_ANNOTATION} if present. If not present it
     *                        will count tokens and chars of the parsed section itself.
     * @param includeHeadings if all {@link SectionType#heading}s should be considered as content
     * @param minWords        the minimum number of words so that a section is considered content
     * @param minChars        the minimum number of chars so that a section is considered content
     * @return if the parsed section contains natural language content
     */
    public static boolean isContentSection(SpanCollection section, boolean includeHeadings, int minWords, int minChars) {
        Value<SectionTag> sectionTag = section.getValue(NlpAnnotations.SECTION_ANNOTATION);
        if (sectionTag != null) {
            SectionType sectionType = sectionTag.value().getSection();
            if (!sectionType.isContentSection()) {
                return false; //this section cotains sub-section and no content itself
            }
            if (sectionType == SectionType.heading && includeHeadings) {
                return true;
            }
        }
        Value<SectionStats> sectionStats = section.getValue(NlpAnnotations.SECTION_STATS_ANNOTATION);
        if (sectionStats != null) {
            //TODO: improve sectionStats annotation to include word level statistics
            return sectionStats.value().getNumAlpha() >= minChars &&
                    sectionStats.value().getFractionWhitespace() < 0.3f;
        } else {
            int tokenCount = 0;
            int charCount = 0;
            Iterator<Token> tokens = section.getTokens();
            while (tokens.hasNext() && charCount < minChars && tokenCount < minWords) {
                Token token = tokens.next();
                tokenCount++;
                charCount += (token.getEnd() - token.getStart());
            }
            return charCount >= minChars && tokenCount >= minWords;
        }
    }

    /**
     * Filters {@link Span}s in the parsed Iterator that do have an
     * {@link NlpAnnotations#NER_ANNOTATION}
     *
     * @param spans iterator over Spans
     * @return Spans with a named entity annotation
     */
    public static <T extends Span> Iterator<T> getNamedEntities(Iterator<T> spans) {
        return IteratorUtils.filteredIterator(spans, span -> span.getValue(NlpAnnotations.NER_ANNOTATION) != null);
    }

    /**
     * Uses {@link NlpAnnotations#TRUE_CASE_ANNOTATION} to generate the
     * case corrected version of the parsed {@link Span#getSpan()}
     *
     * @param span the span to case correct
     * @return the case corrected span
     */
    public static String toTrueCase(Span span) {
        return toTrueCase(span, false);
    }

    public static String toTrueCase(Span span, boolean toUpperCaseOnly) {
        if (span instanceof SpanCollection) {
            Iterator<Token> tokens = ((SpanCollection) span).getTokens();
            StringBuilder trueCaseBuilder = new StringBuilder(span.getEnd() - span.getStart());
            CharSequence text = span.getContext().getText();
            int idx = span.getStart();
            while (tokens.hasNext()) {
                Token token = tokens.next();
                Value<String> trueCaseValue = token.getValue(NlpAnnotations.TRUE_CASE_ANNOTATION);
                if (trueCaseValue != null) {
                    String trueCase = trueCaseValue.value();
                    assert trueCase.length() == token.getSpan().length();
                    if (!trueCase.equals(token.getSpan()) &&
                            trueCase.length() == token.getSpan().length() &&
                            (toUpperCaseOnly || (trueCase.length() > 0 && Character.isUpperCase(trueCase.charAt(0))))) {
                        //append the text in-between the last changed token
                        if (idx < token.getStart()) {
                            trueCaseBuilder.append(text.subSequence(idx, token.getStart()));
                        }
                        trueCaseBuilder.append(trueCase); //append the changed token
                        idx = token.getEnd(); //update the index
                    } // same as token or not valid ... keep original text
                }
            }
            if (idx < 0) { //no tokens nor true case information ... return the span
                return span.getSpan();
            } else if (idx < span.getEnd()) { //append remaining unchanged text
                trueCaseBuilder.append(text.subSequence(idx, span.getEnd()));
            }
            String trueCaseSpan = trueCaseBuilder.toString();
            assert trueCaseSpan.length() == span.getSpan().length();
            return trueCaseSpan;
        } else {
            Value<String> trueCaseValue = span.getValue(NlpAnnotations.TRUE_CASE_ANNOTATION);
            return trueCaseValue == null ? span.getSpan() : trueCaseValue.value();
        }

    }

    /**
     * Parse out a charset from a mimeType.
     *
     * @param contentType e.g. "text/plain; charset=utf-8"
     * @param def         the default value if not present
     * @return the parsed or default if not found in the parsed mimeType
     * @throws UnsupportedCharsetException if the parsed charset is not supported
     */
    private static Charset getCharset(String mimeType, Charset def) {
        if (mimeType == null) {
            return def;
        }
        Matcher m = CHARSET_PATTERN.matcher(mimeType);
        if (m.find()) {
            String charset = m.group(1).trim().toUpperCase();
            if (charset != null) {
                try {
                    return Charset.forName(charset);
                } catch (IllegalCharsetNameException e) {
                    return def;
                }
            } else {
                return def;
            }
        } else {
            return def;
        }
    }

}
