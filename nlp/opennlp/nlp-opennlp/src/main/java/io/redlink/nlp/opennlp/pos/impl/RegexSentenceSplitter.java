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

package io.redlink.nlp.opennlp.pos.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.util.Span;

public class RegexSentenceSplitter implements SentenceDetector {

    
    private static final Pattern SENT_SPLIT_PATTERN = Pattern.compile("(?<=[.?!;])\\s+(?=\\p{Lu})");
    
    private static final RegexSentenceSplitter INSTANCE = new RegexSentenceSplitter();
    
    public static RegexSentenceSplitter getInstance(){
        return INSTANCE;
    }
    
    private RegexSentenceSplitter() {}
    
    @Override
    public String[] sentDetect(String text) {
        return Span.spansToStrings(sentPosDetect(text), text);
    }

    @Override
    public Span[] sentPosDetect(String text) {
        List<Span> sentences = new ArrayList<>();
        Matcher m = SENT_SPLIT_PATTERN.matcher(text);
        int index = 0;
        while(m.find()){
            sentences.add(new Span(index, m.end()));
            index = m.end();
        }
        if(index < text.length()){
            sentences.add(new Span(index,text.length()));
        }
        return sentences.toArray(new Span[sentences.size()]);
    }
    
}
