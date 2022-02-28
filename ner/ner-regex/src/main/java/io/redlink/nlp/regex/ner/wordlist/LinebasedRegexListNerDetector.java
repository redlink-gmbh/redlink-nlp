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

package io.redlink.nlp.regex.ner.wordlist;

import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.regex.ner.RegexNerDetector;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Variant of the {@link RegexDetector} that reads from a {@link Reader}
 * expecting one regex pattern per line.
 * It will ignore lines with illegal formatted patterns
 *
 * @author Rupert Westenthaler
 */
public abstract class LinebasedRegexListNerDetector extends RegexNerDetector {

    private int flags = 0;
    private final String lang;

    /**
     * @param name          the name
     * @param type          the type of the Named Entities
     * @param lang          the language (<code>null</code> for any language)
     * @param caseSensitive case sensitive state
     */
    protected LinebasedRegexListNerDetector(String name, NerTag type, String lang, boolean caseSensitive) {
        super(name, type);
        if (!caseSensitive) {
            flags = flags | Pattern.CASE_INSENSITIVE;
        }
        this.lang = lang;
    }

    @Override
    protected final Map<String, List<Pattern>> initPatterns() throws IOException {
        List<Pattern> patterns = new LinkedList<>();
        try (Reader r = readFrom()) {
            int lineNr = 0;
            Iterator<String> lines = IOUtils.lineIterator(r);
            while (lines.hasNext()) {
                lineNr++;
                String patternString = StringUtils.trimToNull(lines.next());
                if (patternString != null) {
                    try {
                        patterns.add(Pattern.compile(patternString, flags));
                    } catch (IllegalArgumentException e) {
                        log.warn("Unable to parsed pattern from line " + lineNr + ":'" + patternString + "'", e);
                    }
                }
            }
        }
        return Collections.singletonMap(lang, patterns);
    }

    protected abstract Reader readFrom();
}
