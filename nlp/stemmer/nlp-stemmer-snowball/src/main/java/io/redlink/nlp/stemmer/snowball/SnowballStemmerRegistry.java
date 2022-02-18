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

package io.redlink.nlp.stemmer.snowball;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.danishStemmer;
import org.tartarus.snowball.ext.dutchStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.finnishStemmer;
import org.tartarus.snowball.ext.frenchStemmer;
import org.tartarus.snowball.ext.germanStemmer;
import org.tartarus.snowball.ext.hungarianStemmer;
import org.tartarus.snowball.ext.italianStemmer;
import org.tartarus.snowball.ext.norwegianStemmer;
import org.tartarus.snowball.ext.portugueseStemmer;
import org.tartarus.snowball.ext.romanianStemmer;
import org.tartarus.snowball.ext.russianStemmer;
import org.tartarus.snowball.ext.spanishStemmer;
import org.tartarus.snowball.ext.swedishStemmer;
import org.tartarus.snowball.ext.turkishStemmer;

import io.redlink.nlp.stemmer.StemmerRegistry;

@Component
public class SnowballStemmerRegistry implements StemmerRegistry {
    
    private static final Map<String, Class<? extends SnowballProgram>> SNOWBALL_CONFIG;
    static {
        Map<String, Class<? extends SnowballProgram>> conf = new HashMap<>();
        conf.put("en", englishStemmer.class);
        conf.put("de", germanStemmer.class);
        conf.put("da", danishStemmer.class);
        conf.put("nl", dutchStemmer.class);
        conf.put("fi", finnishStemmer.class);
        conf.put("fr", frenchStemmer.class);
        conf.put("hu", hungarianStemmer.class);
        conf.put("it", italianStemmer.class);
        conf.put("no", norwegianStemmer.class);
        conf.put("pt", portugueseStemmer.class);
        conf.put("ru", russianStemmer.class);
        conf.put("ro", romanianStemmer.class);
        conf.put("es", spanishStemmer.class);
        conf.put("sv", swedishStemmer.class);
        conf.put("tr", turkishStemmer.class);
        SNOWBALL_CONFIG = Collections.unmodifiableMap(conf);
    }
    
    
    private final Map<String, SnowballStemmerModel> stemmerModels;
    private final ReadWriteLock stemmerModelsLock;
    
    
    public SnowballStemmerRegistry() {
        stemmerModels = new HashMap<>();
        stemmerModelsLock = new ReentrantReadWriteLock();
    }
    
    
    /* (non-Javadoc)
     * @see io.redlink.nlp.stemmer.StemmerRegistry#getStemmerModel(java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public SnowballStemmerModel getStemmerModel(String lang) {
        lang = lang == null ? null : lang.toLowerCase(Locale.ROOT);
        SnowballStemmerModel model;
        stemmerModelsLock.readLock().lock();
        try {
            model = stemmerModels.get(lang);
        } finally {
            stemmerModelsLock.readLock().unlock();
        }
        if(model == null){
            @SuppressWarnings("rawtypes")
            Class stemmerClass = SNOWBALL_CONFIG.get(lang);
            if(stemmerClass != null){ //language supported
                stemmerModelsLock.writeLock().lock();
                try {
                    model = stemmerModels.get(lang); //other thread initialized it in the meantime
                    if(model == null){
                        model = new SnowballStemmerModel(Locale.forLanguageTag(lang), stemmerClass);
                        stemmerModels.put(lang, model);
                    }
                } finally {
                    stemmerModelsLock.writeLock().unlock();
                }
            } //else language not supported
        }
        return model;
    }
    
}
