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

package io.redlink.nlp.stanza.sentiment;

/**
 * Stanford NLP uses Sentiment Classes. Implementations of this interface are
 * used to assign double sentiment values (typically in the range [-1..+1]) to
 * those classes.
 * 
 * @author Rupert Westenthaler
 *
 */
public interface SentimentClassMapping {

    /**
     * Getter for the double weight for the sentiment class with a given index
     * @param index the index of the sentiment class
     * @return the double sentiment weight or {@link Double#NaN} if no weight is
     * defined for this index
     */
    double getIndexWeight(int index);

}
