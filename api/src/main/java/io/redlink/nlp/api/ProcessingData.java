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

package io.redlink.nlp.api;

import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.model.Annotated;
import io.redlink.nlp.api.model.Annotation;
import io.redlink.nlp.api.model.Value;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;

/**
 * Class used to store all information related to
 * processing an item.
 *
 * @author Rupert Westenthaler
 */
public class ProcessingData extends Annotated implements Closeable {

    private final Content content;
    private List<Content> extrContents = new LinkedList<Content>();

    private Map<String, Object> configuration;

    public ProcessingData(Content content, Map<String, Object> configuration) {
        this.configuration = configuration == null ? new HashMap<>() : configuration;
        assert content != null;
        this.content = content;
    }

    /**
     * Getter for the content of a specific mime tpye. In case
     * multiple contents with the same mimeType (excl. parameters)
     * are present this will return the first one.
     *
     * @param mimeType the mimeType. Parameters of the parsed mimeType
     *                 are ignored.
     * @return the Content with the requested mimeType (including the
     * original content as well as extracted contents). <code>null</code>
     * if no Content for the requested mimeType is present
     */
    public Optional<Content> getContent(String mimeType) {
        if (mimeType == null) {
            return Optional.of(getContent());
        } else {
            String cleanedMimeType = cleanMimeType(mimeType);
            if (cleanedMimeType.equals(cleanMimeType(content.getMimeType()))) {
                return Optional.of(content);
            } else {
                return extrContents.stream()
                        .filter(c -> cleanedMimeType.equals(cleanMimeType(c.getMimeType())))
                        .findFirst();
            }
        }
    }

    /**
     * Getter for all contents compatible with the parsed mime type.
     *
     * @param mimeType the mimeType. Parameters of the parsed mimeType
     *                 are ignored.
     * @return the Content with the requested mimeType (including the
     * original content as well as extracted contents). <code>null</code>
     * if no Content for the requested mimeType is present
     */
    public List<Content> getContents(String mimeType) {
        LinkedList<Content> contents = new LinkedList<>();
        if (mimeType == null) {
            contents.add(content);
            contents.addAll(extrContents);
        } else {
            String cleanedMimeType = cleanMimeType(mimeType);
            if (cleanedMimeType.equals(cleanMimeType(content.getMimeType()))) {
                contents.add(content);
            }
            contents.addAll(extrContents.stream()
                    .filter(c -> cleanedMimeType.equals(cleanMimeType(c.getMimeType())))
                    .collect(Collectors.toList()));
        }
        return contents;
    }

    /**
     * The original content this ProcessingData where created for
     *
     * @return the original content
     */
    public Content getContent() {
        return content;
    }

    /**
     * Getter for the list of the extracted contents (in the order they where
     * added)
     *
     * @return the list of extracted contents. Does NOT include the original
     * content returned by {@link #getContent()}.
     */
    public List<Content> getExtractedContents() {
        return Collections.unmodifiableList(extrContents);
    }

    public void addExtractedContent(Content content) {
        if (content != null) {
            extrContents.add(content);
        }
    }

    private String cleanMimeType(String mime) {
        if (mime == null) {
            mime = "application/octet-stream";
        } else {
            int paramsIdx = mime.indexOf(';');
            if (paramsIdx > 0) {
                mime = mime.substring(0, paramsIdx).trim();
            }
            mime = mime.toLowerCase(Locale.ROOT);
        }
        return mime;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public final String getConfiguration(String key, String defaultValue) {
        return configuration.containsKey(key) ? configuration.get(key).toString() : defaultValue;
    }

    public final boolean getConfiguration(String key, boolean defaultValue) {
        Object value = configuration.get(key);
        if (value instanceof Boolean) {
            return ((Boolean) value);
        } else if (value instanceof String) {
            return Boolean.parseBoolean(value.toString());
        } else {
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    public final List<String> getConfiguration(String key, List<String> defaultValue) {
        return configuration.containsKey(key) && configuration.get(key) instanceof List ? (List<String>) configuration.get(key) : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public final Map<String, List<String>> getConfiguration(String key, Map<String, List<String>> defaultValue) {
        return configuration.containsKey(key) && configuration.get(key) instanceof Map ? (Map<String, List<String>>) configuration.get(key) : defaultValue;
    }

    public final long getConfiguration(String key, long defaultValue) {
        Object value = configuration.get(key);
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return ((Number) value).longValue();
        } else if (value instanceof String || value instanceof Number) {
            try {
                //this will also convert things like 4.0 to 4 but fail for 4.1
                return new BigDecimal(value.toString()).toBigIntegerExact().longValueExact();
            } catch (NumberFormatException | ArithmeticException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public final int getConfiguration(String key, int defaultValue) {
        Object value = configuration.get(key);
        if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return ((Number) value).intValue();
        } else if (value instanceof String || value instanceof Number) {
            try {
                //this will also convert things like 4.0 to 4 but fail for 4.1
                return new BigDecimal(value.toString()).toBigIntegerExact().intValueExact();
            } catch (NumberFormatException | ArithmeticException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public final double getConfiguration(String key, double defaultValue) {
        Object value = configuration.get(key);
        if (value instanceof Double || value instanceof Float) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String || value instanceof Number) {
            try {
                //this will also convert things like 4.0 to 4 but fail for 4.1
                return new BigDecimal(value.toString()).doubleValue();
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public final float getConfiguration(String key, float defaultValue) {
        Object value = configuration.get(key);
        if (value instanceof Float) {
            return ((Number) value).floatValue();
        } else if (value instanceof String || value instanceof Number) {
            try {
                //this will also convert things like 4.0 to 4 but fail for 4.1
                return new BigDecimal(value.toString()).floatValue();
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }


    /**
     * Looksup the language using the following order
     * <ol>
     * <li> explicitly parsed document language by looking up the {@link #getConfiguration()}
     * for the {@link Configuration#LANGUAGE} property.
     * <li> extracted languages by calling {@link #getAnnotation(Annotation)} for the {@link Annotations#LANGUAGE} annotation
     * </ol>
     *
     * @return the language or <code>null</code> if not present in any of the above locations
     */
    public String getLanguage() {
        String language = getConfiguration(ProcessingData.Configuration.LANGUAGE, (String) null);
        if (language != null) { //look if we have detected the lanugage (already)
            return language;
        }
        return getAnnotation(Annotations.LANGUAGE);
    }

    /**
     * Similar to {@link #getLanguage()}, but returns potentially multiple detected languages with their probability.
     *
     * @return the list of detected languages or an empty list if no language was detected
     */
    public List<Value<String>> getLanguages() {
        String parsedLang = getConfiguration(ProcessingData.Configuration.LANGUAGE, (String) null);
        if (parsedLang != null) {
            return Collections.singletonList(Value.value(parsedLang));
        }
        return getValues(Annotations.LANGUAGE);
    }

    @Override
    public void close() throws IOException {
        content.close();
        extrContents.stream().forEach(IOUtils::closeQuietly);
    }

    /**
     * Defines well known configuration keys as used with {@link ProcessingData#getConfiguration()}
     *
     * @author Rupert Westenthaler
     */
    public static final class Configuration {

        private Configuration() {
            throw new IllegalStateException("Do not use reflection to create instances of this class");
        }

        public static final String LANGUAGE = "language";

        /**
         * The temporal context used to parse the data (value should be a {@link Date} or parseable to a date)
         */
        public static final String TEMPORAL_CONTEXT = "context.temporal";

    }

}
