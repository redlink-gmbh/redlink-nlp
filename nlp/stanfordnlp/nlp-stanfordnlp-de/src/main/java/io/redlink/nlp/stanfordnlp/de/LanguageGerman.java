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

package io.redlink.nlp.stanfordnlp.de;


import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.international.negra.NegraPennLanguagePack;
import io.redlink.nlp.model.dep.RelTag;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.phrase.PhraseCategory;
import io.redlink.nlp.model.phrase.PhraseTag;
import io.redlink.nlp.model.pos.LexicalCategory;
import io.redlink.nlp.model.pos.Pos;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.tag.TagSet;
import io.redlink.nlp.stanfordnlp.StanfordNlpPipeline;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import static java.util.Locale.GERMAN;

/**
 * Implementation of English-specific tools for natural language processing.
 *
 * @author rupert.westenthaler@redlink.co
 */
@Component
@ConditionalOnClass({AnnotationPipeline.class, AnnotatorPool.class})
@EnableConfigurationProperties(LanguageGermanConfiguration.class)
public class LanguageGerman extends StanfordNlpPipeline {

    /*
     * Links to the STTS model as defined by the
     * <a herf="http://nlp2rdf.lod2.eu/olia/">OLIA</a> Ontology.
     * @see German#STTS
     */
    private static final TagSet<PosTag> GERMAN_STTS = new TagSet<PosTag>(
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
        GERMAN_STTS.addTag(new PosTag("PDS", Pos.DemonstrativePronoun, Pos.SubstitutivePronoun));
        GERMAN_STTS.addTag(new PosTag("PDAT", Pos.DemonstrativePronoun, Pos.AttributivePronoun));
        GERMAN_STTS.addTag(new PosTag("PIS", Pos.SubstitutivePronoun, Pos.IndefinitePronoun));
        GERMAN_STTS.addTag(new PosTag("PIAT", Pos.AttributivePronoun, Pos.IndefinitePronoun));
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
        GERMAN_STTS.addTag(new PosTag("$[", Pos.ParentheticalPunctuation));
        //Normal nouns in named entities (not in stts 1999)
        GERMAN_STTS.addTag(new PosTag("NNE", Pos.ProperNoun)); //TODO maybe map to common non
    }

    private static final TagSet<NerTag> NER_TAG_SET = new TagSet<NerTag>("German NER Tagset", "de");

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

