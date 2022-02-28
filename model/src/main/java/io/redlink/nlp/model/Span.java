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
package io.redlink.nlp.model;

import io.redlink.nlp.api.model.Annotated;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.AccessType.Type;
import org.springframework.data.annotation.Transient;

/**
 * A span selected in the given Text. This uses {@link SoftReference}s for
 * holding the {@link #getSpan()} text to allow the Garbage Collector to
 * free up memory for large texts. In addition the span text is lazzy initialised
 * on the first call to {@link #getSpan()}.
 *
 * @author Rupert Westenthaler
 */
public abstract class Span extends Annotated implements Comparable<Span> {

    @Transient
    private final static Logger log = LoggerFactory.getLogger(Span.class);

    /**
     * Enumeration over different types - or roles - spans defined for an
     * {@link AnalyzedText} may play.
     */
    @SuppressWarnings("java:S115")
    public static enum SpanTypeEnum {
        /**
         * The Text as a whole
         */
        Text,
        /**
         * An section of the text (chapter, page, paragraph ...). NOTE: this
         * does NOT define types of sections.
         */
        TextSection,
        /**
         * An Sentence
         */
        Sentence,
        /**
         * A Chunk (e.g. a Noun Phrase) NOTE: this does NOT define types of
         * Chunks
         */
        Chunk,
        /**
         * A Token (e.g. a noun, verb, punctuation) NOTE: this does NOT define
         * types of Tokens
         */
        Token;
    }

    @AccessType(Type.FIELD)
    protected final int[] span;
    /**
     * Lazzy initialised {@link SoftReference} to the text
     */
    @Transient //anyway lazily initialized
    private SoftReference<String> textReference = null;

    @Transient //is set when adding the Spans to the AnalyzedText
    protected AnalyzedText context;

    @Transient //is set correctly to the instantiated class
    protected final SpanTypeEnum type;

    /**
     * Allows to create a SpanImpl without the {@link #getContext()}. The
     * context MUST BE set by using {@link #setContext(AnalyzedText)} before
     * using this span.
     *
     * @param type the type of the span
     * @param span the span <code>[start, end]</code> offset
     */
    protected Span(SpanTypeEnum type, int[] span) {
        assert type != null : "The parsed SpanType MUST NOT be NULL!";
        assert span != null && span.length == 2;
        assert span[0] >= 0; //negative indexes are not allowed
        assert span[1] >= span[0]; //allow empty spans
        this.type = type;
        this.span = span;

    }

    /**
     * Creates a new Span
     *
     * @param at         the context for this Span
     * @param type       the type of the span
     * @param relativeTo the span the start/end offsets are relative to
     * @param start      the start offset
     * @param end        the end offset
     */
    protected Span(AnalyzedText at, SpanTypeEnum type, Span relativeTo, int start, int end) {
        this(type, new int[]{
                relativeTo == null ? start : relativeTo.getStart() + start,
                relativeTo == null ? end : relativeTo.getStart() + end});
        setContext(at);
        //check that Spans that are created relative to an other do not cross
        //the borders of that span
        if (relativeTo != null && relativeTo.getEnd() < getEnd()) {
            throw new IllegalArgumentException("Illegal span [" + start + ',' + end
                    + "] for " + type + " relative to " + relativeTo + " : Span of the "
                    + " contained Token MUST NOT extend the others!");
        }
    }

    /**
     * Allows to set the context for this span
     *
     * @param at the analyzed text instance being the context for this span
     */
    protected void setContext(AnalyzedText at) {
        assert at != null : "The parsed AnalysedText MUST NOT be NULL!";
        this.context = at;
    }


    /**
     * The type of the Span
     *
     * @return the type
     */
    public SpanTypeEnum getType() {
        return type;
    }

    /**
     * Returns <code>true</code> if this span is empty. A
     * span is empty if {@link #getEnd()} equals {@link #getStart()}
     *
     * @return <code>true</code> if the span is empty. Otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return span[0] == span[1];
    }

    /**
     * The start index of this span This is the absolute offset from the
     * {@link #getContext()}{@link AnalyzedText#getText() .getText()}
     *
     * @return the start offset
     */
    public int getStart() {
        return span[0];
    }

    /**
     * The end index of this span. This is the absolute offset from the
     * {@link #getContext()}{@link AnalyzedText#getText() .getText()}
     *
     * @return the end offset
     */
    public int getEnd() {
        return span[1];
    }

    /**
     * The {@link AnalyzedText} this Span was added to.
     *
     * @return the AnalyzedText representing the context of this Span
     */
    public final AnalyzedText getContext() {
        return context;
    }

    /**
     * The section of the text selected by this span
     *
     * @return the selected section of the text
     */
    public String getSpan() {
        if (isEmpty()) {
            return "";
        } else {
            String spanText = textReference == null ? null : textReference.get();
            if (spanText == null) {
                spanText = getContext().getText().subSequence(span[0], span[1]).toString();
                textReference = new SoftReference<String>(spanText);
            }
            return spanText;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(span);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Span other = (Span) obj;
        if (!Arrays.equals(span, other.span))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", type, Arrays.toString(span));
    }

    @Override
    public int compareTo(Span o) {
        if (context != null && o.getContext() != null &&
                !context.equals(o.getContext())) {
            log.warn("Comparing Spans with different Context. This is not an " +
                    "intended usage of this class as start|end|type parameters " +
                    "do not have a natural oder over different texts.");
            log.info("This will sort Spans based on start|end|type parameters " +
                    "regardless that the might be over different texts!");
            //TODO consider throwing an IllegalStateExcetion!
        }
        //Compare Integers ASC (used here three times)
        //    (x < y) ? -1 : ((x == y) ? 0 : 1);
        int start = (span[0] < o.getStart()) ? -1 : ((span[0] == o.getStart()) ? 0 : 1);
        if (start == 0) {
            if (type == o.type) {
                //sort end in DESC order
                return (span[1] < o.getEnd()) ? 1 : ((span[1] == o.getEnd()) ? 0 : -1);
            } else {
                //if start AND end is the same compare based on the span type
                //Natural order of span types is defined by the Enum.ordinal()
                return type.ordinal() < o.type.ordinal() ? -1 : 1;
            }
        } else {
            return start;
        }
    }


}
