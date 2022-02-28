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

package io.redlink.nlp.api.annotation;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.PersistenceConstructor;

/**
 * Provides all information about an extracted keyword needed to write the
 * KeywordAnnotation
 * <p>
 * TODO: check if {@link #getContained()} is a good Idea, or if we should use
 * a flat list of keywords with references to contained one instead
 *
 * @author Rupert Westenthaler
 */
public class Keyword {

    private final String key;
    private final String keyword;
    private String cleanedKeyword;
    private double metric;
    private double count;

    private Set<String> contained = new HashSet<>();

    @PersistenceConstructor
    @JsonCreator
    public Keyword(String key, String keyword) {
        assert key != null;
        this.key = key;
        this.keyword = keyword;
    }

    public String getKey() {
        return key;
    }

    public void setMetric(double metric) {
        this.metric = metric;
    }

    public double getMetric() {
        return metric;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public double getCount() {
        return count;
    }

    /**
     * Getter for the keyword as originally extracted.
     *
     * @return the (uncleaned) keyword.
     */
    public String getOriginalKeyword() {
        return keyword;
    }

    /**
     * The (possible cleaned) textual representation of the keyword
     *
     * @return the textual representation of the keyword
     */
    public String getKeyword() {
        return cleanedKeyword == null ? keyword : cleanedKeyword;
    }

    /**
     * Allows to set the cleaned keyword
     *
     * @param cleanedKeyword the cleaned keyword
     */
    public void setCleanedKeyword(String cleanedKeyword) {
        this.cleanedKeyword = cleanedKeyword;
    }

    public void addContained(String key) {
        contained.add(key);
    }

    public void addAllContained(Collection<String> contained) {
        this.contained.addAll(contained);
    }

    public void setContained(Set<String> contained) {
        this.contained = contained;
    }

    /**
     * The key of (shorter) keywords contained in this one. The full keyword
     * for the key is expected to be present in the same list of keywords
     *
     * @return The {@link Keyword#getKey() key}s of contained keywords
     */
    public Set<String> getContained() {
        return contained;
    }

    @Override
    public String toString() {
        return "Keyword [key=" + key + ", keyword=" + keyword + ", metric=" + metric + ", count=" + count + "]";
    }

}