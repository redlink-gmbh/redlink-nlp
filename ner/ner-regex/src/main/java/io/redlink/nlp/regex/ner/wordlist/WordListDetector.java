/*
 * Copyright (c) 2016-2022 Redlink GmbH.
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
package io.redlink.nlp.regex.ner.wordlist;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.regex.ner.RegexNerDetector;

/**
 * Regex-Based detection of trains
 */
public abstract class WordListDetector extends RegexNerDetector {


    private final String lang;
    private final Locale locale;
    private final boolean caseSensitive;
    private Set<String> words;

    public WordListDetector(String name, NerTag type, Locale lang, boolean caseSensitive){
        super(name, type);
        this.locale = lang == null ? Locale.ROOT : lang;
        this.lang = lang == null ? null : lang.getLanguage().toLowerCase(Locale.ROOT).split("-_")[0];
        this.caseSensitive = caseSensitive;
    }

    @Override
    protected Map<String,List<Pattern>> initPatterns() throws IOException {
        Set<String> loadedWords = loadWords();
        SortedSet<String> sortedWords = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s2.compareTo(s1);
            }
        });
        for(String word : loadedWords){
            String pWord = StringUtils.trimToNull(word);
            if(pWord != null) {
                pWord = caseSensitive ? pWord : pWord.toLowerCase(locale);
                if(!sortedWords.contains(pWord)){
                    sortedWords.add(pWord);
                } else {
                    log.info(" - ignore duplicate lower case word: {} (word:{})", pWord, word);
                }
            } //ignore blank words
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        builder.append('(');
        for(String word : sortedWords){
            if(!first){
                builder.append('|');
            } else {
                first = false;
            }
            builder.append("\\b").append(Pattern.quote(word)).append("\\b");
        }
        builder.append(')');
        words = new HashSet<>(sortedWords);
        int flags = 0;
        if(!caseSensitive){
            flags = flags | Pattern.CASE_INSENSITIVE;
        }
        return Collections.singletonMap(lang, Collections.singletonList(Pattern.compile(builder.toString(), flags)));
    }
    
    protected abstract Set<String> loadWords() throws IOException;
    
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
 
    /**
     * Ensure that only matches are accepted that are actual words in the word list
     */
    @Override
    protected boolean acceptMatch(String word) {
        return words.contains(caseSensitive || word == null ? word : word.toLowerCase(locale));
    }

}
