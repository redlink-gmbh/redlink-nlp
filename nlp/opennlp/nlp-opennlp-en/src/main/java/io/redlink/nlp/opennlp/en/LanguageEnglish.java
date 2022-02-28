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

package io.redlink.nlp.opennlp.en;

import io.redlink.nlp.model.pos.LexicalCategory;
import io.redlink.nlp.model.pos.Pos;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.tag.TagSet;
import io.redlink.nlp.opennlp.pos.OpenNlpLanguageModel;
import org.springframework.stereotype.Service;

import static java.util.Locale.ENGLISH;

/**
 * Implementation of English-specific tools for natural language processing.
 *
 * @author rupert.westenthaler@redlink.co
 */
@Service
public class LanguageEnglish extends OpenNlpLanguageModel {

    /**
     * Links to the Penn Treebank model as defined by the
     * <a herf="http://nlp2rdf.lod2.eu/olia/">OLIA</a> Ontology.
     *
     * @see English#ENGLISH
     */
    public static final TagSet<PosTag> ENGLISH_PENN_TREEBANK = new TagSet<PosTag>(
            "Penn Treebank", "en");

    static {
        //TODO: define constants for annotation model and linking model
        ENGLISH_PENN_TREEBANK.getProperties().put("olia.annotationModel",
                "http://purl.org/olia/penn.owl");
        ENGLISH_PENN_TREEBANK.getProperties().put("olia.linkingModel",
                "http://purl.org/olia/penn-link.rdf");

        ENGLISH_PENN_TREEBANK.addTag(new PosTag("CC", Pos.CoordinatingConjunction));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("CD", Pos.CardinalNumber));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("DT", Pos.Determiner));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("EX", Pos.ExistentialParticle)); //TODO: unsure mapping
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("FW", Pos.Foreign));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("IN", Pos.Preposition, Pos.SubordinatingConjunction));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("JJ", LexicalCategory.Adjective));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("JJR", LexicalCategory.Adjective, Pos.ComparativeParticle));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("JJS", LexicalCategory.Adjective, Pos.SuperlativeParticle));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("LS", Pos.ListMarker));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("MD", Pos.ModalVerb));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("NN", Pos.CommonNoun, Pos.SingularQuantifier));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("NNP", Pos.ProperNoun, Pos.SingularQuantifier));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("NNPS", Pos.ProperNoun, Pos.PluralQuantifier));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("NNS", Pos.CommonNoun, Pos.PluralQuantifier));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("PDT", Pos.Determiner)); //TODO should be Pre-Determiner
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("POS", Pos.PossessiveDeterminer)); //TODO: map Possessive Ending (e.g., Nouns ending in 's)
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("PP", Pos.PersonalPronoun));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("PP$", Pos.PossessivePronoun));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("PRP", Pos.PersonalPronoun));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("PRP$", Pos.PossessivePronoun));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("RB", LexicalCategory.Adverb));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("RBR", LexicalCategory.Adverb, Pos.ComparativeParticle));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("RBS", LexicalCategory.Adverb, Pos.SuperlativeParticle));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("RP", Pos.Participle));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("SYM", Pos.Symbol));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("TO", LexicalCategory.Adposition));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("UH", LexicalCategory.Interjection));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("VB", Pos.Infinitive)); //TODO check a Verb in the base form should be Pos.Infinitive
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("VBD", Pos.PastParticiple)); //TODO check
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("VBG", Pos.PresentParticiple, Pos.Gerund));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("VBN", Pos.PastParticiple));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("VBP", Pos.PresentParticiple));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("VBZ", Pos.PresentParticiple));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("WDT", Pos.WHDeterminer));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("WP", Pos.WHPronoun));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("WP$", Pos.PossessivePronoun, Pos.WHPronoun));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("WRB", Pos.WHTypeAdverbs));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("´´", Pos.CloseQuote));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("''", Pos.Quote));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag(":", Pos.Colon));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag(",", Pos.Comma));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("$", LexicalCategory.Residual));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("\"", Pos.Quote));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("``", Pos.OpenQuote));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag(".", Pos.Point));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("#", Pos.SecondaryPunctuation));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("{", Pos.OpenCurlyBracket));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("}", Pos.CloseCurlyBracket));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("[", Pos.OpenSquareBracket));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("]", Pos.CloseSquareBracket));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("(", Pos.OpenParenthesis));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag(")", Pos.CloseParenthesis));
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("-LRB-", Pos.OpenParenthesis)); //deprecated tag
        ENGLISH_PENN_TREEBANK.addTag(new PosTag("-RRB-", Pos.CloseParenthesis)); //deprecated tag
    }

    public LanguageEnglish() {
        super(ENGLISH, ENGLISH_PENN_TREEBANK, "en-sent.bin", "en-token.bin", "en-pos-maxent.bin");
    }

}
