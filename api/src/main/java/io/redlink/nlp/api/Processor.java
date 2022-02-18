/*
 * Copyright (c) 2016-2022 Redlink GmbH.
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 */
public abstract class Processor implements Comparable<Processor> {

    @SuppressWarnings("java:S115")
    public enum Phase {
        /**
         * pre-processing of the parsed content
         */
        pre(-500),
        /**
         * content-type detection
         */
        contentTypeDetect(-450),
        /**
         * content conversation (e.g. rich-text document to PDF)
         */
        contentConversion(-400),
        /**
         * OCR
         */
        ocr(-300),
        /**
         * processing (plain text extraction and information extraction of rich-text documents)
         */
        richTextDocProcessing(-200),
        /**
         * Natural Language Processing phase
         */
        nlp(-100),
        /**
         * Language detection
         */
        langDetect(-90),
        /**
         * Part-of-Speech tagging
         */
        pos(-50),
        /**
         * Lemmatization (same as {@link #stem} and {@link #stopword})
         */
        lemma(-40),
        /**
         * Stemming (same as {@link #lemma}) and {@link #stopword})
         */
        stem(-40),
        /**
         * Stopword detection  (same as {@link #lemma} and {@link #stem})
         */
        stopword(-40),
        /**
         * Negation detection
         */
        negation(-30),
        /**
         * Named Entity Recognition (same as {@link #entityLinking})
         */
        ner(-20),
        /**
         * Entity Linking (same as {@link #ner})
         */
        entityLinking(-20),
        /**
         * default extraction (after {@link #pre} and {@link #nlp} but before {@link #post})
         */
        extraction(0),
        /**
         * Post processing phase
         */
        post(100);


        private int weight;

        Phase(int w) {
            weight = w;
        }
    }

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final CountDownLatch initComplete;
    private Exception initException = null;
    private final AtomicBoolean initExceptionRecorded = new AtomicBoolean(false);
    private long startupTime = -1;

    @Autowired(required = false)
    @Qualifier(StartupConfiguration.STARTUP_THREAD_POOL_NAME)
    private Optional<ExecutorService> executorService = Optional.empty();

    private final int weight;
    private final String key;
    private final String name;

    private final String enabledKey;

    private Processor(String key, String name, int weight) {
        this.key = key;
        this.enabledKey = key + ".enabled";
        this.name = name;
        this.weight = weight;
        initComplete = new CountDownLatch(2);

    }

    protected Processor(String key, String name, Phase phase) {
        this(key, name, phase.weight);
    }

    protected Processor(String key, String name, Phase pahse, int weight) {
        this(key, name, pahse.weight + weight);
    }

    @PostConstruct
    protected final void postConstruct() {
        initComplete.countDown();
        final long bootTime = System.currentTimeMillis();
        executorService.orElseGet(Executors::newSingleThreadExecutor)
                .execute(() -> {
                    try {
                        init();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupted during initialization of {}", getClass().getSimpleName());
                        initException = e;
                    } catch (final Exception t) {
                        log.error("Error while initializing {}: {}", getClass().getSimpleName(), t.getMessage());
                        initException = t;
                    } finally {
                        startupTime = System.currentTimeMillis() - bootTime;
                        log.debug("Initialisation of {} took {}ms", getClass().getSimpleName(), startupTime);
                        initComplete.countDown();
                    }
                });
    }

    protected abstract void init() throws Exception;

    /**
     * The unique key of this processor (typcially <code>nlp.{processor-name}</code>
     *
     * @return the key used to identify this processor
     */
    public String getKey() {
        return key;
    }

    /**
     * The human readable name for this processor
     */
    public String getName() {
        return name;
    }


    /**
     * processes the parsed data
     *
     * @param processingData the data to process
     */
    public final void process(ProcessingData processingData) throws ProcessingException {
        //check if we are running outside Spring
        if (initComplete.getCount() > 1) { //so call postConstruct manually
            synchronized (this) { //but ensure to call postConstruct only once
                if (initComplete.getCount() > 1) { //running outside Spring ...
                    postConstruct();
                }

            }
        }
        if (processingData.getConfiguration(enabledKey, true)) { //check if enabled
            log.trace("call {} (name: {}) for {}", getKey(), getName(), processingData);
            //await initialization
            try {
                initComplete.await();
                if (initException != null) {
                    if (initExceptionRecorded.compareAndSet(false, true)) {
                        throw new IllegalStateException("Error during initialisation", initException);
                    } else {
                        return;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted during initialisation", e);
            }

            doProcessing(processingData);
        } else {
            log.trace("{} (name: {}) disabled", getKey(), getName());
        }
    }

    /**
     * Actual processors need to do the processing in here
     *
     * @param processingData the data to process
     */
    protected abstract void doProcessing(ProcessingData processingData) throws ProcessingException;

    /**
     * Returns a map with all supported configuration parameters as key
     * and the default value as value.
     *
     * @return the map with the default configuration for the processor
     */
    public abstract Map<String, Object> getDefaultConfiguration();

    @Override
    public int compareTo(Processor other) {
        return Integer.compare(this.weight, other.weight);
    }

    @Override
    public String toString() {
        return "Processor [key=" + key + ", name=" + name + ", impl=" + getClass().getSimpleName() + "]";
    }

}
