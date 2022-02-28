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

package io.redlink.nlp.api.annotation;

import io.redlink.nlp.api.model.Annotation;
import java.net.URI;
import java.util.Date;

public interface Annotations {

    /**
     * The Language of the Document as detected based on its content (or metadata)
     */
    Annotation<String> LANGUAGE = new Annotation<>("dc:language", String.class);
    /**
     * The creation date of the Document
     */
    Annotation<Date> CREATED = new Annotation<>("dc:created", Date.class);
    /**
     * The modification date of the document
     */
    Annotation<Date> MODIFIED = new Annotation<>("dc:modified", Date.class);

    /**
     * The title of the document
     */
    Annotation<String> TITLE = new Annotation<>("dc:title", String.class);

    /**
     * A description for the document (e.g. as provided from metadata of the document)
     */
    Annotation<String> DESCRIPTION = new Annotation<>("dc:description", String.class);

    /**
     * The sub-title of the document
     */
    Annotation<String> SUB_TITLE = new Annotation<>("redlink:subtitle", String.class);
    /**
     * Headlines extracted from the document
     */
    Annotation<Headline> HEADLINE = new Annotation<>("redlink:headline", Headline.class);

    /**
     * Links extracted from the document
     */
    Annotation<URI> OUTLINK = new Annotation<>("redlink:outlink", URI.class);

    /**
     * The {@link ContentImage} annotation provides information about images embedded in the content. This
     * can e.g. be useful when selecting for a thumbnail.
     */
    Annotation<ContentImage> CONTENT_IMAGE = new Annotation<>("redlink:contentimage", ContentImage.class);
    /**
     * Keywords extracted from the document
     */
    Annotation<Keyword> KEYWORD = new Annotation<>("redlink:keyword", Keyword.class);
    /**
     * NamedEntities extracted from the document
     */
    Annotation<NamedEntity> NAMED_ENTITY = new Annotation<>("redlink:namedentity", NamedEntity.class);

}
