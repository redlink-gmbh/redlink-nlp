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
package io.redlink.nlp.regex.ner.vocab;

import io.redlink.nlp.model.SpanCollection;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.regex.ner.RegexNamedEntityFactory;
import io.redlink.nlp.regex.ner.RegexNerProcessor.NamedEntity;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;

/**
 * Regex-Based detection of trains
 */
public abstract class VocabularyDetector extends RegexNamedEntityFactory {


    private List<NamedPattern> patterns;
    private final String name;
    private final NerTag type;
    private final String lang;
    private final Locale locale;
    private final CaseSensitivity cs;
    private Map<String, List<VocabularyEntry>> words;

    @SuppressWarnings("java:S115")
    public static enum CaseSensitivity {
        /**
         * Vocabulary entries are matched fully case sensitive with the text
         */
        full,
        /**
         * All upper case words are matched case sensitive. Other vocabulary
         * entries use case insensitive matching.
         */
        smart,
        /**
         * Case insensitive matching
         */
        off;
        /**
         * By default {@link CaseSensitivity} is set to {@link CaseSensitivity#off}
         */
        public final static CaseSensitivity DEFAULT = CaseSensitivity.off;
    }

    public VocabularyDetector(String name, NerTag type, Locale lang, CaseSensitivity caseSensitivity) {
        this.name = name;
        this.type = type;
        this.locale = lang == null ? Locale.ROOT : lang;
        this.lang = lang == null ? null : lang.getLanguage().toLowerCase(Locale.ROOT).split("-_")[0];
        ;
        this.cs = caseSensitivity == null ? CaseSensitivity.DEFAULT : caseSensitivity;
    }

    public final String getName() {
        return name;
    }

    /**
     * Normalizes labels by {@link StringUtils#trimToNull(String)} and
     * if {@link #isCaseSensitive()} converts the label to lower case
     * using {@link #getLanguage()} specific rules
     *
     * @param label the label to normalize
     * @return the normalized label - <code>null</code> if the label is invalid
     */
    protected String normalize(String label) {
        String pWord = StringUtils.trimToNull(label);
        if (pWord != null) {
            switch (cs) {
                case off:
                    pWord = pWord.toLowerCase(locale);
                    break;
                case smart:
                    pWord = isAllAlphaUpperCase(pWord) ? pWord : pWord.toLowerCase(locale);
                    break;
                default: //full ... nothing to do
                    break;
            }
        }
        return pWord;
    }

    /**
     * Checks if all {@link Character#isAlphabetic(char)} are also
     * {@link Character#isUpperCase(char)}.
     *
     * @param cs
     * @return the state. <code>false</code> if the parsed sequence does not
     * contain a single alphabetic char
     */
    private static boolean isAllAlphaUpperCase(final CharSequence cs) {
        if (StringUtils.isBlank(cs)) {
            return false;
        }
        final int sz = cs.length();
        boolean hasAlpha = false;
        for (int i = 0; i < sz; i++) {
            char c = cs.charAt(i);
            if (Character.isAlphabetic(c)) {
                if (!Character.isUpperCase(c)) {
                    return false;
                }
                hasAlpha = true;
            }
        }
        return hasAlpha; //return false if no alpha char is present
    }


    public CaseSensitivity getCaseSensitivity() {
        return cs;
    }

    public Locale getLanguage() {
        return locale;
    }

