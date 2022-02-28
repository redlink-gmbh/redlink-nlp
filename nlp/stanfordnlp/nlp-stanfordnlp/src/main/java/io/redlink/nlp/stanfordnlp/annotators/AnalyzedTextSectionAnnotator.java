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

package io.redlink.nlp.stanfordnlp.annotators;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.Section;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AnalyzedTextSectionAnnotator implements Annotator {


    @Override
    public void annotate(Annotation annotation) {
        AnalyzedText at = annotation.get(AnalyzedTextAnnotation.class);
        if (at == null) {
            return; // no AnalyzedText present ... nothing to do
        }
        List<Section> activeSections = new LinkedList<>();
        Iterator<Section> sections = at.getSections();
        if (!sections.hasNext()) {
            return; // no sections ... nothing to do
        }
        if (annotation.containsKey(CoreAnnotations.TokensAnnotation.class)) {
            Section current = sections.next();
            List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
            CoreLabel token = null;
            for (Iterator<CoreLabel> tokenIt = tokens.iterator(); (current != null || !activeSections.isEmpty()) && tokenIt.hasNext(); ) {
                token = tokenIt.next();
                int start = token.beginPosition();
                while (current != null && current.getStart() <= start) {
                    activeSections.add(0, current);
                    current = sections.hasNext() ? sections.next() : null;
                    //TODO: maybe we want also use SectionStart annotation
                }
                //also consider non token chars after the current token for the end position
                int end = token.endPosition() + (token.after() != null ? token.after().length() : 0);
                boolean isSectionEnd = false;
                for (Iterator<Section> sectionIt = activeSections.iterator(); sectionIt.hasNext(); ) {
                    Section active = sectionIt.next();
                    if (active.getEnd() <= end) {
                        isSectionEnd = true; //this token represents a section end
                        sectionIt.remove();
                    } //else section is still active
                }
                if (isSectionEnd) {
                    //TODO: maybe we want also use SectionEnd annotation
                    token.set(CoreAnnotations.ForcedSentenceEndAnnotation.class, true);
                }
            }
        }
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.emptySet();
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.singleton(CoreAnnotations.TokensAnnotation.class);
    }

    /**
     * Annotation that provides the AnalyzedText with pre-defined {@link Section}
     */
    public static class AnalyzedTextAnnotation implements CoreAnnotation<AnalyzedText> {
        public Class<AnalyzedText> getType() {
            return AnalyzedText.class;
        }
    }

}
