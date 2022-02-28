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

package io.redlink.nlp.model;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.ProcessingData.Configuration;
import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.annotation.Keyword;
import io.redlink.nlp.api.model.Annotation;
import io.redlink.nlp.model.coref.CorefFeature;
import io.redlink.nlp.model.dep.Relation;
import io.redlink.nlp.model.entitylinking.LinkedEntity;
import io.redlink.nlp.model.morpho.MorphoFeatures;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.phrase.PhraseTag;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.section.SectionStats;
import io.redlink.nlp.model.section.SectionTag;
import io.redlink.nlp.model.section.SectionType;
import io.redlink.nlp.model.temporal.DateTimeValue;
import java.util.Date;

/**
 * Defines the {@link Annotation} constants typically used by NLP components
 */
public final class NlpAnnotations {

    /**
     * The Section annotation is used to annotate the {@link SectionType} of a
     * {@link Section} in the text.
     */
    public static final Annotation<SectionTag> SECTION_ANNOTATION = new Annotation<SectionTag>(
            "stanbol_enhancer_nlp_section", SectionTag.class);
    /**
     * Allows to attach statistics about a Section.
     */
    public static final Annotation<SectionStats> SECTION_STATS_ANNOTATION = new Annotation<SectionStats>(
            "stanbol_enhancer_nlp_section_stats", SectionStats.class);

    /**
     * Allows to mark a section as containing Content.
     */
    public static final Annotation<Boolean> SECTION_CLASSIFICATION_CONTENT_SECTION = new Annotation<Boolean>(
            "stanbol_enhancer_nlp_classification_content_section", Boolean.class);

    /**
     * The POS {@link Annotation} added by POS taggers to {@link Token}s of an
     * {@link AnalyzedText}.
     */
    public static final Annotation<PosTag> POS_ANNOTATION = new Annotation<PosTag>(
            "stanbol_enhancer_nlp_pos", PosTag.class);
    /**
     *
     */
    public static final Annotation<NerTag> NER_ANNOTATION = new Annotation<NerTag>(
            "stanbol_enhancer_nlp_ner", NerTag.class);

    /**
     * The Phrase {@link Annotation} added by chunker to a group of [1..*]
     * {@link Token}s.
     * <p>
     * This annotation is typically found on {@link Chunk}s.
     */
    public static final Annotation<PhraseTag> PHRASE_ANNOTATION = new Annotation<PhraseTag>(
            "stanbol_enhancer_nlp_phrase", PhraseTag.class);

    /**
     * The Sentiment {@link Annotation} added by a sentiment tagger typically to
     * single {@link Token}s that do carry a positive or negative sentiment.
     */
    public static final Annotation<Double> SENTIMENT_ANNOTATION = new Annotation<Double>(
            "stanbol_enhancer_nlp_sentiment", Double.class);
    /**
     * {@link Annotation} representing the Morphological analysis of a word.
     * Typically used on {@link Token}s.
     * <p>
     * The {@link MorphoFeatures} defines at least the Lemma and [1..*] POS
     * tags. NOTE that the POS tag information does not assign a Tag to the
     * {@link Token}, but rather specifies that if the Token is classified by a
     * {@link #POS_ANNOTATION} to be of one of the Tags the definitions of this
     * {@link MorphoFeatures} can be applied.
     */
    public static final Annotation<MorphoFeatures> MORPHO_ANNOTATION = new Annotation<MorphoFeatures>(
            "stanbol_enhancer_nlp_morpho", MorphoFeatures.class);

    /**
     * {@link Annotation} representing the grammatical relations a word has with
     * other words in the sentence. Typically used on {@link Token}s.
     */
    public static final Annotation<Relation> DEPENDENCY_ANNOTATION = new Annotation<Relation>(
            "stanbol_enhancer_nlp_dependency", Relation.class);

    /**
     * {@link Annotation} representing all the words which are a
     * mention/reference of a given word. Typically used on {@link Token}s.
     */
    public static final Annotation<CorefFeature> COREF_ANNOTATION = new Annotation<CorefFeature>(
            "stanbol_enhancer_nlp_coref", CorefFeature.class);

    /**
     * {@link Annotation} that marks a {@link Token} as a stop word
     */
    public static final Annotation<Boolean> STOPWORD_ANNOTATION = new Annotation<>(
            "stanbol_enhancer_nlp_stopword", Boolean.class);

    /**
     * {@link Annotation} that provides the stem for a {@link Token}
     */
    public static final Annotation<String> STEM_ANNOTATION = new Annotation<>(
            "stanbol_enhancer_nlp_stem", String.class);

