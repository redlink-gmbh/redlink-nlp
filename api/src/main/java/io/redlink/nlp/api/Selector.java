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

package io.redlink.nlp.api;

/**
 * A selector
 * <p>
 * <pre>
 *   This is &gt;an example&lt; of a selection
 * </pre>
 *
 * @author westei
 */
public class Selector {

    private int start;
    private int end;
    private String head;
    private String tail;
    private String mention;
    private String prefix;
    private String suffix;

    public Selector(int start, int end) {
        assert start >= 0;
        assert end > start;
        setStart(start);
        setEnd(end);
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getTail() {
        return tail;
    }

    public void setTail(String tail) {
        this.tail = tail;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Selector [")
                .append(start).append("..").append(end).append("|");
        if (prefix != null && suffix != null) {
            sb.append(prefix).append(">");
        }
        if (mention != null) {
            sb.append(mention);
        } else if (head != null && tail != null) {
            sb.append(head).append("[..]").append(tail);
        }
        if (prefix != null && suffix != null) {
            sb.append("<").append(suffix);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + start;
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
        Selector other = (Selector) obj;
        if (end != other.end)
            return false;
        if (start != other.start)
            return false;
        return true;
    }


}
