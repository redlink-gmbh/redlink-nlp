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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.DanishStemmer;
import org.tartarus.snowball.ext.DutchStemmer;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FinnishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.GermanStemmer;
import org.tartarus.snowball.ext.HungarianStemmer;
import org.tartarus.snowball.ext.ItalianStemmer;
import org.tartarus.snowball.ext.NorwegianStemmer;
import org.tartarus.snowball.ext.PortugueseStemmer;
import org.tartarus.snowball.ext.RomanianStemmer;
import org.tartarus.snowball.ext.RussianStemmer;
import org.tartarus.snowball.ext.SpanishStemmer;
import org.tartarus.snowball.ext.SwedishStemmer;
import org.tartarus.snowball.ext.TurkishStemmer;

import io.redlink.nlp.stemmer.StemmerRegistry;

@Component
public class LuceneStemmerRegistry implements StemmerRegistry {
    
    private static final Map<String, Class<? extends SnowballProgram>> SNOWBALL_CONFIG;
    static { //TODO: investigate the use of Light/Minimal stemmers
        Map<String, Class<? extends SnowballProgram>> conf = new HashMap<>();
        conf.put("en", EnglishStemmer.class);
        conf.put("de", GermanStemmer.class);
        conf.put("da", DanishStemmer.class);
        conf.put("nl", DutchStemmer.class);
        conf.put("fi", FinnishStemmer.class);
        conf.put("fr", FrenchStemmer.class);
        conf.put("hu", HungarianStemmer.class);
        conf.put("it", ItalianStemmer.class);
        conf.put("no", NorwegianStemmer.class);
        conf.put("pt", PortugueseStemmer.class);
        conf.put("ru", RussianStemmer.class);
        conf.put("ro", RomanianStemmer.class);
        conf.put("es", SpanishStemmer.class);
        conf.put("sv", SwedishStemmer.class);
        conf.put("tr", TurkishStemmer.class);
        SNOWBALL_CONFIG = Collections.unmodifiableMap(conf);
    }
    
    
    private final Map<String, LuceneStemmerModel> stemmerModels;
    private final ReadWriteLock stemmerModelsLock;
    
    
    public LuceneStemmerRegistry() {
        stemmerModels = new HashMap<>();
        stemmerModelsLock = new ReentrantReadWriteLock();
    }
    
    
    /* (non-Javadoc)
     * @see io.redlink.nlp.stemmer.StemmerRegistry#getStemmerModel(java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public LuceneStemmerModel getStemmerModel(String lang) {
        lang = lang == null ? null : lang.toLowerCase(Locale.ROOT);
        LuceneStemmerModel model;
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
                        model = new LuceneStemmerModel(Locale.forLanguageTag(lang), stemmerClass);
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
