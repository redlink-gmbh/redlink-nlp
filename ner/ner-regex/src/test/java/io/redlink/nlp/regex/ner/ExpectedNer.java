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

package io.redlink.nlp.regex.ner;

import io.redlink.nlp.api.ProcessingData;
import io.redlink.nlp.api.model.Value;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Span;
import io.redlink.nlp.model.Span.SpanTypeEnum;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.util.NlpUtils;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExpectedNer {

    private static Logger log = LoggerFactory.getLogger(ExpectedNer.class);

    final String tag;
    final String type;
    final String mention;
    final String lemma;

    public ExpectedNer(String tag, String type, String mention) {
        this(tag, type, mention, null);
    }

    public ExpectedNer(String tag, String type, String mention, String lemma) {
        this.tag = tag;
        this.type = type;
        this.mention = mention;
        this.lemma = lemma;
    }

    @Override
    public String toString() {
        return "ExpectedNer [tag=" + tag + ", type=" + type + ", mention=" + mention + ", lemma=" + lemma + "]";
    }

    public static void assertNlpProcessingResults(ProcessingData pd, List<ExpectedNer> expected) {
        expected = new LinkedList<>(expected); //copy the parsed list
        Optional<AnalyzedText> at = NlpUtils.getAnalyzedText(pd);
        Assert.assertTrue(at.isPresent());
        Iterator<Span> spans = at.get().getEnclosed(EnumSet.allOf(SpanTypeEnum.class));
        int lastNerEnd = 0;
        while (spans.hasNext()) {
            Span span = spans.next();
            switch (span.getType()) {
                case Chunk:
                    log.debug("{} {}", span, span.getSpan());
                    Assert.assertTrue(lastNerEnd <= span.getStart());  //none overlapping
                    lastNerEnd = span.getEnd();
                    List<Value<NerTag>> nerAnnos = span.getValues(NlpAnnotations.NER_ANNOTATION);
                    Assert.assertNotNull(nerAnnos);
                    Assert.assertEquals(1, nerAnnos.size());
                    Value<NerTag> nerAnno = nerAnnos.get(0);
                    Assert.assertNotNull(nerAnno);
                    Assert.assertTrue(nerAnno.probability() == Value.UNKNOWN_PROBABILITY ||
                            (nerAnno.probability() >= 0d && nerAnno.probability() <= 1d));
                    NerTag ner = nerAnno.value();
                    log.debug(" - {}", ner);
                    Assert.assertNotNull(ner);
                    Assert.assertNotNull(ner.getTag());
                    ExpectedNer expectedNer = null;
                    for (Iterator<ExpectedNer> it = expected.iterator(); it.hasNext(); ) {
                        ExpectedNer en = it.next();
                        if (en.mention.equals(span.getSpan())) {
                            expectedNer = en;
                            it.remove();
                            break;
                        }
                    }
                    Assert.assertNotNull("Unexpected Named Entity " + span + " " + span.getSpan(), expectedNer);
                    Assert.assertEquals(expectedNer.tag, ner.getTag());
                    Assert.assertEquals(expectedNer.type, ner.getType());
                    List<Value<String>> lemmas = span.getValues(NlpAnnotations.LEMMA_ANNOTATION);
                    Assert.assertNotNull(lemmas);
                    if (expectedNer.lemma == null) {
                        Assert.assertTrue(lemmas.isEmpty());
                    } else {
                        Assert.assertEquals(1, lemmas.size());
                        Value<String> lemmaAnno = lemmas.get(0);
                        Assert.assertNotNull(lemmaAnno);
                        Assert.assertTrue(lemmaAnno.probability() == Value.UNKNOWN_PROBABILITY ||
                                (lemmaAnno.probability() >= 0d && lemmaAnno.probability() <= 1d));
                        String lemma = lemmaAnno.value();
                        log.debug(" - lemma {}", lemma);
                        Assert.assertEquals(expectedNer.lemma, lemma);
                    }
                    break;
                default:
                    break;
            }
        }
        Assert.assertTrue("Missing Expected: " + expected, expected.isEmpty());
    }
}