        NER_TAG_SET.addTag(new NerTag("MISC", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("misc", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("B-MISC", NerTag.NAMED_ENTITY_MISC));
        NER_TAG_SET.addTag(new NerTag("I-MISC", NerTag.NAMED_ENTITY_MISC));
    }

    private static final TagSet<PhraseTag> GERMAN_NEGRA = new TagSet<>("Negra Phrase Tags", "de");

    static {
        GERMAN_NEGRA.addTag(new PhraseTag("AA")); //superlative phrase with "am"
        GERMAN_NEGRA.addTag(new PhraseTag("AP", PhraseCategory.AdjectivePhrase)); //adjective phrase
        GERMAN_NEGRA.addTag(new PhraseTag("AVP", PhraseCategory.AdverbPhrase)); //adverbial phrase
        GERMAN_NEGRA.addTag(new PhraseTag("CAC", PhraseCategory.PrepositionalPhrase, true)); //coordinated adpositions (prepositions or postpositions, APPR, APZR, APPO)
        GERMAN_NEGRA.addTag(new PhraseTag("CAP", PhraseCategory.AdjectivePhrase, true)); //coordinated adjektive phrase
        GERMAN_NEGRA.addTag(new PhraseTag("CAVP", PhraseCategory.AdverbPhrase, true)); //coordinated adverbial phrase
        GERMAN_NEGRA.addTag(new PhraseTag("CCP", PhraseCategory.ConjunctionPhrase, true)); //coordinated complementiser
        GERMAN_NEGRA.addTag(new PhraseTag("CH")); //chunk
        GERMAN_NEGRA.addTag(new PhraseTag("CNP", PhraseCategory.NounPhrase, true));
        GERMAN_NEGRA.addTag(new PhraseTag("CO", true)); //coordination
        GERMAN_NEGRA.addTag(new PhraseTag("CPP", PhraseCategory.PrepositionalPhrase, true)); //coordinated adpositional phrase
        GERMAN_NEGRA.addTag(new PhraseTag("CS", PhraseCategory.Sentence, true)); //coordinated sentence
        GERMAN_NEGRA.addTag(new PhraseTag("CVP", PhraseCategory.VerbPhrase, true)); //coordinated verb phrase (non-finite)
        GERMAN_NEGRA.addTag(new PhraseTag("CVZ", PhraseCategory.InfinitiveVerbPhrase, true)); //coordinated zu-marked infinitive
        GERMAN_NEGRA.addTag(new PhraseTag("DL")); //discourse level constituent
        GERMAN_NEGRA.addTag(new PhraseTag("ISU")); //idiosyncratic unit
        GERMAN_NEGRA.addTag(new PhraseTag("MPN", PhraseCategory.NounPhrase)); //multi-word proper noun
        GERMAN_NEGRA.addTag(new PhraseTag("MTA", PhraseCategory.AdjectivePhrase)); //multi-token adjective
        GERMAN_NEGRA.addTag(new PhraseTag("NM", PhraseCategory.QuantifierPhrase)); //multi-token number
        GERMAN_NEGRA.addTag(new PhraseTag("NP", PhraseCategory.NounPhrase)); //noun phrase
        GERMAN_NEGRA.addTag(new PhraseTag("PP", PhraseCategory.PrepositionalPhrase)); //adpositional phrase
        GERMAN_NEGRA.addTag(new PhraseTag("QL")); //quasi-language
        GERMAN_NEGRA.addTag(new PhraseTag("S", PhraseCategory.Sentence)); //sentence
        GERMAN_NEGRA.addTag(new PhraseTag("VP", PhraseCategory.VerbPhrase)); //verb phrase
        GERMAN_NEGRA.addTag(new PhraseTag("VZ", PhraseCategory.InfinitiveVerbPhrase)); //zu-marked infinitive
        GERMAN_NEGRA.addTag(new PhraseTag("NUR", PhraseCategory.Phrase)); //Non Unary Root (see https://mailman.stanford.edu/pipermail/parser-user/2013-April/002383.html)

    }


    private static final Map<String, String> DEFAULT_CONF;

    static {
        Map<String, String> dc = new HashMap<>();
        dc.put("parse.keepPunct", "false");
        dc.put("ner.useSUTime", "false");
        dc.put("tokenize.language", "de");
        dc.put("ner.applyNumericClassifiers", "false");
        DEFAULT_CONF = Collections.unmodifiableMap(dc);
    }


    /**
     * Instantiates a German NLP processing pipeline using the parsed configuration
     */
    public LanguageGerman(LanguageGermanConfiguration config) {
        super("default", GERMAN);
        setCaseSensitive(config.isCasesensitive());

        //Construct the Properties based on the configuration
        Properties props;
        if (config.isDefaults()) {
            Properties defaultProps = new Properties();
            try {
                defaultProps.load(LanguageGerman.class.getClassLoader().getResourceAsStream("StanfordCoreNLP-german.properties"));
            } catch (IOException e) {
                //Can only happen if the German model dependency is missing or the property file is
                //renamed after a version update of StanfordNLP
                throw new IllegalStateException("Unable to load default properties for German Stanford "
                        + "NLP Models ('StanfordCoreNLP-german.properties')", e);
            }
            props = new Properties(defaultProps);
        } else {
            props = new Properties();
        }
        if (StringUtils.isNotBlank(config.getAnnotators())) {
            log.info(" - set annotators pipeline to '{}'", config.getAnnotators());
            props.setProperty("annotators", config.getAnnotators());
        } else if (!config.isDefaults()) {
            throw new IllegalStateException("The 'nlp.stanfordnlp.de.annotators' MUST BE set to an none empty value if "
                    + "'nlp.stanfordnlp.de.defaults' is disabled!");
        } else {
            log.info(" - use default annotators pipeline '{}'", props.getProperty("annotators"));
        }
        if (StringUtils.isNotBlank(config.getPosModel())) {
            log.info(" - set custom 'pos.model': {}", config.getPosModel());
            props.setProperty("pos.model", config.getPosModel());
        }
        if (StringUtils.isNotBlank(config.getNerModel())) {
            log.info(" - set custom 'ner.model': {}", config.getNerModel());
            props.setProperty("ner.model", config.getNerModel());
        }
        if (StringUtils.isNotBlank(config.getParseModel())) {
            log.info(" - set custom 'parse.model': {}", config.getParseModel());
            props.setProperty("parse.model", config.getParseModel());
        }
        props.setProperty("parse.maxlen", String.valueOf(config.getParseMaxLen()));
        //apply defaults (if keys are not present)
        for (Entry<String, String> dc : DEFAULT_CONF.entrySet()) {
            if (props.getProperty(dc.getKey()) == null) {
                props.setProperty(dc.getKey(), dc.getValue());
            }
        }
        setProperties(props);
    }

    @Override
    protected TagSet<PosTag> getPosTagset() {
        return GERMAN_STTS;
    }

    @Override
    protected TagSet<NerTag> getNerTagset() {
        return NER_TAG_SET;
    }

    @Override
    protected TagSet<PhraseTag> getPhraseTagset() {
        return GERMAN_NEGRA;
    }

    @Override
    protected TagSet<RelTag> getRelTagset() {
        return null; //not supported
    }

    @Override
    public TreebankLanguagePack getLanguagePack() {
        return new NegraPennLanguagePack();
    }
}
