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

package io.redlink.nlp.opennlp.es;

import io.redlink.nlp.model.pos.LexicalCategory;
import io.redlink.nlp.model.pos.Pos;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.tag.TagSet;
import io.redlink.nlp.opennlp.pos.OpenNlpLanguageModel;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Implementation of Spanish-specific tools for natural language processing.
 *
 * @author rupert.westenthaler@redlink.co
 */
@Service
public class LanguageSpanish extends OpenNlpLanguageModel {
    /**
     * Links to the PAROLE model as defined by the
     * <a herf="http://nlp2rdf.lod2.eu/olia/">OLIA</a> Ontology.
     *
     * @see Spanish#SPANISH_PAROLE
     */
    public static final TagSet<PosTag> SPANISH_PAROLE = new TagSet<PosTag>("PAROLE Spanish", "es");

    static {
        //TODO: define constants for annotation model and linking model
        SPANISH_PAROLE.getProperties().put("olia.annotationModel",
                "http://purl.org/olia/parole_es_cat.owl");

        SPANISH_PAROLE.addTag(new PosTag("AO", LexicalCategory.Adjective));
        SPANISH_PAROLE.addTag(new PosTag("AQ", Pos.QualifierAdjective));
        SPANISH_PAROLE.addTag(new PosTag("CC", Pos.CoordinatingConjunction));
        SPANISH_PAROLE.addTag(new PosTag("CS", Pos.SubordinatingConjunction));
        SPANISH_PAROLE.addTag(new PosTag("DA", Pos.Article));
        SPANISH_PAROLE.addTag(new PosTag("DD", Pos.DemonstrativeDeterminer));
        SPANISH_PAROLE.addTag(new PosTag("DE", Pos.ExclamatoryDeterminer));
        SPANISH_PAROLE.addTag(new PosTag("DI", Pos.IndefiniteDeterminer));
        SPANISH_PAROLE.addTag(new PosTag("DN", Pos.Numeral, Pos.Determiner));
        SPANISH_PAROLE.addTag(new PosTag("DP", Pos.PossessiveDeterminer));
        SPANISH_PAROLE.addTag(new PosTag("DT", Pos.InterrogativeDeterminer));
        SPANISH_PAROLE.addTag(new PosTag("Faa", LexicalCategory.Punctuation));
        SPANISH_PAROLE.addTag(new PosTag("Fat", Pos.ExclamativePoint));
        SPANISH_PAROLE.addTag(new PosTag("Fc", Pos.Comma));
        SPANISH_PAROLE.addTag(new PosTag("Fd", Pos.Colon));
        SPANISH_PAROLE.addTag(new PosTag("Fe", Pos.Quote));
        SPANISH_PAROLE.addTag(new PosTag("Fg", Pos.Hyphen));
        SPANISH_PAROLE.addTag(new PosTag("Fh", Pos.Slash));
        SPANISH_PAROLE.addTag(new PosTag("Fia", Pos.InvertedQuestionMark));
        SPANISH_PAROLE.addTag(new PosTag("Fit", Pos.QuestionMark));
        SPANISH_PAROLE.addTag(new PosTag("Fp", Pos.Point));
        SPANISH_PAROLE.addTag(new PosTag("Fpa", Pos.OpenParenthesis));
        SPANISH_PAROLE.addTag(new PosTag("Fpt", Pos.CloseParenthesis));
        SPANISH_PAROLE.addTag(new PosTag("Fs", Pos.SuspensionPoints));
        SPANISH_PAROLE.addTag(new PosTag("Fx", Pos.SemiColon));
        SPANISH_PAROLE.addTag(new PosTag("Fz", LexicalCategory.Punctuation));
        SPANISH_PAROLE.addTag(new PosTag("Ft", Pos.SecondaryPunctuation)); // %
        SPANISH_PAROLE.addTag(new PosTag("I", LexicalCategory.Interjection));
        SPANISH_PAROLE.addTag(new PosTag("NC", Pos.CommonNoun));
        SPANISH_PAROLE.addTag(new PosTag("NP", Pos.ProperNoun));
        SPANISH_PAROLE.addTag(new PosTag("P0", Pos.Pronoun)); //TODO: CliticPronoun is missing
        SPANISH_PAROLE.addTag(new PosTag("PD", Pos.DemonstrativePronoun));
        SPANISH_PAROLE.addTag(new PosTag("PE", Pos.ExclamatoryPronoun));
        SPANISH_PAROLE.addTag(new PosTag("PI", Pos.IndefinitePronoun));
        SPANISH_PAROLE.addTag(new PosTag("PN", Pos.Pronoun)); //TODO: NumeralPronoun is missing
        SPANISH_PAROLE.addTag(new PosTag("PP", Pos.PersonalPronoun));
        SPANISH_PAROLE.addTag(new PosTag("PR", Pos.RelativePronoun));
        SPANISH_PAROLE.addTag(new PosTag("PT", Pos.InterrogativePronoun));
        SPANISH_PAROLE.addTag(new PosTag("PX", Pos.PossessivePronoun));
        SPANISH_PAROLE.addTag(new PosTag("RG", LexicalCategory.Adverb));
        SPANISH_PAROLE.addTag(new PosTag("RN", Pos.NegativeAdverb));
        SPANISH_PAROLE.addTag(new PosTag("SP", Pos.Preposition));
        SPANISH_PAROLE.addTag(new PosTag("VAG", Pos.StrictAuxiliaryVerb, Pos.Gerund));
        SPANISH_PAROLE.addTag(new PosTag("VAI", Pos.StrictAuxiliaryVerb, Pos.IndicativeVerb));
        SPANISH_PAROLE.addTag(new PosTag("VAM", Pos.StrictAuxiliaryVerb, Pos.ImperativeVerb));
        SPANISH_PAROLE.addTag(new PosTag("VAN", Pos.StrictAuxiliaryVerb, Pos.Infinitive));
        SPANISH_PAROLE.addTag(new PosTag("VAP", Pos.StrictAuxiliaryVerb, Pos.Participle));
        SPANISH_PAROLE.addTag(new PosTag("VAS", Pos.StrictAuxiliaryVerb, Pos.SubjunctiveVerb));
        SPANISH_PAROLE.addTag(new PosTag("VMG", Pos.MainVerb, Pos.Gerund));
        SPANISH_PAROLE.addTag(new PosTag("VMI", Pos.MainVerb, Pos.IndicativeVerb));
        SPANISH_PAROLE.addTag(new PosTag("VMM", Pos.MainVerb, Pos.ImperativeVerb));
        SPANISH_PAROLE.addTag(new PosTag("VMN", Pos.MainVerb, Pos.Infinitive));
        SPANISH_PAROLE.addTag(new PosTag("VMP", Pos.MainVerb, Pos.Participle));
        SPANISH_PAROLE.addTag(new PosTag("VMS", Pos.MainVerb, Pos.SubjunctiveVerb));
        SPANISH_PAROLE.addTag(new PosTag("VSG", Pos.ModalVerb, Pos.Gerund));
        SPANISH_PAROLE.addTag(new PosTag("VSI", Pos.ModalVerb, Pos.IndicativeVerb));
        SPANISH_PAROLE.addTag(new PosTag("VSM", Pos.ModalVerb, Pos.ImperativeVerb));
        SPANISH_PAROLE.addTag(new PosTag("VSN", Pos.ModalVerb, Pos.Infinitive));
        SPANISH_PAROLE.addTag(new PosTag("VSP", Pos.ModalVerb, Pos.Participle));
        SPANISH_PAROLE.addTag(new PosTag("VSS", Pos.ModalVerb, Pos.SubjunctiveVerb));
        SPANISH_PAROLE.addTag(new PosTag("W", Pos.Date)); //date times
        SPANISH_PAROLE.addTag(new PosTag("X")); //unknown
        SPANISH_PAROLE.addTag(new PosTag("Y", Pos.Abbreviation)); //abbreviation
        SPANISH_PAROLE.addTag(new PosTag("Z", Pos.Image)); //Figures
        SPANISH_PAROLE.addTag(new PosTag("Zm", Pos.Symbol)); //currency
        SPANISH_PAROLE.addTag(new PosTag("Zp", Pos.Symbol)); //percentage
    }

    public LanguageSpanish() {
        super(Locale.forLanguageTag("es"), SPANISH_PAROLE, "es-sent.bin", null, "es-pos-maxent.bin");
    }

}
