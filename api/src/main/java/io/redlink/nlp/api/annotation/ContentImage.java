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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.data.annotation.Transient;

/**
 * An image referenced by the content of a document.
 * <p>
 * This class is used during pre-processing to collect information about
 * images mentioned in documents.
 *
 * @author Rupert Westenthaler
 */
public class ContentImage {

    private final String uri;
    private Integer height;
    private Integer width;
    private Float wScale;
    private Float hScale;
    private String alt;
    private Double contentScore;
    private String property;

    @Transient
    private boolean isValid;

    /**
     * Creates a new ContentImage for the specified uri
     *
     * @param uri the absolute uri. MUST NOT be <code>null</code>
     */
    @JsonCreator
    public ContentImage(@JsonProperty("uri") String uri) {
        if (uri == null) {
            throw new NullPointerException("The parsed URI MUST NOT be NULL!");
        }
        try {
            isValid = new URI(uri).isAbsolute();
        } catch (URISyntaxException e) {
            String correctedUri = uri.trim().replace(" ", "%20");
            try {
                isValid = new URI(correctedUri).isAbsolute();
                uri = correctedUri;
            } catch (URISyntaxException e1) {
                isValid = false;
            }
        }
        this.uri = uri;
    }

    /**
     * Explicit height of the embedded image. Might be different to the image height.
     *
     * @return the height
     */
    @JsonProperty(value = "h", index = 2)
    public Integer getHeight() {
        return height;
    }

    /**
     * Explicit height of the embedded image. Might be different to the image height.
     *
     * @param height the height
     */
    public void setHeight(@JsonProperty("h") Integer height) {
        this.height = height;
    }

    /**
     * Explicit width of the embedded image. Might be different to the image width.
     *
     * @return the width
     */
    @JsonProperty(value = "w", index = 1)
    public Integer getWidth() {
        return width;
    }

    /**
     * Explicit width of the embedded image. Might be different to the image width.
     *
     * @param width the width
     */
    public void setWidth(@JsonProperty("w") Integer width) {
        this.width = width;
    }

    /**
     * The scale factor for the width
     *
     * @return the width scale
     */
    @JsonProperty(value = "sw", index = 3)
    public Float getWidthScale() {
        return wScale;
    }

    /**
     * The scale factor for the width
     *
     * @param wScale the width scale
     */
    public void setWidthScale(@JsonProperty("sw") Float wScale) {
        this.wScale = wScale;
    }

    /**
     * The scale factor for the height
     *
     * @return the height scale
     */
    @JsonProperty(value = "sh", index = 4)
    public Float getHeightScale() {
        return hScale;
    }

    /**
     * The scale factor for the height
     *
     * @param hScale the height scale
     */
    public void setHeightScale(@JsonProperty("sh") Float hScale) {
        this.hScale = hScale;
    }

    /**
     * Getter for the alternative text for this image
     *
     * @return the alternative text or <code>null</code> if none
     */
    @JsonProperty(value = "alt", index = 5)
    public String getAlternativeText() {
        return alt;
    }

    /**
     * Setter for the alternative Text for this image
     *
     * @param alt the alternative text or <code>null</code> if none
     */
    public void setAlternativeText(@JsonProperty("alt") String alt) {
        this.alt = alt;
    }

    /**
     * A score based on the amount of content surrounding the referenced image.
     * <p>
     * NOTE: This is currently calculated by the pre-processor based on the
     * number of sentences. The value is normalized using the
     * {@link Math#log1p(double) natural logarithm of the sum of the argument and 1}
     *
     * @return the content score
     */
    @JsonProperty(value = "cs", index = 6)
    public Double getContentScore() {
        return contentScore;
    }

    /**
     * A score based on the amount of content surrounding the referenced image.
     * <p>
     * NOTE: This is currently calculated by the pre-processor based on the
     * number of sentences. The value is normalized using the
     * {@link Math#log1p(double) natural logarithm of the sum of the argument and 1}
     *
     * @param contentScore the content score
     */
    public void setContentScore(@JsonProperty("cs") Double contentScore) {
        this.contentScore = contentScore;
    }

    /**
     * The (absolute) uri of the image. Might be a file URL in case of embedded
     * images (such in a docx or pdf document)
     *
     * @return the uri or the image referenced by the content
     */
    @JsonProperty(value = "uri", index = 0)
    public String getUri() {
        return uri;
    }

    /**
     * The property used to link to the image. Useful for images referenced by
     * &lt;meta /&gt; tags and/or rich snippets. For normal image references
     * (e.g. &lt;img /&gt; elements) this shall be <code>null</code>.
     *
     * @return the property referring to the image
     */
    @JsonProperty(value = "prop", index = 7)
    public String getProperty() {
        return property;
    }

    /**
     * Setter for the property referring to the image. Useful for images referenced by
     * &lt;meta /&gt; tags and/or rich snippets. For normal image references
     * (e.g. &lt;img /&gt; elements) this shall be <code>null</code>.
     *
     * @param property the property or <code>null</code> if not known or not
     *                 applicable.
     */
    @JsonProperty(value = "prop")
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * If the {@link #getUri()} is a valid and absolute URI
     */
    @JsonIgnore
    public boolean isValid() {
        return isValid;
    }

    @Override
    public String toString() {
        return "ContentImage [uri=" + uri + ", contentScore=" + contentScore + ", height=" + height + ", width=" + width
                + ", wScale=" + wScale + ", hScale=" + hScale + ", alt=" + alt + ", isValid=" + isValid + "]";
    }

}