    @PostConstruct
    protected final void init() throws IOException {
        log.info("load Vocabulary {} (type: {} | lang: {} | cases ensitivity: {}",
                name, type, locale.getDisplayName(), cs);
        words = new HashMap<>();
        Collection<VocabularyEntry> loadedEntries = loadEntries();
        SortedSet<String> sortedNames = new TreeSet<>(Comparator.reverseOrder());
        SortedSet<String> sortedSmartCaseNames = new TreeSet<>(Comparator.reverseOrder());
//         new Comparator<String>() { //NOTE: Comparator.reverseOrder() should do the trick (<3 java8)
//            @Override
//            public int compare(String s1, String s2) {
//                return s2.compareTo(s1);
//            }
//        });
        int i = 0;
        for (VocabularyEntry vacabEntry : loadedEntries) {
            log.trace("{}. {}", ++i, vacabEntry.getName());
            boolean isName = true; //the first label is the name
            for (String label : vacabEntry) {
                boolean isSmartCase = cs == CaseSensitivity.smart && isAllAlphaUpperCase(label);
                String pWord = normalize(label);
                Set<String> nameSet = isSmartCase ? sortedSmartCaseNames : sortedNames;
                if (pWord != null) {
                    if (!nameSet.contains(pWord)) {
                        nameSet.add(pWord);
                    } else {
                        log.debug(" - ignore duplicate lower case {}: {} ({}:{})",
                                isName ? "name" : "synonym", pWord, label, isName ? "name" : "synonym");
                    }
                } //ignore blank words
                List<VocabularyEntry> entries = words.get(pWord);
                if (entries == null) {
                    entries = new LinkedList<>();
                    words.put(pWord, entries);
                } else {
                    log.warn("{} '{}' used by {} times ",
                            isName ? "name" : "synonym", pWord, entries.size() + 1);
                }
                if (isName) {
                    log.trace(" - name   : {} (processed: {})", label, pWord);
                    entries.add(0, vacabEntry); //add entries for names at the top
                } else {
                    log.trace(" - synonym: {} (processed: {})", label, pWord);
                    entries.add(vacabEntry); //add entries for synonyms at the end
                }
                if (entries.size() > 1) {
                    log.warn(" - entries: {}", entries);
                }
                isName = false;
            }
        }
        //now build the regex pattern
        List<NamedPattern> patterns = new LinkedList<>();
        if (!sortedNames.isEmpty()) {
            patterns.add(new NamedPattern(name, cs != CaseSensitivity.full ? Pattern.compile(buildNamesRegex(sortedNames), Pattern.CASE_INSENSITIVE) :
                    Pattern.compile(buildNamesRegex(sortedNames))));
        }
        if (!sortedSmartCaseNames.isEmpty()) { //samrt case patterns are always case insensitive
            patterns.add(new NamedPattern(name, Pattern.compile(buildNamesRegex(sortedSmartCaseNames))));
        }
        this.patterns = Collections.unmodifiableList(patterns);
    }

    private String buildNamesRegex(Collection<String> names) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        builder.append('(');
        for (String word : names) {
            if (!first) {
                builder.append('|');
            } else {
                first = false;
            }
            builder.append("\\b").append(Pattern.quote(word)).append("\\b");
        }
        builder.append(')');
        return builder.toString();
    }

    protected abstract Collection<VocabularyEntry> loadEntries() throws IOException;

    @Override
    protected NamedEntity createNamedEntity(String patternName, MatchResult match) {
        log.debug("Create {} Token for [{},{}] - {}", type, match.start(), match.end(), match.group());
        if (StringUtils.isBlank(match.group())) return null;
        final String word = match.group();
        final String pWord = normalize(word);
        final List<VocabularyEntry> entries = words.get(pWord);
        if (entries != null) {
            VocabularyEntry entry = entries.get(0);
            if (log.isDebugEnabled() && entries.size() > 1) {
                log.debug("Multiple Vocabulary Entries for matched Word {} (entries: {})", word, entries);
                log.debug(" - create Token for {}", entry);
            }
            final NamedEntity ne = new NamedEntity(match.start(), match.end(), type);
            ne.setLemma(entry.name);
            ne.setConfidence(1);
            return ne;
        } else {
            return null;
        }

    }

    @Override
    protected List<NamedPattern> getRegexes(SpanCollection section, String lang) {
        String normLang = lang == null ? null : lang.toLowerCase(Locale.ROOT).split("-_")[0];
        if (this.lang == null || this.lang.equals(normLang)) {
            return patterns;
        } else {
            return Collections.emptyList();
        }
    }

}
