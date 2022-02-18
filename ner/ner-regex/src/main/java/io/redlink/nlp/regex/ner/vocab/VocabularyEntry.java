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

package io.redlink.nlp.regex.ner.vocab;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class VocabularyEntry implements Iterable<String> {

    final String name;
    final Set<String> synonyms;
    final Set<String> unmodSynonyms;

    public VocabularyEntry(String name) {
        assert StringUtils.isNotBlank(name);
        this.name = name;
        this.synonyms = new HashSet<>();
        this.unmodSynonyms = Collections.unmodifiableSet(synonyms);
    }

    public final String getName() {
        return name;
    }

    public final Set<String> getSynonyms() {
        return unmodSynonyms;
    }

    public final boolean addSynonym(String synonym) {
        if (name.equals(synonym)) {
            return false;
        } else {
            return synonyms.add(synonym);
        }
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {

            boolean first = true;
            Iterator<String> it = synonyms.iterator();

            @Override
            public boolean hasNext() {
                return first || it.hasNext();
            }

            @Override
            public String next() {
                if (first) {
                    first = false;
                    return name;
                } else {
                    return it.next();
                }
            }
        };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
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
        VocabularyEntry other = (VocabularyEntry) obj;
        if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "VocabEntry [name=" + name + ", synonyms=" + synonyms + "]";
    }

}
