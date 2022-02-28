/*
 * Copyright (c) 2016-2022 Redlink GmbH.
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
package io.redlink.nlp.regex.ner;

import io.redlink.nlp.model.SpanCollection;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.regex.ner.RegexNerProcessor.NamedEntity;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Adaption of the TrainDetector to be a RegexNamedEntityFactory to be used for Unit Testing.
 */
@Component
public class TrainDetector extends RegexNamedEntityFactory {

    public static final NerTag TRAIN_TAG = new NerTag("train", "train");

    //TODO: move to a different module!
    private static final String[] prefixes = {
            // from http://kursbuch.bahn.de/hafas-res/img/kbview/ContentPDFs/zeichenerkl.pdf
            "CNL", //CityNightLine
            "EC", // Eurocity
            "EN", // Euronight
            "EST", // Eurostar
            "IC", // Intercity
            "ICE", // Intercity-Express
            "IR", // Interregio (unofficial)
            "IRE", // Interregio-Express
            "rj", // railjet
            "RB", // Regionalbahn
            "RE", // Regional-Express
            "S", // S-Bahn
            "D", // Schnellzuh
            "STR", // Straßenbahn
            "TGV", // Train à Grande Vitesse
    };


    private final Pattern pattern;

    public TrainDetector() {
        pattern = Pattern.compile(
                "\\b(" + StringUtils.join(prefixes, "|") + ")(\\s*|-)?(\\d{1,5})+\\b",
                Pattern.CASE_INSENSITIVE
        );
    }

    @Override
    protected NamedEntity createNamedEntity(String patternName, MatchResult match) {
        log.debug("Create Train Named Entity for {}", match);
        if (StringUtils.isBlank(match.group())) return null;
        final String train = String.format("%S %s", match.group(1), StringUtils.defaultIfBlank(match.group(3), "")).trim();

        final NamedEntity ne = new NamedEntity(match.start(), match.end(), TRAIN_TAG);
        ne.setConfidence(1f);
        if (!match.group(0).equals(train)) {
            ne.setLemma(train);
        }
        return ne;
    }

    @Override
    protected List<NamedPattern> getRegexes(SpanCollection section, String lang) {
        return Collections.singletonList(new NamedPattern("trains", pattern)); //use for all languages
    }

}
