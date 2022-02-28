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

package io.redlink.nlp.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public interface Content extends Closeable {

    /**
     * The name of the resource (e.g. the file name) if available
     *
     * @return the name of the file name if available
     */
    Optional<String> getName();

    /**
     * The content as stream
     *
     * @return the content
     */
    InputStream getContent() throws IOException;

    /**
     * Setter for the mimeType of this content
     *
     * @param mimeType the mimeType (e.g. as detected by some framework)
     */
    void setMimeType(String mimeType);

    /**
     * The mime type (RFC 2045 and 2046)
     *
     * @return the mime type as string
     */
    String getMimeType();

    /**
     * Setter for the base URL (e.g. for XML based files)
     *
     * @param url the base URL or <code>null</code> if none
     */
    void setBaseUrl(String url);

    /**
     * Getter for the optional base URL
     *
     * @return the base URL if available
     */
    Optional<String> getBaseUrl();

    /**
     * Optionally the Path to the local copy of the content
     *
     * @return the local copy of the content (if available)
     */
    Optional<Path> getLocalCopy();

    /**
     * Optionally returns the plain content. Should only be implemented
     * if the content is internally stored as {@link CharSequence}.
     *
     * @return the plain content as {@link CharSequence} (if available)
     */
    Optional<CharSequence> getPlainContent();

    /**
     * Optionally returns the binary content as byte array. Should only be
     * implemented if the content is internally stored in-memory.
     *
     * @return the binary in-memory content as <code>byte[]</code> (if available)
     */
    Optional<byte[]> getBinaryContent();
}
