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

package io.redlink.nlp.stanfordnlp.sentiment;

public class LinearSentimentClassMapping implements SentimentClassMapping {

    final int numClasses;
    final int minValue;
    final int maxValue;
    final double increment;

    public LinearSentimentClassMapping(int numClasses) {
        this(numClasses, -1, 1);
    }

    public LinearSentimentClassMapping(int numClasses, int minValue, int maxValue) {
        assert numClasses > 1;
        assert maxValue > minValue;
        this.numClasses = numClasses;
        this.minValue = minValue;
        this.maxValue = maxValue;
        increment = (maxValue - minValue) / ((double) numClasses - 1);
    }

    @Override
    public double getIndexWeight(int index) {
        return minValue + index * increment;
    }
}
