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

package io.redlink.nlp.opennlp.def;


import io.redlink.nlp.opennlp.pos.OpenNlpLanguageModel;
import io.redlink.nlp.opennlp.pos.impl.RegexSentenceSplitter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.springframework.stereotype.Component;

/**
 * A simple Language Model that will use the {@link SimpleTokenizer} for
 * tokenization and the {@link RegexSentenceSplitter} for sentence splitting.
 * It registers itself for the <code>null</code> {@link Locale} and will
 * Therefore be used for languages where no dedicated language model is
 * available.
 *
 * @author Rupert Westenthaler
 */
@Component
public class LanguageDefault extends OpenNlpLanguageModel {

    /**
     * We can not support languages without whitespaces
     */
    private static final Set<String> UNSUPPORTED = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(Locale.CHINESE.getLanguage(),
                    Locale.KOREAN.getLanguage(), Locale.JAPANESE.getLanguage())));


    public LanguageDefault() {
        super(null, null, null, null, null);
    }

    /*
     * This overrides the supports method to only return false for languages without
     * whitespaces
     */
    @Override
    public boolean supports(String lang) {
        if (lang == null) {
            return true; //try to process documents with unknown language
        } else {
            String normLang = Locale.forLanguageTag(lang).getLanguage();
            String[] normLangParts = normLang.split("-_");
            return !UNSUPPORTED.contains(normLangParts[0]);
        }
    }

    /**
     * This is a fallback Model so it returns {@link Integer#MIN_VALUE} as ranking
     */
    @Override
    public int getModelRanking() {
        return Integer.MIN_VALUE;
    }

}
