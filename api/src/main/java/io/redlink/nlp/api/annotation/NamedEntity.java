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

package io.redlink.nlp.api.annotation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.PersistenceConstructor;

public class NamedEntity {

    private String name;
    private String cleanedName;
    private String type;
    private int count;

    @PersistenceConstructor
    @JsonCreator
    public NamedEntity(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public void setCleanedName(String cleanedName) {
        this.cleanedName = cleanedName;
    }

    public String getName() {
        return cleanedName == null ? name : cleanedName;
    }

    @JsonIgnore
    public String getOriginalName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "NamedEntity [name=" + getName() +
                (cleanedName != null && !cleanedName.equals(name) ? ", originalName=" + name : "")
                + ", type=" + type + ", count=" + count + "]";
    }


}
