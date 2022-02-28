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
package io.redlink.nlp.model.morpho;

import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.tag.Tag;
import io.redlink.nlp.model.tag.TagSet;
import org.springframework.data.annotation.PersistenceConstructor;


/**
 * An VerbMood tag typically assigned by a Morphological Analyzer (an
 * NLP component) to a {@link Token} <p>
 *
 * @author Alessio Bosca
 */
public class VerbMoodTag extends Tag<VerbMoodTag> {

    private final VerbMood verbMood;

    /**
     * Creates a new VerbMoodTag for the parsed tag. The created Tag is not
     * assigned to any {@link VerbMood}.<p> This constructor can be used
     * by components that encounter an Tag they do not know
     * (e.g. that is not defined by the configured {@link TagSet}).<p>
     *
     * @param tag the Tag
     * @throws IllegalArgumentException if the parsed tag is <code>null</code>
     *                                  or empty.
     */
    public VerbMoodTag(String tag) {
        this(tag, null);
    }

    /**
     * Creates a VerbMoodTag that is assigned to a {@link Case}
     *
     * @param tag      the tag
     * @param verbMood the lexical VerbMood or <code>null</code> if not known
     * @throws IllegalArgumentException if the parsed tag is <code>null</code>
     *                                  or empty.
     */
    @PersistenceConstructor
    public VerbMoodTag(String tag, VerbMood verbMood) {
        super(tag);
        this.verbMood = verbMood;
    }

    /**
     * The verbMood of this tag (if known)
     *
     * @return the VerbMood or <code>null</code> if not mapped to any
     */
    public VerbMood getVerbForm() {
        return this.verbMood;
    }

    @Override
    public String toString() {
        return String.format("VERB FORM %s (%s)", tag,
                verbMood == null ? "none" : verbMood.name());
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof VerbMoodTag &&
                (verbMood == null && ((VerbMoodTag) obj).verbMood == null) ||
                (verbMood != null && verbMood.equals(((VerbMoodTag) obj).verbMood));
    }

}

