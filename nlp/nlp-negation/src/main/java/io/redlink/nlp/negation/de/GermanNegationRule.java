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

package io.redlink.nlp.negation.de;

import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.pos.Pos;
import io.redlink.nlp.model.pos.PosSet;
import io.redlink.nlp.model.util.NlpUtils;
import io.redlink.nlp.negation.DefaultNegationRule;
import io.redlink.nlp.negation.NegationRule;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * For German we also want to assume {@link Pos#IndefinitePronoun}s that
 * represent "kein(e|es|..)" as negation
 *
 * @author Rupert Westenthaler
 */
@Component
public class GermanNegationRule extends DefaultNegationRule implements NegationRule {

    private static final PosSet INDEV_PRONOUN = PosSet.of(Pos.IndefinitePronoun);
    private static final PosSet PREPOS = PosSet.of(Pos.Preposition);

    public GermanNegationRule() {
        super();
    }

    @Override
    public boolean isNegation(Token token) {
        if (!super.isNegation(token)) {
            String word = token.getSpan().toLowerCase(Locale.GERMAN);
            //keine <Token>
            if (NlpUtils.isOfPos(token, INDEV_PRONOUN)) {
                if (word.startsWith("kein")) {
                    return true;
                }
            }
            //exclusive <Token>
            if (word.matches("ex(c|k)l(\\.?$|usiv)")) {
                return true;
            }
            //ohne <Token>
            if (NlpUtils.isOfPos(token, PREPOS)) {
                if (word.startsWith("ohne")) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getLanguage() {
        return "de";
    }

}
