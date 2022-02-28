/*
 * Copyright (c) 2016-2022 Redlink GmbH.
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
package io.redlink.nlp.time.duckling;

import io.redlink.nlp.model.temporal.Temporal;
import java.util.Comparator;

/**
 *
 */
public class DateToken {

    /**
     * Comparator that sorts {@link DateToken}s based on<ol>
     * <li> lower message index
     * <li> lower start char offset
     * <li> higher end char offset
     * </ol>
     * first
     */
    public static final Comparator<DateToken> IDX_START_END_COMPARATOR = (t1, t2) -> {
        //lower start first
        int c = Integer.compare(t1.getOffsetStart(), t2.getOffsetStart());
        if (c == 0) { //higher end first
            c = Integer.compare(t2.getOffsetEnd(), t1.getOffsetEnd());
        }
        return c;
    };

    /**
     * Comparator that sorts the {@link DateToken} with the highest {@link DateToken#getConfidence()}
     * first.
     */
    public static final Comparator<DateToken> CONFIDENCE_COMPARATOR = (t1, t2) -> Float.compare(t2.getConfidence(), t1.getConfidence());


    /**
     * The start char offset of the mention for this token
     */
    private int offsetStart = -1;
    /**
     * The end char offset of the mention for this token
     */
    private int offsetEnd = -1;

    private Temporal start, end;

    /**
     * The confidence of the token. Provided by the component that extracted the token
     */
    private float confidence;

    private boolean instant;

    /**
     * Getter for the start char offset of the Token
     *
     * @return the start char offset
     */
    public int getOffsetStart() {
        return offsetStart;
    }

    /**
     * Setter for the start char offset of the Token
     *
     * @param offsetStart the start char offset
     */
    void setOffsetStart(int offsetStart) {
        this.offsetStart = offsetStart;
    }

    /**
     * Getter for the end char offset of the Token
     *
     * @return the end char offset (exclusive) (similar to {@link String#substring(int, int)})
     */
    public int getOffsetEnd() {
        return offsetEnd;
    }

    /**
     * Setter for the end char offset of the Token
     *
     * @param offsetEnd the end char offset (exclusive) (similar to {@link String#substring(int, int)})
     */
    void setOffsetEnd(int offsetEnd) {
        this.offsetEnd = offsetEnd;
    }

    /**
     * The start of the date-range. If instants, this is the same as {@link #getEnd()}
     *
     * @return the start of the date-range
     */
    public Temporal getStart() {
        return start;
    }

    /**
     * Setter for the value of the token.
     */
    void setStart(Temporal start) {
        this.start = start;
    }

    /**
     * The end of the date-range. If instants, this is the same as {@link #getStart()}
     *
     * @return the end of the date-range
     */
    public Temporal getEnd() {
        return end;
    }

    void setEnd(Temporal end) {
        this.end = end;
    }

    public boolean isOpenInterval() {
        return isInterval() && (start == null || end == null);
    }

    public boolean isInterval() {
        return !isInstant();
    }

    public boolean isInstant() {
        return instant;
    }

    void setInstant(boolean instant) {
        this.instant = instant;
    }

    /**
     * Getter for the <code>[0..1]</code> confidence of this token.
     *
     * @return the confidence
     */
    public float getConfidence() {
        return confidence;
    }

    /**
     * Setter for the <code>[0..1]</code> confidence of this token.
     *
     * @param confidence the confidence
     */
    void setConfidence(float confidence) {
        this.confidence = confidence > 1f ? 1f : confidence < 0f ? 0f : confidence;
    }

    @Override
    public String toString() {
        if (isInstant()) {
            return "DateToken [" +
                    "instant" +
                    ", start=" + start +
                    ", end=" + end +
                    ", offset=" + offsetStart + ".." + offsetEnd +
                    ", conf=" + confidence + "]";
        } else {
            return "DateToken [" +
                    "interval" + (isOpenInterval() ? " (open)" : "") +
                    ", start=" + start +
                    ", end=" + end +
                    ", offset=" + offsetStart + ".." + offsetEnd +
                    ", conf=" + confidence + "]";
        }
    }


}
