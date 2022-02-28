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

package io.redlink.nlp.opennlp.pos.it;

import io.redlink.nlp.model.pos.LexicalCategory;
import io.redlink.nlp.model.pos.Pos;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.tag.TagSet;
import io.redlink.nlp.opennlp.pos.OpenNlpLanguageModel;
import org.springframework.stereotype.Service;

import static java.util.Locale.ITALIAN;

/**
 * Implementation of Italian-specific tools for natural language processing.
 *
 * @author rupert.westenthaler@redlink.co
 */
@Service
public class LanguageItalian extends OpenNlpLanguageModel {

    //TODO: add Italien PosTag set
    /**
     * Mappings for the the <a href="http://medialab.di.unipi.it/wiki/Tanl_POS_Tagset">Tanl Tagset</a> for Italian
     */
    public static final TagSet<PosTag> ITALIAN_TANL = new TagSet<PosTag>("Tanl Italian", "it");

    static {
        ITALIAN_TANL.addTag(new PosTag("A", LexicalCategory.Adjective)); //adjective
        ITALIAN_TANL.addTag(new PosTag("An", LexicalCategory.Adjective)); //underspecified adjective
        ITALIAN_TANL.addTag(new PosTag("Ap", LexicalCategory.Adjective, Pos.PluralQuantifier)); //plural adjective
        ITALIAN_TANL.addTag(new PosTag("As", LexicalCategory.Adjective, Pos.SingularQuantifier)); //singular adjective
        ITALIAN_TANL.addTag(new PosTag("AP", Pos.PossessiveAdjective)); //possessive adjective
        ITALIAN_TANL.addTag(new PosTag("APn", Pos.PossessiveAdjective)); //underspecified possessive adjective
        ITALIAN_TANL.addTag(new PosTag("APs", Pos.PossessiveAdjective, Pos.SingularQuantifier)); //singular possessive adjective
        ITALIAN_TANL.addTag(new PosTag("APp", Pos.PossessiveAdjective, Pos.PluralQuantifier)); //plural possessive adjective
        ITALIAN_TANL.addTag(new PosTag("B", LexicalCategory.Adverb)); //adverb
        ITALIAN_TANL.addTag(new PosTag("BN", Pos.NegativeAdverb)); //negation adverb
        ITALIAN_TANL.addTag(new PosTag("C", LexicalCategory.Conjuction)); //conjunction
        ITALIAN_TANL.addTag(new PosTag("CC", Pos.CoordinatingConjunction)); //coordinate conjunction
        ITALIAN_TANL.addTag(new PosTag("CS", Pos.SubordinatingConjunction)); //subordinate conjunction
        ITALIAN_TANL.addTag(new PosTag("D", Pos.Determiner)); //determiner
        ITALIAN_TANL.addTag(new PosTag("DD", Pos.DemonstrativeDeterminer)); //demonstrative determiner
        ITALIAN_TANL.addTag(new PosTag("DE", Pos.ExclamatoryDeterminer)); //exclamative determiner
        ITALIAN_TANL.addTag(new PosTag("DI", Pos.IndefiniteDeterminer)); //indefinite determiner
        ITALIAN_TANL.addTag(new PosTag("DQ", Pos.InterrogativeDeterminer)); //interrogative determiner
        ITALIAN_TANL.addTag(new PosTag("DR", Pos.RelativeDeterminer)); //relative determiner
        ITALIAN_TANL.addTag(new PosTag("E", Pos.Preposition)); //preposition
        ITALIAN_TANL.addTag(new PosTag("EA", Pos.FusedPrepArt)); //articulated preposition
        ITALIAN_TANL.addTag(new PosTag("F", LexicalCategory.Punctuation)); //punctuation
        ITALIAN_TANL.addTag(new PosTag("FB", Pos.ParentheticalPunctuation)); //balanced punctuation
        ITALIAN_TANL.addTag(new PosTag("FC", Pos.MainPunctuation)); //clause boundary punctuation
        ITALIAN_TANL.addTag(new PosTag("FF", Pos.Comma)); //comma
        ITALIAN_TANL.addTag(new PosTag("FS", Pos.SentenceFinalPunctuation)); //sentence boundary punctuation
        ITALIAN_TANL.addTag(new PosTag("I", Pos.Interjection)); //interjection
        ITALIAN_TANL.addTag(new PosTag("N", Pos.CardinalNumber)); //cardinal number
        ITALIAN_TANL.addTag(new PosTag("NO", Pos.OrdinalNumber)); //ordinal number
        ITALIAN_TANL.addTag(new PosTag("NOn", Pos.OrdinalNumber)); //underspecified ordinal number
        ITALIAN_TANL.addTag(new PosTag("NOs", Pos.OrdinalNumber, Pos.SingularQuantifier)); //ordinal number
        ITALIAN_TANL.addTag(new PosTag("NOp", Pos.OrdinalNumber, Pos.PluralQuantifier)); //ordinal number
        ITALIAN_TANL.addTag(new PosTag("P", Pos.Pronoun)); //pronoun
        ITALIAN_TANL.addTag(new PosTag("PC", Pos.Pronoun)); //clitic pronoun  TODO: clitic is missing
        ITALIAN_TANL.addTag(new PosTag("PD", Pos.DemonstrativePronoun)); //demonstrative pronoun
        ITALIAN_TANL.addTag(new PosTag("PE", Pos.PersonalPronoun)); //personal pronoun
        ITALIAN_TANL.addTag(new PosTag("PI", Pos.IndefinitePronoun)); //indefinite pronoun
        ITALIAN_TANL.addTag(new PosTag("PP", Pos.PossessivePronoun)); //possessive pronoun
        ITALIAN_TANL.addTag(new PosTag("PQ", Pos.InterrogativePronoun)); //interrogative pronoun
        ITALIAN_TANL.addTag(new PosTag("PR", Pos.RelativePronoun)); //relative pronoun
        ITALIAN_TANL.addTag(new PosTag("R", Pos.Article)); //article
        ITALIAN_TANL.addTag(new PosTag("RD", Pos.DefiniteArticle)); //determinative article  TODO: determinative ~ definite??
        ITALIAN_TANL.addTag(new PosTag("RI", Pos.IndefiniteArticle)); //indeterminative article
        ITALIAN_TANL.addTag(new PosTag("S", Pos.CommonNoun)); //common noun
        ITALIAN_TANL.addTag(new PosTag("Sn", LexicalCategory.Noun)); //underspecified noun
        ITALIAN_TANL.addTag(new PosTag("Ss", LexicalCategory.Noun, Pos.SingularQuantifier)); //singular noun
        ITALIAN_TANL.addTag(new PosTag("Sp", LexicalCategory.Noun, Pos.PluralQuantifier)); //plural noun
        ITALIAN_TANL.addTag(new PosTag("SA", Pos.Abbreviation)); //abbreviation
        ITALIAN_TANL.addTag(new PosTag("SP", Pos.ProperNoun)); //proper noun
        ITALIAN_TANL.addTag(new PosTag("SW", Pos.ProperNoun, Pos.Foreign)); //foreign name
        ITALIAN_TANL.addTag(new PosTag("SWn", Pos.ProperNoun, Pos.Foreign)); //underspecified foreign name
        ITALIAN_TANL.addTag(new PosTag("SWs", Pos.ProperNoun, Pos.Foreign, Pos.SingularQuantifier)); //underspecified foreign name singular
        ITALIAN_TANL.addTag(new PosTag("SWp", Pos.ProperNoun, Pos.Foreign, Pos.PluralQuantifier)); //underspecified foreign name plural
        ITALIAN_TANL.addTag(new PosTag("T", Pos.Determiner)); //predeterminer
        ITALIAN_TANL.addTag(new PosTag("V", LexicalCategory.Verb)); //verb
        ITALIAN_TANL.addTag(new PosTag("Vip", Pos.MainVerb, Pos.IndicativeVerb, Pos.PresentParticiple)); //main verb indicative present
        ITALIAN_TANL.addTag(new PosTag("Vip3", Pos.MainVerb, Pos.IndicativeVerb, Pos.PresentParticiple)); //main verb indicative present 3rd person
        ITALIAN_TANL.addTag(new PosTag("Vii", Pos.MainVerb, Pos.IndicativeVerb)); //main verb indicative imperfect
        ITALIAN_TANL.addTag(new PosTag("Vii3", Pos.MainVerb, Pos.IndicativeVerb)); //main verb indicative imperfect 3rd person
        ITALIAN_TANL.addTag(new PosTag("Vis", Pos.MainVerb, Pos.IndicativeVerb, Pos.PastParticiple)); //main verb indicative past
        ITALIAN_TANL.addTag(new PosTag("Vis3", Pos.MainVerb, Pos.IndicativeVerb, Pos.PastParticiple)); //main verb indicative past 3rd person
        ITALIAN_TANL.addTag(new PosTag("Vif", Pos.MainVerb, Pos.IndicativeVerb, Pos.FutureParticle)); //main verb indicative future
        ITALIAN_TANL.addTag(new PosTag("Vif3", Pos.MainVerb, Pos.IndicativeVerb, Pos.FutureParticle)); //main verb indicative future 3rd person
        ITALIAN_TANL.addTag(new PosTag("Vm", Pos.MainVerb, Pos.ImperativeVerb)); //main verb imperative
        ITALIAN_TANL.addTag(new PosTag("Vm3", Pos.MainVerb, Pos.ImperativeVerb)); //main verb imperative 3rd person
        ITALIAN_TANL.addTag(new PosTag("Vcp", LexicalCategory.Conjuction, Pos.MainVerb, Pos.PresentParticiple)); //main verb conjunctive present
        ITALIAN_TANL.addTag(new PosTag("Vcp3", LexicalCategory.Conjuction, Pos.MainVerb, Pos.PresentParticiple)); //main verb conjunctive present 3rd person
        ITALIAN_TANL.addTag(new PosTag("Vci", LexicalCategory.Conjuction, Pos.MainVerb)); //main verb conjunctive imperfect
        ITALIAN_TANL.addTag(new PosTag("Vci3", LexicalCategory.Conjuction, Pos.MainVerb)); //main verb conjunctive imperfect, 3rd person
        ITALIAN_TANL.addTag(new PosTag("Vd", Pos.MainVerb, Pos.ConditionalVerb)); //main verb conditional
        ITALIAN_TANL.addTag(new PosTag("Vdp", Pos.MainVerb, Pos.ConditionalVerb, Pos.PresentParticiple)); //main verb conditional present, other than 3rd person
        ITALIAN_TANL.addTag(new PosTag("Vdp3", Pos.MainVerb, Pos.ConditionalVerb, Pos.PresentParticiple)); //main verb conditional present 3rd person
        ITALIAN_TANL.addTag(new PosTag("Vf", Pos.MainVerb, Pos.InfinitiveParticle)); //main verb infinite
        ITALIAN_TANL.addTag(new PosTag("Vg", Pos.MainVerb, Pos.Gerund)); //main verb gerundive
        ITALIAN_TANL.addTag(new PosTag("Vp", Pos.MainVerb, Pos.Participle)); //main verb participle
        ITALIAN_TANL.addTag(new PosTag("Vpp", Pos.MainVerb, Pos.PresentParticiple)); //main verb participle present
        ITALIAN_TANL.addTag(new PosTag("Vps", Pos.MainVerb, Pos.PastParticiple)); //main verb participle past
        ITALIAN_TANL.addTag(new PosTag("VAip", Pos.AuxiliaryVerb, Pos.IndicativeVerb, Pos.PresentParticiple)); //auxiliary verb indicative present
        ITALIAN_TANL.addTag(new PosTag("VAip3", Pos.AuxiliaryVerb, Pos.IndicativeVerb, Pos.PresentParticiple)); //auxiliary verb indicative present 3rd perso
        ITALIAN_TANL.addTag(new PosTag("VAii", Pos.AuxiliaryVerb, Pos.IndicativeVerb)); //auxiliary verb indicative imperfect
        ITALIAN_TANL.addTag(new PosTag("VAii3", Pos.AuxiliaryVerb, Pos.IndicativeVerb)); //auxiliary verb indicative imperfect 3rd person
        ITALIAN_TANL.addTag(new PosTag("VAis", Pos.AuxiliaryVerb, Pos.IndicativeVerb, Pos.PastParticiple)); //auxiliary verb indicative past
        ITALIAN_TANL.addTag(new PosTag("VAis3", Pos.AuxiliaryVerb, Pos.IndicativeVerb, Pos.PastParticiple)); //auxiliary verb indicative past 3rd person
        ITALIAN_TANL.addTag(new PosTag("VAif", Pos.AuxiliaryVerb, Pos.IndicativeVerb, Pos.FutureParticle)); //auxiliary verb indicative future
        ITALIAN_TANL.addTag(new PosTag("VAif3", Pos.AuxiliaryVerb, Pos.IndicativeVerb, Pos.FutureParticle)); //auxiliary verb indicative future 3rd person
        ITALIAN_TANL.addTag(new PosTag("VAm", Pos.AuxiliaryVerb, Pos.ImperativeVerb)); //auxiliary verb imperative
        ITALIAN_TANL.addTag(new PosTag("VAcp", LexicalCategory.Conjuction, Pos.AuxiliaryVerb, Pos.PresentParticiple)); //auxiliary verb conjunctive present
        ITALIAN_TANL.addTag(new PosTag("VAcp3", LexicalCategory.Conjuction, Pos.AuxiliaryVerb, Pos.PresentParticiple)); //auxiliary verb conjunctive present 3rd person
        ITALIAN_TANL.addTag(new PosTag("VAci", LexicalCategory.Conjuction, Pos.AuxiliaryVerb)); //auxiliary verb conjunctive imperfect
        ITALIAN_TANL.addTag(new PosTag("VAci3", LexicalCategory.Conjuction, Pos.AuxiliaryVerb)); //auxiliary verb conjunctive imperfect 3rd person
        ITALIAN_TANL.addTag(new PosTag("VAd", Pos.AuxiliaryVerb, Pos.PresentParticiple)); //auxiliary verb conditional
        ITALIAN_TANL.addTag(new PosTag("VAdp", Pos.AuxiliaryVerb, Pos.PresentParticiple, Pos.ConditionalParticiple)); //auxiliary verb conditional present, other than 3rd person
        ITALIAN_TANL.addTag(new PosTag("VAdp3", Pos.AuxiliaryVerb, Pos.PresentParticiple, Pos.ConditionalParticiple)); //auxiliary verb conditional present 3rd person
        ITALIAN_TANL.addTag(new PosTag("VAf", Pos.AuxiliaryVerb, Pos.InfinitiveParticle)); //auxiliary verb infinite
        ITALIAN_TANL.addTag(new PosTag("VAg", Pos.AuxiliaryVerb, Pos.Gerund)); //main verb gerundive
        ITALIAN_TANL.addTag(new PosTag("VAp", Pos.AuxiliaryVerb, Pos.Participle)); //main verb gerundive
        ITALIAN_TANL.addTag(new PosTag("VApp", Pos.AuxiliaryVerb, Pos.PresentParticiple)); //auxiliary verb participle present
        ITALIAN_TANL.addTag(new PosTag("VAps", Pos.AuxiliaryVerb, Pos.PastParticiple)); //auxiliary verb participle past
        ITALIAN_TANL.addTag(new PosTag("VMip", Pos.ModalVerb, Pos.IndicativeVerb, Pos.PresentParticiple)); //modal verb indicative present
        ITALIAN_TANL.addTag(new PosTag("VMip3", Pos.ModalVerb, Pos.IndicativeVerb, Pos.PresentParticiple)); //modal verb indicative present 3rd person
        ITALIAN_TANL.addTag(new PosTag("VMii", Pos.ModalVerb, Pos.IndicativeVerb)); //modal verb indicative imperfect
        ITALIAN_TANL.addTag(new PosTag("VMii3", Pos.ModalVerb, Pos.IndicativeVerb)); //modal verb indicative imperfect 3rd person
        ITALIAN_TANL.addTag(new PosTag("VMis", Pos.ModalVerb, Pos.IndicativeVerb, Pos.PastParticiple)); //modal verb indicative past
        ITALIAN_TANL.addTag(new PosTag("VMis3", Pos.ModalVerb, Pos.IndicativeVerb, Pos.PastParticiple)); //modal verb indicative past 3rd person
        ITALIAN_TANL.addTag(new PosTag("VMif", Pos.ModalVerb, Pos.IndicativeVerb, Pos.FutureParticle)); //modal verb indicative future
        ITALIAN_TANL.addTag(new PosTag("VMif3", Pos.ModalVerb, Pos.IndicativeVerb, Pos.FutureParticle)); //modal verb indicative future 3rd person
        ITALIAN_TANL.addTag(new PosTag("VMm", Pos.ModalVerb)); //modal verb imperative
        ITALIAN_TANL.addTag(new PosTag("VMcp", LexicalCategory.Conjuction, Pos.ModalVerb, Pos.PresentParticiple)); //modal verb conjunctive present
        ITALIAN_TANL.addTag(new PosTag("VMcp3", LexicalCategory.Conjuction, Pos.ModalVerb, Pos.PresentParticiple)); //modal verb conjunctive present 3rd person
        ITALIAN_TANL.addTag(new PosTag("VMci", LexicalCategory.Conjuction, Pos.ModalVerb)); //modal verb conjunctive imperfect
        ITALIAN_TANL.addTag(new PosTag("VMci3", LexicalCategory.Conjuction, Pos.ModalVerb)); //modal verb conjunctive imperfect 3rd person
        ITALIAN_TANL.addTag(new PosTag("VMd", Pos.ModalVerb, Pos.ConditionalVerb)); //modal verb conditional
        ITALIAN_TANL.addTag(new PosTag("VMdp", Pos.ModalVerb, Pos.ConditionalVerb, Pos.PresentParticiple)); //modal verb conditional present, other than 3rd person
        ITALIAN_TANL.addTag(new PosTag("VMdp3", Pos.ModalVerb, Pos.ConditionalVerb, Pos.PresentParticiple)); //modal verb conditional present, 3rd person
        ITALIAN_TANL.addTag(new PosTag("VMf", Pos.ModalVerb, Pos.InfinitiveParticle)); //modal verb infinite
        ITALIAN_TANL.addTag(new PosTag("VMg", Pos.ModalVerb, Pos.Gerund)); //modal verb gerundive
        ITALIAN_TANL.addTag(new PosTag("VMp", Pos.ModalVerb, Pos.Participle)); //modal verb participle
        ITALIAN_TANL.addTag(new PosTag("VMpp", Pos.ModalVerb, Pos.Participle, Pos.PresentParticiple)); //modal verb participle present
        ITALIAN_TANL.addTag(new PosTag("VMps", Pos.ModalVerb, Pos.Participle, Pos.PastParticiple)); //modal verb participle past
        ITALIAN_TANL.addTag(new PosTag("X", LexicalCategory.Residual)); //residual class    }
    }

    public LanguageItalian() {
        super(ITALIAN, ITALIAN_TANL, "it-sent.bin", "it-token.bin", "it-pos-maxent.bin");
    }

}
