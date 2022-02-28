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

package io.redlink.nlp.stanza;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redlink.nlp.api.ProcessingException;
import io.redlink.nlp.api.Processor;
import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.Chunk;
import io.redlink.nlp.model.NlpAnnotations;
import io.redlink.nlp.model.Sentence;
import io.redlink.nlp.model.Token;
import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.model.pos.PosTag;
import io.redlink.nlp.model.tag.TagSet;
import io.redlink.nlp.model.util.NlpUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * The Named Entity {@link Preprocessor} used for extracting named entities
 * from processed documents.
 *
 * @author rupert.westenthaler@redlink.co
 */
@Component
@ConditionalOnProperty(StanzaConfiguration.STANZA_URL)
@EnableConfigurationProperties(StanzaConfiguration.class)
public class StanzaProcessor extends Processor {

    private static final Logger LOG = LoggerFactory.getLogger(StanzaProcessor.class);

    private final StanzaConfiguration config;

    private final ObjectMapper mapper;

    private final CloseableHttpClient httpClient;

    private final Map<String, PosTag> unmappedUPosTags;
    private final Map<String, Map<String, PosTag>> unmappedXPosTags;
    private final Map<String, NerTag> unmappedNerTags;

    @Autowired
    public StanzaProcessor(StanzaConfiguration config, ObjectMapper mapper) {
        super("stanza", "Stanza NLP", Phase.pos); //this does token, sent, pos and ner
        this.config = config;
        this.mapper = mapper;
        httpClient = HttpClientBuilder.create().build();
        unmappedUPosTags = new HashMap<>();
        unmappedXPosTags = new HashMap<>();
        unmappedNerTags = new HashMap<>();
    }

    @Override
    public Map<String, Object> getDefaultConfiguration() {
        return new HashMap<>();
    }

    protected void init() {
        //We should add Webserivces to the stanza endpoint that allows to retrieve
        //supported languages and annotation pipeline information
    }

    @Override
    protected void doProcessing(io.redlink.nlp.api.ProcessingData processingData) throws ProcessingException {
        LOG.debug("> process {} with {}", processingData, getClass().getSimpleName());

        AnalyzedText at = NlpUtils.getOrInitAnalyzedText(processingData);
        if (at == null) {
            LOG.debug("Unable to process {} because no palin/text content is present", processingData);
            return;
        }

        String language = processingData.getLanguage();
        if (!config.supports(language)) {
            LOG.debug("Unable to preprocess {} because language '{}' is not supported (supported: {})",
                    processingData, language, config.getLangs());
            return;
        }
        LOG.debug(" - language: {}", language);
        if (language == null || language.length() < 2) {
            LOG.warn("Unable to process {} because missing/invalid language {}",
                    processingData, language);
            return;
        }

        HttpPost req = new HttpPost(config.getUrl());
        Map<String, String> reqData = new HashMap<>();
        reqData.put("lang", language);
        reqData.put("text", at.getSpan());
        req.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        req.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        try {
            req.setEntity(new ByteArrayEntity(mapper.writeValueAsBytes(reqData)));
        } catch (JsonProcessingException e) {
            throw new ProcessingException(String.format("Unable to create JSON Response (reason: %s", e.getMessage()),
                    e, this, processingData);
        }
        try {
            httpClient.execute(req, new AnalysisResponseHandler(language, at));
        } catch (IOException e) {
            throw new ProcessingException(String.format("Error wile Stanza Analyis Request (endpoint: %s, reason: %s",
                    config.getUrl(), e.getMessage()), e, this, processingData);
        }
    }

    /**
     * Response Handler for Stanza Annotation results that adds the
     * corresponding Redlink NLP annotations directly to the Analyzed Text
     */
    class AnalysisResponseHandler implements ResponseHandler<Void> {

        final String language;
        final AnalyzedText at;
        final Map<String, StanzaToken> tokens;
        final Map<String, StanzaWord> words;

        public AnalysisResponseHandler(String language, AnalyzedText at) {
            this.language = Objects.requireNonNull(language);
            this.at = Objects.requireNonNull(at);
            tokens = new HashMap<>();
            words = new HashMap<>();
        }

        @Override
        public Void handleResponse(HttpResponse response) throws IOException {

            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                try (InputStream in = response.getEntity().getContent()) {
                    processAnnptations(in);
                }
            } else {
                throw new ClientProtocolException(String.format(
                        "Unexpected response status '%s' ", response.getStatusLine()));
            }
            return null;
        }

