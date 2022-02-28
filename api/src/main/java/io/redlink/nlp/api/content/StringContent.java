/*
 * Copyright (c) 2017-2022 Redlink GmbH.
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
package io.redlink.nlp.api.content;

import io.redlink.nlp.api.Content;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class StringContent implements Content {

    private static final Charset UTF8 = Charset.forName("utf-8");
    private final Optional<String> name;
    private final CharSequence content;
    private String mime = "text/plain";
    private Optional<String> baseUrl;

    public StringContent(CharSequence content) {
        this(content, null);
    }

    public StringContent(CharSequence content, String name) {
        assert content != null;
        this.content = content;
        this.name = StringUtils.isBlank(name) ? Optional.empty() : Optional.of(name);
    }

    @Override
    public void close() {
    }

    @Override
    public Optional<String> getName() {
        return name;
    }

    @Override
    public InputStream getContent() throws IOException {
        return new CharSequenceInputStream(content, UTF8);
    }

    @Override
    public void setMimeType(String mimeType) {
        mime = mimeType == null ? "text/plain" : mimeType;
    }

    @Override
    public String getMimeType() {
        return mime;
    }

    @Override
    public void setBaseUrl(String url) {
        this.baseUrl = StringUtils.isBlank(url) ? Optional.empty() : Optional.of(url);
    }

    @Override
    public Optional<String> getBaseUrl() {
        return baseUrl;
    }

    @Override
    public Optional<Path> getLocalCopy() {
        return Optional.empty();
    }

    @Override
    public Optional<byte[]> getBinaryContent() {
        return Optional.empty();
    }

    @Override
    public Optional<CharSequence> getPlainContent() {
        return Optional.of(content);
    }

}
