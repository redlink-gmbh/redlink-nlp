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
package io.redlink.nlp.api.content;

import io.redlink.nlp.api.Content;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 *
 */
public class FileContent implements Content {

    private String name;
    protected final Path file;
    private String mimeType;
    private Optional<String> baseUrl = Optional.empty();

    public FileContent(Path path, String mimeType) {
        assert path != null;
        assert Files.isRegularFile(path);
        this.file = path;
        this.mimeType = mimeType;
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Optional<String> getName() {
        return Optional.of(name == null ? file.getFileName().toString() : name);
    }

    @Override
    public InputStream getContent() throws IOException {
        return Files.newInputStream(file, StandardOpenOption.READ);
    }

    @Override
    public String getMimeType() {
        return mimeType == null ? "application/octet-stream" : mimeType;
    }

    @Override
    public Optional<String> getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void setBaseUrl(String url) {
        this.baseUrl = Optional.ofNullable(url);
    }

    @Override
    public Optional<Path> getLocalCopy() {
        return Optional.of(file);
    }

    @Override
    public Optional<byte[]> getBinaryContent() {
        return Optional.empty(); //we do not have content in-memory
    }

    @Override
    public Optional<CharSequence> getPlainContent() {
        return Optional.empty(); //we do not have plain content in-memory
    }

    @Override
    public void close() throws IOException {
        //nothing to do here
    }
}
