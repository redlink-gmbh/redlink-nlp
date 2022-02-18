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

package io.redlink.nlp.regex.ner.wordlist;

import io.redlink.nlp.model.ner.NerTag;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Variant of the {@link WordListDetector} that reads from a {@link Reader}
 * expecting one word per line.
 *
 * @author Rupert Westenthaler
 */
public abstract class LinebasedWordListNerDetector extends WordListDetector {

    protected LinebasedWordListNerDetector(String name, NerTag type, Locale lang, boolean caseSensitive) {
        super(name, type, lang, caseSensitive);
    }

    @Override
    protected final Set<String> loadWords() throws IOException {
        Set<String> words = new HashSet<>();
        try (Reader r = readFrom()) {
            Iterator<String> lines = IOUtils.lineIterator(r);
            while (lines.hasNext()) {
                String word = StringUtils.trimToNull(lines.next());
                if (word != null) {
                    words.add(word);
                }
            }
        }
        return words;
    }

    protected abstract Reader readFrom();

}
