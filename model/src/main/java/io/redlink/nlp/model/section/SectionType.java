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

package io.redlink.nlp.model.section;

public enum SectionType {

    /**
     * A page. Mainly relevant for PDFs
     */
    page(false),
    /**
     * A heading (e.g. &lt;h1&gt; to &lt;h6&gt; in HTML)
     */
    heading(true, "h"),
    /**
     * A Paragraph (e.g. &lt;p&gt; in HTML or multiple linebreaks in PDFs)
     */
    paragraph(true, "p"),
    /**
     * A list element (e.g. &lt;li&gt; in HTML)
     */
    listing(true, "li")
    
    ;
    
    private final String defaultTag;
    
    private final boolean contentSection;
    
    private SectionType(boolean contentSection) {
        this(contentSection, null);
    }
    private SectionType(boolean contentSection, String defaultTag){
        this.contentSection = contentSection;
        this.defaultTag = defaultTag;
    }
    /**
     * If this section does contain content
     * @return <code>true</code> if sections of this type directly contain content.
     * <code>false</code> if sections of this type typically contain sub-sections
     */
    public boolean isContentSection() {
        return contentSection;
    }
    
    public String tag(){
        return defaultTag == null ? name() : defaultTag;
    }
}
