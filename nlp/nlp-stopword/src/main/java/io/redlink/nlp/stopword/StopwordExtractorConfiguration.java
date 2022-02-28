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

package io.redlink.nlp.stopword;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = StopwordExtractorConfiguration.NLP_STOPWORD)
public class StopwordExtractorConfiguration {

    public static final String NLP_STOPWORD = "nlp.stopword";
    private static final String CONF_PREFIX = NLP_STOPWORD + '.';


    public static final String PROP_CASE_SENSITIVE = CONF_PREFIX + "caseSensitive";
    public static final boolean DEFAULT_CASE_SENSITIVE = false;

    /**
     * If enabled nouns, verbs and adjectives are never considered as stop words
     */
    public static final String PROP_USE_POS = CONF_PREFIX + "usePos";
    public static final boolean DEFAULT_USE_POS = true;

    private boolean caseSensitive = DEFAULT_CASE_SENSITIVE;

    private boolean usePos = DEFAULT_USE_POS;


    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isUsePos() {
        return usePos;
    }

    public void setUsePos(boolean usePos) {
        this.usePos = usePos;
    }

}
