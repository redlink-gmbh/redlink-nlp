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

package io.redlink.nlp.negation;

import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.pos.PosSet;
import io.redlink.nlp.model.util.NlpUtils;

public class DefaultNegationRule implements NegationRule {

    public static final DefaultNegationRule INSTANCE = new DefaultNegationRule();

    protected DefaultNegationRule() {
    }

    @Override
    public boolean isNegation(Token token) {
        return NlpUtils.isOfPos(token, PosSet.NEGATION);
    }

    @Override
    public String getLanguage() {
        return null;
    }

}
