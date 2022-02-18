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

package io.redlink.nlp.stopword;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Spring Component that cares about the loading and management of stopword lists.
 * @author Rupert Westenthaler
 *
 */
@Component
public class StopwordListRegistry {

    private final Logger log = LoggerFactory.getLogger(StopwordListRegistry.class);

    /**
     * The pattern used to load stopword lists for {@link Locale}s from the classpath
     */
    public static final String STOPWORD_LIST_NAME = "lang/stopwords-%s.txt"; 

    /**
     * Stopwords for different languages
     */
    private Map<Key,Set<String>> stopwords = new HashMap<>();
    private final ReadWriteLock stopwordsLock = new ReentrantReadWriteLock();

    /**
     * Initializes the OpenNLP language model based on the parsed parameters
     * @param sentModelResource Required sentence model resource (loaded via classpath)
     * @param tokenModelResource optional tokenizer model resource (loaded via classpath).
     * If <code>null</code> the {@link SimpleTokenizer} will be used.
     * @param posModelResource Required POS tagging model resource (loaded via classpath)
     * @param stemmerClass Required {@link SnowballStemmer} class
     * @param stopwordListResource Required stopword list resource (loaded via classpath)
     * @throws LanguageModelException if the initialization fails
     */
    protected final Set<String> init(final Locale locale) {
        //NOTE tokenModelResource may be null (to use the SimpleTokenizer
        log.debug("> loading stopwords for {}", locale == null ? "default language" : locale.getDisplayLanguage());

        log.info("  ... loading Stopwords");
        String stopwordListResource = String.format(STOPWORD_LIST_NAME, 
                locale == null ? "default" : locale.getLanguage());
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(stopwordListResource)){
            if(in == null){
                log.warn("Unable to load stopword list for language {} (resource: '{}') via classpath", locale, stopwordListResource);
                return null;
            } else {
                LineIterator stopwordIt = IOUtils.lineIterator(in,"UTF-8");
                Set<String> stopwords = new HashSet<>();
                while(stopwordIt.hasNext()){
                    String stopword = stopwordIt.nextLine();
                    if(stopword != null){
                        stopword = stopword.toLowerCase(Locale.GERMAN).trim();
                        if(!stopword.isEmpty()) {
                            stopwords.add(stopword);
                        }
                    }
                }
                return stopwords.isEmpty() ? null : Collections.unmodifiableSet(stopwords);
            }
        } catch (IOException e) {
            log.warn("Unable to load Stopwords for Locale {} (message: {})", locale, e.getMessage());
            if(log.isDebugEnabled()){
                log.debug("Exception ",e);
            }
            return null;
        }
    }

    /**
     * Getter for the stopwords for a given language.
     * @param locale the locale or <code>null</code> for stopwords to be used
     * in case the language is unknown.
     * @return
     */
    public Set<String> getStopwords(Locale locale, boolean caseSensitive){
        Key key = Key.build(locale, caseSensitive);
        stopwordsLock.readLock().lock();
        try {
            if(stopwords.containsKey(key)){ //check contains as map contais null values
                return stopwords.get(key);
            }
        } finally {
            stopwordsLock.readLock().unlock();
        }
        stopwordsLock.writeLock().lock();
        try {
            if(stopwords.containsKey(key)){ //check contains as map contais null values
                return stopwords.get(key);
            } else {
                Set<String> localeStopwords = init(locale);
                stopwords.put(Key.build(locale, false), localeStopwords);
                Set<String> insensitiveLocaleStopwords = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                if(localeStopwords != null) {
                    insensitiveLocaleStopwords.addAll(localeStopwords);
                } //no stopwords for this combination
                stopwords.put(Key.build(locale, true), insensitiveLocaleStopwords);
                return stopwords.get(key);
            }
        } finally {
            stopwordsLock.writeLock().unlock();
        }
    }
    /**
     * Internally used as key to manage stopwords for locale and case sensitivity
     */
    private static class Key {
        private final Locale locale;
        private final boolean caseSensitive;
        
        public static Key build(Locale locale, boolean caseSensitive) {
            return new Key(locale, caseSensitive);
        }
        
        private Key(Locale locale, boolean caseSensitive) {
            this.locale = locale;
            this.caseSensitive = caseSensitive;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (caseSensitive ? 1231 : 1237);
            result = prime * result + ((locale == null) ? 0 : locale.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (caseSensitive != other.caseSensitive)
                return false;
            if (locale == null) {
                if (other.locale != null)
                    return false;
            } else if (!locale.equals(other.locale))
                return false;
            return true;
        }
        
    }
    
}
