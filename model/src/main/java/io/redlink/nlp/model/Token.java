/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.redlink.nlp.model;

import java.util.Arrays;
import org.springframework.data.annotation.PersistenceConstructor;

public final class Token extends Span {

    @PersistenceConstructor
    protected Token(int[] span) {
        super(SpanTypeEnum.Token, span);
    }

    protected Token(AnalyzedText at, Span relativeTo, int start, int end) {
        super(at, SpanTypeEnum.Token, relativeTo, start, end);
    }

    @Override
    public String toString() {
        return String.format("%s: %s %s", type, Arrays.toString(span), getSpan());
    }
}
