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

package io.redlink.nlp.opennlp.de;

import static java.util.Locale.GERMAN;

import org.springframework.stereotype.Service;

import io.redlink.nlp.model.pos.LexicalCategory;
import io.redlink.nlp.model.pos.Pos;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.tag.TagSet;
import io.redlink.nlp.opennlp.pos.OpenNlpLanguageModel;

/**
 * Implementation of English-specific tools for natural language processing.
 *
 * @author rupert.westenthaler@redlink.co
 */
@Service
public class LanguageGerman extends OpenNlpLanguageModel {
   /*
    * Links to the STTS model as defined by the 
    * <a herf="http://nlp2rdf.lod2.eu/olia/">OLIA</a> Ontology.
    * @see German#STTS
    */
   public static final TagSet<PosTag> GERMAN_STTS = new TagSet<PosTag>(
           "STTS", "de");

       static {
           //TODO: define constants for annotation model and linking model
           GERMAN_STTS.getProperties().put("olia.annotationModel", 
               "http://purl.org/olia/stts.owl");
           GERMAN_STTS.getProperties().put("olia.linkingModel", 
               "http://purl.org/olia/stts-link.rdf");
           
           GERMAN_STTS.addTag(new PosTag("ADJA", Pos.AttributiveAdjective));
           GERMAN_STTS.addTag(new PosTag("ADJD", Pos.PredicativeAdjective));
           GERMAN_STTS.addTag(new PosTag("ADV", LexicalCategory.Adverb));
           GERMAN_STTS.addTag(new PosTag("APPR", Pos.Preposition));
           GERMAN_STTS.addTag(new PosTag("APPRART", Pos.FusedPrepArt));
           GERMAN_STTS.addTag(new PosTag("APPO", Pos.Postposition));
           GERMAN_STTS.addTag(new PosTag("APZR", Pos.Circumposition));
           GERMAN_STTS.addTag(new PosTag("ART", Pos.Article));
           GERMAN_STTS.addTag(new PosTag("CARD", Pos.CardinalNumber));
           GERMAN_STTS.addTag(new PosTag("FM", Pos.Foreign));
           GERMAN_STTS.addTag(new PosTag("ITJ", LexicalCategory.Interjection));
           GERMAN_STTS.addTag(new PosTag("KOUI", Pos.SubordinatingConjunction));
           GERMAN_STTS.addTag(new PosTag("KOUS", Pos.SubordinatingConjunctionWithFiniteClause));
           GERMAN_STTS.addTag(new PosTag("KON", Pos.CoordinatingConjunction));
           GERMAN_STTS.addTag(new PosTag("KOKOM", Pos.ComparativeParticle));
           GERMAN_STTS.addTag(new PosTag("NN", Pos.CommonNoun));
           GERMAN_STTS.addTag(new PosTag("NE", Pos.ProperNoun));
           GERMAN_STTS.addTag(new PosTag("PDS", Pos.DemonstrativePronoun,Pos.SubstitutivePronoun));
           GERMAN_STTS.addTag(new PosTag("PDAT", Pos.DemonstrativePronoun, Pos.AttributivePronoun));
           GERMAN_STTS.addTag(new PosTag("PIS", Pos.SubstitutivePronoun, Pos.IndefinitePronoun));
           GERMAN_STTS.addTag(new PosTag("PIAT",  Pos.AttributivePronoun, Pos.IndefinitePronoun));
           GERMAN_STTS.addTag(new PosTag("PIDAT", Pos.AttributivePronoun, Pos.IndefinitePronoun));
           GERMAN_STTS.addTag(new PosTag("PPER", Pos.PersonalPronoun));
           GERMAN_STTS.addTag(new PosTag("PPOSS", Pos.SubstitutivePronoun, Pos.PossessivePronoun));
           GERMAN_STTS.addTag(new PosTag("PPOSAT", Pos.AttributivePronoun, Pos.PossessivePronoun));
           GERMAN_STTS.addTag(new PosTag("PRELS", Pos.SubstitutivePronoun, Pos.RelativePronoun));
           GERMAN_STTS.addTag(new PosTag("PRELAT", Pos.AttributivePronoun, Pos.RelativePronoun));
           GERMAN_STTS.addTag(new PosTag("PRF", Pos.ReflexivePronoun));
           GERMAN_STTS.addTag(new PosTag("PWS", Pos.SubstitutivePronoun, Pos.InterrogativePronoun));
           GERMAN_STTS.addTag(new PosTag("PWAT", Pos.AttributivePronoun, Pos.InterrogativePronoun));
           GERMAN_STTS.addTag(new PosTag("PWAV", LexicalCategory.Adverb, Pos.RelativePronoun, Pos.InterrogativePronoun));
           GERMAN_STTS.addTag(new PosTag("PAV", Pos.PronominalAdverb));
           //Tiger-STTS for PAV
           GERMAN_STTS.addTag(new PosTag("PROAV", Pos.PronominalAdverb));
           GERMAN_STTS.addTag(new PosTag("PTKA", Pos.AdjectivalParticle));
           GERMAN_STTS.addTag(new PosTag("PTKANT", Pos.Particle));
           GERMAN_STTS.addTag(new PosTag("PTKNEG", Pos.NegativeParticle));
           GERMAN_STTS.addTag(new PosTag("PTKVZ", Pos.VerbalParticle));
           GERMAN_STTS.addTag(new PosTag("PTKZU", Pos.Particle)); //particle "zu"  e.g. "zu [gehen]".
           GERMAN_STTS.addTag(new PosTag("TRUNC", Pos.Abbreviation)); //e.g. An- [und Abreise] 
           GERMAN_STTS.addTag(new PosTag("VVIMP", Pos.ImperativeVerb));
           GERMAN_STTS.addTag(new PosTag("VVINF", Pos.Infinitive));
           GERMAN_STTS.addTag(new PosTag("VVFIN", Pos.FiniteVerb));
           GERMAN_STTS.addTag(new PosTag("VVIZU", Pos.Infinitive));
           GERMAN_STTS.addTag(new PosTag("VVPP", Pos.PastParticiple));
           GERMAN_STTS.addTag(new PosTag("VAFIN", Pos.FiniteVerb, Pos.AuxiliaryVerb));
           GERMAN_STTS.addTag(new PosTag("VAIMP", Pos.AuxiliaryVerb, Pos.ImperativeVerb));
           GERMAN_STTS.addTag(new PosTag("VAINF", Pos.AuxiliaryVerb, Pos.Infinitive));
           GERMAN_STTS.addTag(new PosTag("VAPP", Pos.PastParticiple, Pos.AuxiliaryVerb));
           GERMAN_STTS.addTag(new PosTag("VMFIN", Pos.FiniteVerb, Pos.ModalVerb));
           GERMAN_STTS.addTag(new PosTag("VMINF", Pos.Infinitive, Pos.ModalVerb));
           GERMAN_STTS.addTag(new PosTag("VMPP", Pos.PastParticiple, Pos.ModalVerb));
           GERMAN_STTS.addTag(new PosTag("XY", Pos.Symbol)); //non words (e.g. H20, 3:7 ...)
           GERMAN_STTS.addTag(new PosTag("$.", Pos.Point));
           GERMAN_STTS.addTag(new PosTag("$,", Pos.Comma));
           GERMAN_STTS.addTag(new PosTag("$(", Pos.ParentheticalPunctuation));
           //Normal nouns in named entities (not in stts 1999)
           GERMAN_STTS.addTag(new PosTag("NNE", Pos.ProperNoun)); //TODO maybe map to common non
       }
       
    public LanguageGerman() {
        super(GERMAN,GERMAN_STTS,"de-sent.bin","de-token.bin","de-pos-maxent.bin");
    }

}
