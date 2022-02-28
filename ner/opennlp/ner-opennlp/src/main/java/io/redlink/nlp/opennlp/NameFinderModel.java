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

package io.redlink.nlp.opennlp;

import io.redlink.nlp.model.ner.NerTag;
import java.util.HashMap;
import java.util.Map;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

/**
 * Represents a configured OpenNLP {@link TokenNameFinderModel}.
 * <p>
 * During construction the name and type mappings are set. During the
 * activation the model is loaded. {@link ThreadLocal} {@link NameFinderME}
 * instances are created at runtime.
 *
 * @author Rupert Westenthaler
 */
public class NameFinderModel {

    private final Map<String, String> typeMappings = new HashMap<>();
    /**
     * The name of the model.
     */
    protected final String modelName;
    private TokenNameFinderModel model;
    private ThreadLocal<NameFinderME> nameFinder;

    public NameFinderModel(String modelName) {
        assert modelName != null;
        assert !modelName.isEmpty();
        this.modelName = modelName;
    }

    /**
     * Creates a NameFinderModel including some type mappings
     *
     * @param modelName    the name of the {@link TokenNameFinderModel}
     * @param typeMappings the type mappings formatted like
     *                     <code>[modelType1, type1, modelType2, type2, ... , modelTypeN, typeN]</code>
     */
    public NameFinderModel(String modelName, String... typeMappings) {
        this(modelName);
        assert typeMappings != null;
        assert typeMappings.length % 2 == 0;
        for (int i = 0; i < typeMappings.length - 1; i++) {
            addMapping(typeMappings[i++], typeMappings[i]);
        }
    }

    /**
     * Adds a type mapping for the model.
     *
     * @param modelType the type string used by the model
     * @param type      the type string used by the {@link NamedEntity#type}
     * @return Returns this instance
     */
    public NameFinderModel addMapping(String modelType, String type) {
        typeMappings.put(modelType, type);
        return this;
    }

    /**
     * Used by the {@link OpenNlpNerModel#activate()} method to set the
     * loaded model.
     *
     * @param model the model to set
     */
    void setModel(TokenNameFinderModel model) {
        assert model != null;
        this.model = model;
        this.nameFinder = new ThreadLocal<NameFinderME>() {
            @Override
            protected NameFinderME initialValue() {
                return new NameFinderME(NameFinderModel.this.model);
            }
        };
    }

    /**
     * Allows to reset the {@link #model} and {@link #nameFinder}
     */
    void reset() {
        this.model = null;
        this.nameFinder = null;
    }

    /**
     * Getter for the model.
     *
     * @return the model
     */
    public TokenNameFinderModel getModel() {
        return model;
    }

    /**
     * Getter for the name finder
     *
     * @return the name finder
     */
    public NameFinderME getNameFinder() {
        return nameFinder == null ? null : nameFinder.get();
    }

    @Override
    public String toString() {
        return modelName;
    }

    /**
     * Getter for the type based on the model type
     *
     * @param type the model type
     * @return the mapped type or <code>null</code> if no mapping for this type
     * is present. If <code>null</code> is parsed this will return
     * {@link Constants.Extraction#NAMED_ENTITY_UNKOWN}
     */
    public String getType(String type) {
        return type == null ? NerTag.NAMED_ENTITY_UNKOWN : typeMappings.get(type);
    }
}
