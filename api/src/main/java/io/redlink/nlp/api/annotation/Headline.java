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

import org.apache.commons.lang3.StringUtils;

public class Headline {

    private final int level;
    private final String text;

    public Headline(int level, String text) {
        assert (level >= 0 && level <= 6); // 0 unknown
        assert StringUtils.isNotBlank(text);
        this.level = level;
        this.text = text;
    }

    /**
     * The level <code>[1-6]</code> or <code>0</code> if unknown
     *
     * @return the level of the headline
     */
    public int getLevel() {
        return level;
    }

    /**
     * The headline
     *
     * @return the headline
     */
    public String getText() {
        return text;
    }

}