    /**
     * Simple Annotation to store the lemma. NOTE the lemma can also be
     * annotated by using the {@link #MORPHO_ANNOTATION}. In that case it
     * is obtainable by calling {@link MorphoFeatures#getLemma()}
     * {@link Annotation} that provides the lemma for a {@link Token}
     */
    public static final Annotation<String> LEMMA_ANNOTATION = new Annotation<>(
            "stanbol_enhancer_nlp_lemma", String.class);

    /**
     * The case corrected version of the token
     */
    public static final Annotation<String> TRUE_CASE_ANNOTATION = new Annotation<>(
            "stanbol_enhancer_nlp_truecase", String.class);

    /**
     * Keywords extracted from the text including some information on their ranking
     */
    public final static Annotation<Keyword> KEYWORD_ANNOTATION = new Annotation<>(
            "stanbol_enhancer_nlp_keyword", Keyword.class);

    /**
     * Used to Annotate that the marked {@link Span} (typically a {@link Token} or
     * a {@link Chunk}) is negated
     */
    public final static Annotation<Boolean> NEGATION_ANNOTATION = new Annotation<>(
            "stanbol_enhancer_nlp_negation", Boolean.class);

    /**
     * Used for fine grained Language Annotations of {@link Span}s (typically a {@link Section} or
     * the whole {@link AnalyzedText}). NOTE: That the language detected for the Document as a
     * whole is expected to be annotated using {@link Annotations#LANGUAGE} on the
     * {@link ProcessingData}.
     *
     * @see Annotations#LANGUAGE
     */
    public final static Annotation<String> LANGUAGE_ANNOTATION = new Annotation<>(
            "stanbol_enhancer_nlp_language", String.class);

    /*
     * Temporal Annotations:
     *   1. the Configuration#TEMPORAL_CONTEXT set as ProcessingData#getConfiguration(). This
     *      provides the context date for parsing the document as a whole.
     *   2. the TEMPORAL_CONTEXT annotation overriding the Configuration#TEMPORAL_CONTEXT. It
     *      also allows to set different context for Sections of an AnalyzedText
     *      Input for temporal parsing. Hence this uses a simple Date value
     *   2. the DateTimeValue(s) extracted from the content (usually on a Chunk).
     *      This annotation uses DateTimeValue allowing to represent both
     *      instants and intervals with a Date and a Grain.
     */

    /**
     * Used to provide the temporal context of a {@link AnalyzedText} or single {@link Section}s.
     * This annotation if present for the {@link AnalyzedText} will override the {@link Configuration#TEMPORAL_CONTEXT}
     * property on the {@link ProcessingData} but its main intension is for allowing to define different
     * temporal context on {@link Section} level. <p>
     * <b>IMPORTANT:</b> Do <b>NOT</b> use this annotation for date/time values extracted from the Text.
     *
     * @see Configuration#TEMPORAL_CONTEXT
     * @see NlpAnnotations#TEMPORAL_ANNOTATION
     */
    public final static Annotation<Date> TEMPORAL_CONTEXT = new Annotation<>(
            "stanbol_enhancer_nlp_context_temporal", Date.class);

    /**
     * Used to annotation {@link Chunk}s with the instant or intervals they refer.
     */
    public final static Annotation<DateTimeValue> TEMPORAL_ANNOTATION = new Annotation<>(
            "stanbol_enhancer_nlp_temporal", DateTimeValue.class);

    /**
     * Used to annotation {@link Chunk}s with the linked entity as value.
     */
    public final static Annotation<LinkedEntity> LINKED_ENTITY_ANNOTATION = new Annotation<>(
            "stanbol_enhancer_nlp_linkedentity", LinkedEntity.class);
    /*
     * Currently only used as part of MorphoFeatures
     */
    // Annotation<CaseTag> CASE_ANNOTATION = new Annotation<CaseTag>(
    // "stanbol_enhancer_nlp_morpho_case",CaseTag.class);
    //
    // Annotation<GenderTag> GENDER_ANNOTATION = new Annotation<GenderTag>(
    // "stanbol_enhancer_nlp_morpho_gender",GenderTag.class);
    //
    // Annotation<NumberTag> NUMBER_ANNOTATION = new Annotation<NumberTag>(
    // "stanbol_enhancer_nlp_morpho_number",NumberTag.class);
    //
    // Annotation<PersonTag> PERSON_ANNOTATION = new Annotation<PersonTag>(
    // "stanbol_enhancer_nlp_morpho_person",PersonTag.class);
    //
    // Annotation<TenseTag> TENSE_ANNOTATION = new Annotation<TenseTag>(
    // "stanbol_enhancer_nlp_morpho_tense",TenseTag.class);
    //
    // Annotation<VerbMoodTag> VERB_MOOD_ANNOTATION = new
    // Annotation<VerbMoodTag>(
    // "stanbol_enhancer_nlp_morpho_verb-mood",VerbMoodTag.class);

    private NlpAnnotations() {/*static only*/}
}