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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A language specific configuration for OpenNLP based NER recognitions.
 * Subclasses need to register them self as {@link Service}s so that they can
 * get injected to the {@link OpenNlpNerProcessor}
 *
 * @author Rupert Westenthaler
 */
public abstract class OpenNlpNerModel {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Locale locale;
    private List<NameFinderModel> models;
    private boolean caseSensitive = true;

    private boolean activated;

    protected OpenNlpNerModel(Locale locale, NameFinderModel... models) {
        assert locale != null;
        assert models != null;
        assert models.length > 0;
        this.locale = locale;
        this.models = Arrays.asList(models);
        assert !this.models.contains(null);
    }

    /**
     * Needs to be set to false if the NER models are case less
     *
     * @param caseSensitive the case sensitive state
     */
    protected void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * If the NER Models are case sensitive or not
     *
     * @return the case sensitive state
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void activate() throws IOException {
        if (!activated) {
            synchronized (this) {
                if (!activated) {
                    log.info("> activating {}", getClass().getSimpleName());
                    ClassLoader cl = getClass().getClassLoader();
                    for (NameFinderModel model : models) {
                        log.info("  ... loading {}", model.modelName);
                        InputStream in = cl.getResourceAsStream(model.modelName);
                        if (in != null) {
                            try {
                                model.setModel(new TokenNameFinderModel(in));
                            } catch (InvalidFormatException e) {
                                throw new IOException("Unable to load OpenNLP Name Finder Model '"
                                        + model.modelName + "' for language '" + locale.getLanguage()
                                        + "' Message: " + e.getMessage(), e);
                            } catch (IOException e) {
                                throw new IOException("Unable to load OpenNLP Name Finder Model '"
                                        + model.modelName + "' for language '" + locale.getLanguage()
                                        + "' from Classpath!", e);
                            }
                        } else {
                            throw new IOException("Unable to load OpenNLP Name Finder Model '"
                                    + model.modelName + "' for language '" + locale.getLanguage()
                                    + "' via Classpath!", null);
                        }
                    }
                    activated = true;
                } else {
                    log.info(" > {} already activated", getClass().getSimpleName());
                }
            }
        } else {
            log.info(" > {} already activated", getClass().getSimpleName());
        }

    }

    /**
     * The NameFinder models
     */
    public List<NameFinderModel> getNameFinders() {
        return models;
    }

    public void deactivate() {
        synchronized (this) {
            log.info("> deactivate {}", getClass().getSimpleName());
            activated = false;
            for (NameFinderModel model : models) {
                model.reset();
            }
        }
    }

    public boolean isActive() {
        return activated;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * The language supported by the NerModel. This method
     * can be called before {@link #activate() activation}
     *
     * @return the ISO 639-1 language code (e.g. "en" for English)
     */
    public final String getLanguage() {
        Locale l = getLocale();
        return l == null ? null : l.getLanguage();
    }

    public String getName() {
        return "OpenNLP Named Entity Extraction support for " + locale.getLanguage()
                + " based on " + models.toString();
    }

}
