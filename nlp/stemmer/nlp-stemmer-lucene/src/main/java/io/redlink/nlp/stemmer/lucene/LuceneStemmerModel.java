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

package io.redlink.nlp.stemmer.lucene;

import io.redlink.nlp.stemmer.StemmerModel;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.SnowballProgram;

/**
 * Stemmer model
 *
 * @author rupert.westenthaler@redlink.co
 */

public class LuceneStemmerModel implements StemmerModel {

    private final Logger log = LoggerFactory.getLogger(LuceneStemmerModel.class);


    /**
     * The stemmer class used for the {@link #language}
     */
    private final Class<? extends SnowballProgram> stemmerClass;
    /**
     * the stemmer as thread local var
     */
    private final ThreadLocal<SnowballProgram> stemmer;

    private Locale language;

    protected LuceneStemmerModel(Locale language, Class<? extends SnowballProgram> clazz) {
        assert language != null;
        this.language = language;
        this.stemmerClass = clazz;
        log.info("  ... loading {} Snowball Stemmer", stemmerClass);
        stemmer = new ThreadLocal<SnowballProgram>() {
            @Override
            protected SnowballProgram initialValue() {
                try {
                    return stemmerClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalStateException("Unable to create Snowball Stemmer instance!");
                }
            }
        };
    }

    public final String getName() {
        return (language == null ? "default language" : language.getDisplayLanguage(language)) + " Stemmer";
    }


    public Locale getLocale() {
        return language;
    }

    /**
     * Perform stemming on the given token.
     */
    @Override
    public final String stemToken(final String token) {
        SnowballProgram s = stemmer.get();
        s.setCurrent(token);
        s.stem();
        return s.getCurrent();
    }
}
