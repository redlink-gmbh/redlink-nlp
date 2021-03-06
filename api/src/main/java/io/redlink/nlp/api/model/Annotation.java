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
package io.redlink.nlp.api.model;

/**
 * Definition of an Annotation including the <ul>
 * <li>key used to store the Annotation
 * <li>generic type of Values for this Annotation
 * </ul>
 *
 * @param <V>
 */
public final class Annotation<V> {

    /**
     * The type of the used Key
     */
    final String key;
    /**
     * The type of the used Value
     */
    final Class<V> valueType;

    public Annotation(String key, Class<V> valueType) {
        if (key == null || valueType == null) {
            throw new IllegalArgumentException("Key and Value MUST NOT be NULL!");
        }
        this.key = key;
        this.valueType = valueType;
    }

    public String getKey() {
        return key;
    }

    public Class<V> getValueType() {
        return valueType;
    }

}
