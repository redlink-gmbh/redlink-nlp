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

package io.redlink.nlp.stanza;

import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.pos.LexicalCategory;
import io.redlink.nlp.model.pos.Pos;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.tag.TagSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class StanzaConstants {

    private StanzaConstants() {
        throw new IllegalStateException("Do not use reflection to create instances of Util classes :(");
    }

    /**
     * {@link TagSet} for the <a href="https://universaldependencies.org/u/pos/">
     * Universal POS tags</a>
     */
    public static final TagSet<PosTag> U_POS = new TagSet<PosTag>(
            "Universal POS tags");

    static {
        U_POS.getProperties().put("olia.annotationModel",
                "https://www.w3.org/2012/pyRdfa/extract?uri=http://fginter.github.io/docs/u/pos/all.html&format=turtle&rdfagraph=output&vocab_expansion=false&rdfa_lite=false&embedded_rdf=true&space_preserve=false&vocab_cache=true&vocab_cache_report=false&vocab_cache_refresh=false");
        U_POS.getProperties().put("olia.linkingModel",
                "http://www.acoli.informatik.uni-frankfurt.de/resources/olia/ud-pos-link.rdf");

        U_POS.addTag(new PosTag("ADJ", LexicalCategory.Adjective));
        U_POS.addTag(new PosTag("ADP", LexicalCategory.Adposition));
        U_POS.addTag(new PosTag("ADV", LexicalCategory.Adverb));
        U_POS.addTag(new PosTag("AUX", Pos.AuxiliaryVerb));
        U_POS.addTag(new PosTag("CONJ", Pos.CoordinatingConjunction));
        U_POS.addTag(new PosTag("DET", Pos.Determiner));
        U_POS.addTag(new PosTag("INTJ", Pos.Interjection));
        U_POS.addTag(new PosTag("NOUN", Pos.CommonNoun));
        U_POS.addTag(new PosTag("NUM", LexicalCategory.Quantifier));
        U_POS.addTag(new PosTag("PART", Pos.Particle));
        U_POS.addTag(new PosTag("PRON", Pos.Pronoun));
        U_POS.addTag(new PosTag("PROPN", Pos.ProperNoun));
        U_POS.addTag(new PosTag("PUNCT", LexicalCategory.Punctuation));
        U_POS.addTag(new PosTag("SCONJ", Pos.SubordinatingConjunction));
        U_POS.addTag(new PosTag("SYM", Pos.Symbol));
        U_POS.addTag(new PosTag("VERB", LexicalCategory.Verb));
        U_POS.addTag(new PosTag("X")); //other stuff
    }

    public static final TagSet<PosTag> ENGLISH = new TagSet<PosTag>(
            "English Penn Treebank tagset", "en");

    static {
        //TODO: define constants for annotation model and linking model
        ENGLISH.getProperties().put("olia.annotationModel",
                "http://purl.org/olia/penn.owl");
        ENGLISH.getProperties().put("olia.linkingModel",
                "http://purl.org/olia/penn-link.rdf");

        ENGLISH.addTag(new PosTag("CC", Pos.CoordinatingConjunction));
        ENGLISH.addTag(new PosTag("CD", Pos.CardinalNumber));
        ENGLISH.addTag(new PosTag("DT", Pos.Determiner));
        ENGLISH.addTag(new PosTag("EX", Pos.ExistentialParticle)); //TODO: unsure mapping
        ENGLISH.addTag(new PosTag("FW", Pos.Foreign));
        ENGLISH.addTag(new PosTag("IN", Pos.Preposition, Pos.SubordinatingConjunction));
        ENGLISH.addTag(new PosTag("JJ", LexicalCategory.Adjective));
        ENGLISH.addTag(new PosTag("JJR", LexicalCategory.Adjective, Pos.ComparativeParticle));
        ENGLISH.addTag(new PosTag("JJS", LexicalCategory.Adjective, Pos.SuperlativeParticle));
        ENGLISH.addTag(new PosTag("LS", Pos.ListMarker));
        ENGLISH.addTag(new PosTag("MD", Pos.ModalVerb));
        ENGLISH.addTag(new PosTag("NN", Pos.CommonNoun, Pos.SingularQuantifier));
        ENGLISH.addTag(new PosTag("NNP", Pos.ProperNoun, Pos.SingularQuantifier));
        ENGLISH.addTag(new PosTag("NNPS", Pos.ProperNoun, Pos.PluralQuantifier));
        ENGLISH.addTag(new PosTag("NNS", Pos.CommonNoun, Pos.PluralQuantifier));
        ENGLISH.addTag(new PosTag("PDT", Pos.Determiner)); //TODO should be Pre-Determiner
        ENGLISH.addTag(new PosTag("POS", Pos.PossessiveDeterminer)); //TODO: map Possessive Ending (e.g., Nouns ending in 's)
        ENGLISH.addTag(new PosTag("PP", Pos.PersonalPronoun));
        ENGLISH.addTag(new PosTag("PP$", Pos.PossessivePronoun));
        ENGLISH.addTag(new PosTag("PRP", Pos.PersonalPronoun));
        ENGLISH.addTag(new PosTag("PRP$", Pos.PossessivePronoun));
        ENGLISH.addTag(new PosTag("RB", LexicalCategory.Adverb));
        ENGLISH.addTag(new PosTag("RBR", LexicalCategory.Adverb, Pos.ComparativeParticle));
        ENGLISH.addTag(new PosTag("RBS", LexicalCategory.Adverb, Pos.SuperlativeParticle));
        ENGLISH.addTag(new PosTag("RP", Pos.Participle));
        ENGLISH.addTag(new PosTag("SYM", Pos.Symbol));
        ENGLISH.addTag(new PosTag("TO", LexicalCategory.Adposition));
        ENGLISH.addTag(new PosTag("UH", LexicalCategory.Interjection));
        ENGLISH.addTag(new PosTag("VB", Pos.Infinitive)); //TODO check a Verb in the base form should be Pos.Infinitive
        ENGLISH.addTag(new PosTag("VBD", Pos.PastParticiple)); //TODO check
        ENGLISH.addTag(new PosTag("VBG", Pos.PresentParticiple, Pos.Gerund));
        ENGLISH.addTag(new PosTag("VBN", Pos.PastParticiple));
        ENGLISH.addTag(new PosTag("VBP", Pos.PresentParticiple));
        ENGLISH.addTag(new PosTag("VBZ", Pos.PresentParticiple));
        ENGLISH.addTag(new PosTag("WDT", Pos.WHDeterminer));
        ENGLISH.addTag(new PosTag("WP", Pos.WHPronoun));
        ENGLISH.addTag(new PosTag("WP$", Pos.PossessivePronoun, Pos.WHPronoun));
        ENGLISH.addTag(new PosTag("WRB", Pos.WHTypeAdverbs));
        ENGLISH.addTag(new PosTag("´´", Pos.CloseQuote));
        ENGLISH.addTag(new PosTag("''", Pos.Quote));
        ENGLISH.addTag(new PosTag(":", Pos.Colon));
        ENGLISH.addTag(new PosTag(",", Pos.Comma));
        ENGLISH.addTag(new PosTag("$", LexicalCategory.Residual));
        ENGLISH.addTag(new PosTag("\"", Pos.Quote));
        ENGLISH.addTag(new PosTag("``", Pos.OpenQuote));
        ENGLISH.addTag(new PosTag(".", Pos.Point));
        ENGLISH.addTag(new PosTag("#", Pos.SecondaryPunctuation));
        ENGLISH.addTag(new PosTag("{", Pos.OpenCurlyBracket));
        ENGLISH.addTag(new PosTag("}", Pos.CloseCurlyBracket));
        ENGLISH.addTag(new PosTag("[", Pos.OpenSquareBracket));
        ENGLISH.addTag(new PosTag("]", Pos.CloseSquareBracket));
        ENGLISH.addTag(new PosTag("(", Pos.OpenParenthesis));
        ENGLISH.addTag(new PosTag(")", Pos.CloseParenthesis));
        ENGLISH.addTag(new PosTag("-LRB-", Pos.OpenParenthesis)); //deprecated tag
        ENGLISH.addTag(new PosTag("-RRB-", Pos.CloseParenthesis)); //deprecated tag
    }

    /*
     * Links to the STTS model as defined by the
     * <a herf="http://nlp2rdf.lod2.eu/olia/">OLIA</a> Ontology.
     * @see German#STTS
     */
    private static final TagSet<PosTag> GERMAN = new TagSet<PosTag>(
            "German STTS, Tiger and Negra POS tagsets", "de");

    static {
        //This covers Tiger and Negra Tag sets (as documented by the mappings
        //to uPos (see 
        GERMAN.getProperties().put("olia.annotationModel",
                "http://purl.org/olia/stts.owl");
        GERMAN.getProperties().put("olia.linkingModel",
                "http://purl.org/olia/stts-link.rdf");

        GERMAN.addTag(new PosTag("ADJA", Pos.AttributiveAdjective));
        GERMAN.addTag(new PosTag("ADJD", Pos.PredicativeAdjective));
        GERMAN.addTag(new PosTag("ADV", LexicalCategory.Adverb));
        GERMAN.addTag(new PosTag("APPR", Pos.Preposition));
        GERMAN.addTag(new PosTag("APPRART", Pos.FusedPrepArt));
        GERMAN.addTag(new PosTag("APPO", Pos.Postposition));
        GERMAN.addTag(new PosTag("APZR", Pos.Circumposition));
        GERMAN.addTag(new PosTag("ART", Pos.Article));
        GERMAN.addTag(new PosTag("CARD", Pos.CardinalNumber));
        GERMAN.addTag(new PosTag("FM", Pos.Foreign));
        GERMAN.addTag(new PosTag("ITJ", LexicalCategory.Interjection));
        GERMAN.addTag(new PosTag("KOUI", Pos.SubordinatingConjunction));
        GERMAN.addTag(new PosTag("KOUS", Pos.SubordinatingConjunctionWithFiniteClause));
        GERMAN.addTag(new PosTag("KON", Pos.CoordinatingConjunction));
        GERMAN.addTag(new PosTag("KOKOM", Pos.ComparativeParticle));
        GERMAN.addTag(new PosTag("NN", Pos.CommonNoun));
        GERMAN.addTag(new PosTag("NE", Pos.ProperNoun));
        GERMAN.addTag(new PosTag("PDS", Pos.DemonstrativePronoun, Pos.SubstitutivePronoun));
        GERMAN.addTag(new PosTag("PDAT", Pos.DemonstrativePronoun, Pos.AttributivePronoun));
        GERMAN.addTag(new PosTag("PIS", Pos.SubstitutivePronoun, Pos.IndefinitePronoun));
        GERMAN.addTag(new PosTag("PIAT", Pos.AttributivePronoun, Pos.IndefinitePronoun));
        GERMAN.addTag(new PosTag("PIDAT", Pos.AttributivePronoun, Pos.IndefinitePronoun));
        GERMAN.addTag(new PosTag("PPER", Pos.PersonalPronoun));
        GERMAN.addTag(new PosTag("PPOSS", Pos.SubstitutivePronoun, Pos.PossessivePronoun));
        GERMAN.addTag(new PosTag("PPOSAT", Pos.AttributivePronoun, Pos.PossessivePronoun));
        GERMAN.addTag(new PosTag("PRELS", Pos.SubstitutivePronoun, Pos.RelativePronoun));
        GERMAN.addTag(new PosTag("PRELAT", Pos.AttributivePronoun, Pos.RelativePronoun));
        GERMAN.addTag(new PosTag("PRF", Pos.ReflexivePronoun));
        GERMAN.addTag(new PosTag("PWS", Pos.SubstitutivePronoun, Pos.InterrogativePronoun));
        GERMAN.addTag(new PosTag("PWAT", Pos.AttributivePronoun, Pos.InterrogativePronoun));
        GERMAN.addTag(new PosTag("PWAV", LexicalCategory.Adverb, Pos.RelativePronoun, Pos.InterrogativePronoun));
        GERMAN.addTag(new PosTag("PAV", Pos.PronominalAdverb));
        //Tiger-STTS for PAV
        GERMAN.addTag(new PosTag("PROAV", Pos.PronominalAdverb));
        GERMAN.addTag(new PosTag("PTKA", Pos.AdjectivalParticle));
        GERMAN.addTag(new PosTag("PTKANT", Pos.Particle));
        GERMAN.addTag(new PosTag("PTKNEG", Pos.NegativeParticle));
        GERMAN.addTag(new PosTag("PTKVZ", Pos.VerbalParticle));
        GERMAN.addTag(new PosTag("PTKZU", Pos.Particle)); //particle "zu"  e.g. "zu [gehen]".
        GERMAN.addTag(new PosTag("TRUNC", Pos.Abbreviation)); //e.g. An- [und Abreise] 
        GERMAN.addTag(new PosTag("VVIMP", Pos.ImperativeVerb));
        GERMAN.addTag(new PosTag("VVINF", Pos.Infinitive));
        GERMAN.addTag(new PosTag("VVFIN", Pos.FiniteVerb));
        GERMAN.addTag(new PosTag("VVIZU", Pos.Infinitive));
        GERMAN.addTag(new PosTag("VVPP", Pos.PastParticiple));
        GERMAN.addTag(new PosTag("VAFIN", Pos.FiniteVerb, Pos.AuxiliaryVerb));
        GERMAN.addTag(new PosTag("VAIMP", Pos.AuxiliaryVerb, Pos.ImperativeVerb));
        GERMAN.addTag(new PosTag("VAINF", Pos.AuxiliaryVerb, Pos.Infinitive));
        GERMAN.addTag(new PosTag("VAPP", Pos.PastParticiple, Pos.AuxiliaryVerb));
        GERMAN.addTag(new PosTag("VMFIN", Pos.FiniteVerb, Pos.ModalVerb));
        GERMAN.addTag(new PosTag("VMINF", Pos.Infinitive, Pos.ModalVerb));
        GERMAN.addTag(new PosTag("VMPP", Pos.PastParticiple, Pos.ModalVerb));
        GERMAN.addTag(new PosTag("XY", Pos.Symbol)); //non words (e.g. H20, 3:7 ...)
        GERMAN.addTag(new PosTag("$.", Pos.Point));
        GERMAN.addTag(new PosTag("$,", Pos.Comma));
        GERMAN.addTag(new PosTag("$(", Pos.ParentheticalPunctuation));
        GERMAN.addTag(new PosTag("$[", Pos.ParentheticalPunctuation));
        GERMAN.addTag(new PosTag("NNE", Pos.ProperNoun)); //Normal nouns in named entities (not in stts 1999)
    }

    public static final Map<String, TagSet<PosTag>> TAG_SETS;

    static {
        Map<String, TagSet<PosTag>> tagSets = new HashMap<>();
        tagSets.put("de", GERMAN);
        tagSets.put("en", ENGLISH);
        TAG_SETS = Collections.unmodifiableMap(tagSets);
    }


    public static final TagSet<NerTag> NER_TAG_SET = new TagSet<NerTag>("German NER Tagset", "de");

    static {
        NER_TAG_SET.addTag(new NerTag("PERSON", NerTag.NAMED_ENTITY_PERSON));
        NER_TAG_SET.addTag(new NerTag("person", NerTag.NAMED_ENTITY_PERSON));
        NER_TAG_SET.addTag(new NerTag("Person", NerTag.NAMED_ENTITY_PERSON));
        NER_TAG_SET.addTag(new NerTag("PER", NerTag.NAMED_ENTITY_PERSON));
        NER_TAG_SET.addTag(new NerTag("per", NerTag.NAMED_ENTITY_PERSON));
        NER_TAG_SET.addTag(new NerTag("B-PERS", NerTag.NAMED_ENTITY_PERSON));
        NER_TAG_SET.addTag(new NerTag("I-PERS", NerTag.NAMED_ENTITY_PERSON));
        NER_TAG_SET.addTag(new NerTag("B-PER", NerTag.NAMED_ENTITY_PERSON));
        NER_TAG_SET.addTag(new NerTag("I-PER", NerTag.NAMED_ENTITY_PERSON));

        NER_TAG_SET.addTag(new NerTag("ORGANIZATION", NerTag.NAMED_ENTITY_ORGANIZATION));
        NER_TAG_SET.addTag(new NerTag("organization", NerTag.NAMED_ENTITY_ORGANIZATION));
        NER_TAG_SET.addTag(new NerTag("Organization", NerTag.NAMED_ENTITY_ORGANIZATION));
        NER_TAG_SET.addTag(new NerTag("ORGANISATION", NerTag.NAMED_ENTITY_ORGANIZATION));
        NER_TAG_SET.addTag(new NerTag("organisation", NerTag.NAMED_ENTITY_ORGANIZATION));
        NER_TAG_SET.addTag(new NerTag("Organisation", NerTag.NAMED_ENTITY_ORGANIZATION));
        NER_TAG_SET.addTag(new NerTag("ORG", NerTag.NAMED_ENTITY_ORGANIZATION));
        NER_TAG_SET.addTag(new NerTag("org", NerTag.NAMED_ENTITY_ORGANIZATION));
        NER_TAG_SET.addTag(new NerTag("B-ORG", NerTag.NAMED_ENTITY_ORGANIZATION));
        NER_TAG_SET.addTag(new NerTag("I-ORG", NerTag.NAMED_ENTITY_ORGANIZATION));

        NER_TAG_SET.addTag(new NerTag("LOCATION", NerTag.NAMED_ENTITY_LOCATION));
        NER_TAG_SET.addTag(new NerTag("Location", NerTag.NAMED_ENTITY_LOCATION));
        NER_TAG_SET.addTag(new NerTag("location", NerTag.NAMED_ENTITY_LOCATION));
        NER_TAG_SET.addTag(new NerTag("LOC", NerTag.NAMED_ENTITY_LOCATION));
        NER_TAG_SET.addTag(new NerTag("loc", NerTag.NAMED_ENTITY_LOCATION));
        NER_TAG_SET.addTag(new NerTag("B-LOC", NerTag.NAMED_ENTITY_LOCATION));
        NER_TAG_SET.addTag(new NerTag("I-LOC", NerTag.NAMED_ENTITY_LOCATION));
        NER_TAG_SET.addTag(new NerTag("FACILITY", NerTag.NAMED_ENTITY_LOCATION));
        NER_TAG_SET.addTag(new NerTag("GPE", NerTag.NAMED_ENTITY_LOCATION));

        NER_TAG_SET.addTag(new NerTag("EVENT", NerTag.NAMED_ENTITY_EVENT));

        NER_TAG_SET.addTag(new NerTag("MISC", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("misc", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("B-MISC", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("I-MISC", NerTag.NAMED_ENTITY_MISC));

        NER_TAG_SET.addTag(new NerTag("NORP", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("PRODUCT", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("WORK OF ART", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("LAW", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("LANGUAGE", NerTag.NAMED_ENTITY_MISC));

        NER_TAG_SET.addTag(new NerTag("DATE", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("TIME", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("PERCENT", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("MONEY", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("QUANTITY", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("ORDINAL", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("CARDINAL", NerTag.NAMED_ENTITY_MISC));
    }
}
