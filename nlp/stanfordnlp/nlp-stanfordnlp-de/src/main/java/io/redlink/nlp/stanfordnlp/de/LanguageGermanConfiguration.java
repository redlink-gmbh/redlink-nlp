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

package io.redlink.nlp.stanfordnlp.de;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="nlp.stanfordnlp.de")
public class LanguageGermanConfiguration {
    /**
     * Configuration property for defining the annoators pipeline for German
     */
    private String annotators = "tokenize, atSection, ssplit, pos, ner, parse";
    
    private boolean defaults = true;
    
    private String posModel;
    
    private String nerModel;
    
    private String parseModel;
    
    private int parseMaxLen = -1;
    
    private boolean casesensitive = true;

    public String getAnnotators() {
        return annotators;
    }

    public void setAnnotators(String annotators) {
        this.annotators = annotators;
    }

    public boolean isDefaults() {
        return defaults;
    }

    public void setDefaults(boolean defaults) {
        this.defaults = defaults;
    }

    public String getPosModel() {
        return posModel;
    }

    public void setPosModel(String posModel) {
        this.posModel = posModel;
    }

    public String getNerModel() {
        return nerModel;
    }

    public void setNerModel(String nerModel) {
        this.nerModel = nerModel;
    }

    public String getParseModel() {
        return parseModel;
    }

    public void setParseModel(String parseModel) {
        this.parseModel = parseModel;
    }

    public int getParseMaxLen() {
        return parseMaxLen;
    }

    public void setParseMaxLen(int parseMaxLen) {
        this.parseMaxLen = parseMaxLen;
    }

    public boolean isCasesensitive() {
        return casesensitive;
    }

    public void setCasesensitive(boolean casesensitive) {
        this.casesensitive = casesensitive;
    }

    
    
}