        /**
         * Stream processing of the top level annotations. Object mapping for
         * {@link StanzaSentence} and {@link StanzaEntity}
         *
         * @param in
         */
        private void processAnnptations(InputStream in) throws IOException {
            try (JsonParser parser = mapper.getFactory().createParser(in)) {
                if (JsonToken.START_OBJECT != parser.nextToken()) {
                    throw new IOException(String.format("Unexpected response format: Root is expected to be an Object (was: '%s' )!", parser.currentToken()));
                }
                while (parser.nextToken() != null) {
                    String fieldname = parser.getCurrentName();
                    if ("sentences".equals(fieldname)) {
                        consumeArray(parser, StanzaSentence.class, this::annotateSentence);
                    } else if ("entities".equals(fieldname)) {
                        consumeArray(parser, StanzaEntity.class, this::annotateEntity);
                    } else if (!parser.getCurrentToken().isStructEnd()) {
                        consumeTree(parser); //consume unknown trees
                    } //else we are done
                }
            }
        }

        /**
         * Parses entries of an array to java beans using the {@link ObjectMapper}
         *
         * @param <T>
         * @param parser   the {@link JsonParser} to read the data from. The parser
         *                 is expected to be at the field with an array as value. In other words
         *                 <code>{@link JsonParser#nextToken()} == JsonToken.START_ARRAY</code>
         * @param type     the type of the elements in the array
         * @param consumer the consumer receiving the parsed instances
         * @throws IOException if <code>{@link JsonParser#nextToken()} != JsonToken.START_ARRAY</code>
         *                     or a {@link ObjectMapper} exception on {@link ObjectMapper#readValue(JsonParser, Class)}
         *                     calls for the items in that array
         */
        private <T> void consumeArray(JsonParser parser, Class<T> type, Consumer<T> consumer) throws IOException {
            String fieldName = parser.getCurrentName();
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException(String.format("Unexpected response format: '%s' value was NOT an array!", fieldName));
            }
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                consumer.accept(mapper.readValue(parser, type));
            }

        }

        /**
         * Consumes the whole json tree under the current position of the json parser
         *
         * @param parser
         */
        private void consumeTree(JsonParser parser) throws IOException {
            int count = 0;
            do {
                JsonToken t = parser.nextToken();
                if (t.isStructStart()) {
                    count++;
                } else if (t.isStructEnd()) {
                    count--;
                }
            } while (count > 0);
        }

        private void annotateSentence(StanzaSentence sent) {
            int start = sent.getTokens().get(0).getStart();
            int end = sent.getTokens().get(sent.getTokens().size() - 1).getEnd();
            Sentence s = at.addSentence(start, end);
            if (sent.getSentiment() != null) {
                s.addAnnotation(NlpAnnotations.SENTIMENT_ANNOTATION, sent.getSentiment());
            }
            sent.getTokens().forEach(this::annotateToken);
            sent.getWords().forEach(this::annotateWord);
        }

        private void annotateToken(StanzaToken token) {
            tokens.put(token.getId(), token);
        }

        private void annotateWord(StanzaWord word) {
            StanzaToken token = tokens.get(word.getToken());
            Objects.requireNonNull(token, String.format("Unresolfable link to Token[id:%s] for %s", word.getToken(), token));
            Token t = at.addToken(token.getStart(), token.getEnd());
            if (word.getPos() != null) {
                t.addAnnotation(NlpAnnotations.POS_ANNOTATION, getPosTag(language, word.getXPos(), word.getPos()));
            }
            if (word.getLemma() != null) {
                t.addAnnotation(NlpAnnotations.LEMMA_ANNOTATION, word.getLemma());
            }
            //TODO: Add support for Word Features
            //MorphoFeatures features = new MorphoFeatures(word.getLemma());
            //t.addAnnotation(NlpAnnotations.MORPHO_ANNOTATION, features);
        }

        private void annotateEntity(StanzaEntity entity) {
            Chunk chunk = at.addChunk(entity.getStart(), entity.getEnd());
            if (entity.getType() != null) {
                chunk.addAnnotation(NlpAnnotations.NER_ANNOTATION, getNerTag(entity.getType()));
            }
        }


    }

    NerTag getNerTag(String ner) {
        NerTag tag = StanzaConstants.NER_TAG_SET.getTag(ner);
        if (tag == null) {
            LOG.trace("Unmapped NER '{}'", ner);
            tag = unmappedNerTags.computeIfAbsent(ner, NerTag::new);
        }
        return tag;
    }

    PosTag getPosTag(String lang, String xPos, String uPos) {
        TagSet<PosTag> langTagSet = StanzaConstants.TAG_SETS.get(lang);
        PosTag tag = null;
        if (langTagSet != null && StringUtils.isNotEmpty(xPos)) {
            tag = langTagSet.getTag(xPos);
            if (tag == null) {
                tag = unmappedXPosTags.computeIfAbsent(lang, l -> new HashMap<>())
                        .computeIfAbsent(xPos, createUnmappedXPosTag(uPos));
            }
        } else if (StringUtils.isNotEmpty(uPos)) {
            tag = StanzaConstants.U_POS.getTag(uPos);
            if (tag == null) {
                LOG.trace("Unmapped POS '{}'", uPos);
                tag = unmappedUPosTags.computeIfAbsent(uPos, PosTag::new);
            }
        }
        return tag;
    }

    /**
     * Creates an unmapped xPos {@link PosTag} (language specific PosTag). Supports
     * using a possible uPos mapping as fallback
     *
     * @param uPos the uPos for the xPos
     * @return the PosTag
     */
    private Function<? super String, ? extends PosTag> createUnmappedXPosTag(String uPos) {
        return xPos -> {
            PosTag uPosTag = StanzaConstants.U_POS.getTag(uPos);
            if (uPosTag != null) { //we have a uPos mapping ... so use it
                return new PosTag(xPos, uPosTag.getCategories(), uPosTag.getPos());
            } else { // create an unmapped PosTag for the xPos
                return new PosTag(xPos);
            }
        };
    }

    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     * Simple Java Beans for the Stanza Server JSON Format
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     * They are used for Jackson ObjectMapper to map Sentences and Entities
     */

    static class StanzaEntity {
        private int start;
        private int end;
        private String text;
        private String type;
        private List<String> tokens;
        private List<String> words;

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getTokens() {
            return tokens;
        }

        public void setTokens(List<String> tokens) {
            this.tokens = tokens;
        }

        public List<String> getWords() {
            return words;
        }

        public void setWords(List<String> words) {
            this.words = words;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + end;
            result = prime * result + start;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StanzaEntity other = (StanzaEntity) obj;
            if (end != other.end)
                return false;
            if (start != other.start)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "StanzaEntity [start=" + start + ", end=" + end + ", text=" + text + ", type=" + type + ", tokens="
                    + tokens + ", words=" + words + "]";
        }

    }

    static class StanzaSentence {

        private String text;
        private Double sentiment;
        private List<StanzaToken> tokens = new LinkedList<>();
        private List<StanzaWord> words = new LinkedList<>();

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<StanzaToken> getTokens() {
            return tokens;
        }

        public void setTokens(List<StanzaToken> tokens) {
            this.tokens = tokens;
        }

        public List<StanzaWord> getWords() {
            return words;
        }

        public void setWords(List<StanzaWord> words) {
            this.words = words;
        }

        public Double getSentiment() {
            return sentiment;
        }

        public void setSentiment(Double sentiment) {
            this.sentiment = sentiment;
        }

        @Override
        public String toString() {
            return "StanzaSentence [text=" + StringUtils.abbreviate(text, 40) + ", sentiment=" + sentiment + "]";
        }

    }

    static class StanzaToken {
        private String id;
        private String text;
        private int start;
        private int end;
        private String ner;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public String getNer() {
            return ner;
        }

        public void setNer(String ner) {
            this.ner = ner;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StanzaToken other = (StanzaToken) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "StanzaToken [id=" + id + ", text=" + text + ", start=" + start + ", end=" + end + ", ner=" + ner
                    + "]";
        }

    }

    static class StanzaWord {

        private String id;
        private String text;
        private String token;
        private String pos;
        @JsonProperty("xpos")
        private String xpos;
        private String lemma;
        private String features;
        private String misc;

        private Map<String, String> featureMap = new HashMap<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

        public String getXPos() {
            return xpos;
        }

        public void setXPos(String xpos) {
            this.xpos = xpos;
        }

        public String getLemma() {
            return lemma;
        }

        public void setLemma(String lemma) {
            this.lemma = lemma;
        }

        public String getFeatures() {
            return features;
        }

        public void setFeatures(String features) {
            this.features = features;
            if (features != null) {
                featureMap = Arrays.stream(StringUtils.split(features, '|'))
                        .filter(e -> {
                            int idx = e.indexOf('=');
                            return idx > 0 && idx < e.length() - 1;
                        })
                        .collect(Collectors.toMap(
                                e -> e.substring(0, e.indexOf('=')),
                                e -> e.substring(e.indexOf('=') + 1)));
            } else {
                featureMap.clear();
            }
        }

        @JsonIgnore
        public String getFeature(String key) {
            return featureMap.get(key);
        }

        @JsonIgnore
        public boolean hasFeature(String key) {
            return featureMap.containsKey(key);
        }

        public String getMisc() {
            return misc;
        }

        public void setMisc(String misc) {
            this.misc = misc;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StanzaWord other = (StanzaWord) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "StanzaWord [id=" + id + ", text=" + text + ", token=" + token + ", pos=" + pos + ", lemma=" + lemma
                    + ", features=" + features + ", misc=" + misc + "]";
        }

    }
}
