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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 *
 */
public class TmpFileContent extends FileContent {

    public TmpFileContent(String mimeType) throws IOException {
        this(null, mimeType);
    }

    public TmpFileContent(String name, String mimeType) throws IOException {
        super(Files.createTempFile(null, ".cnt"), mimeType); //content
        setName(name);
    }

    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(file, StandardOpenOption.WRITE);
    }

    @Override
    public void close() throws IOException {
        Files.deleteIfExists(file); //delete the tmp file when existing
    }

}
