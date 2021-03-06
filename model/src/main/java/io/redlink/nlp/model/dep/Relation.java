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
package io.redlink.nlp.model.dep;

import io.redlink.nlp.model.Sentence;
import io.redlink.nlp.model.Span;
import io.redlink.nlp.model.Token;

/**
 * Represents the grammatical relation that a {@link Token} can have with
 * another {@link Token} from the same {@link Sentence}
 *
 * @author Cristian Petroaca
 * @author Rupert Westenthaler
 */
public class Relation {

    /**
     * The actual grammatical relation tag
     */
    private final RelTag relation;

    /**
     * Denotes whether the {@link Token} which has this relation is dependent in
     * the relation
     */
    private final boolean isDependent;

    /**
     * The {@link Token} with which the relation is made.
     */
    private final Span partner;

    public Relation(RelTag relation, boolean isDependent, Span partner) {
        if (relation == null) {
            throw new IllegalArgumentException("The grammatical relation MUST NOT be NULL");
        }
        this.relation = relation;

        this.isDependent = isDependent;
        if (partner == null) {
            throw new IllegalArgumentException("The partner of the dependency MUST NOT be NULL");
        }
        this.partner = partner;
    }

    public RelTag getGrammaticalRelationTag() {
        return relation;
    }

    public boolean isDependent() {
        return isDependent;
    }

    public Span getPartner() {
        return this.partner;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + relation.hashCode();
        result = prime * result + (isDependent ? 1231 : 1237);
        result = prime * result + ((partner == null) ? 0 : partner.hashCode());
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

        Relation other = (Relation) obj;

        if (partner == null) {
            if (other.partner != null)
                return false;
        } else if (!partner.equals(other.partner))
            return false;

        return (relation.equals(other.relation))
                && (isDependent == other.isDependent);
    }

    @Override
    public String toString() {
        return "Relation [relation=" + relation + ", partner=" + partner + (isDependent ? " (dependent)" : "");
    }

}
