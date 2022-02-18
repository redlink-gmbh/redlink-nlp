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

package io.redlink.nlp.regex.ner.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.regex.ner.vocab.VocabularyDetector;
import io.redlink.nlp.regex.ner.vocab.VocabularyEntry;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Variant of the {@link VocabularyDetector} that reads a Vocabulary from a
 * CSV file where the first column is the name and all further columns are
 * synonyms
 * 
 * @author Rupert Westenthaler
 *
 */
public abstract class CsvVocabularyNerDetector extends VocabularyDetector {

    private final CSVFormat csvFormat;

    protected CsvVocabularyNerDetector(String name, NerTag type, Locale lang, CaseSensitivity cs) {
        this(name, type, lang, cs, CSVFormat.DEFAULT);
    }

    protected CsvVocabularyNerDetector(String name, NerTag type, Locale lang, CaseSensitivity cs, CSVFormat csvFormat) {
        super(name, type, lang, cs);
        this.csvFormat = csvFormat;
    }
    @Override
    protected final Collection<VocabularyEntry> loadEntries() throws IOException {
        Map<String,VocabularyEntry> entries = new HashMap<>();
        try (CSVParser csv = new CSVParser(readFrom(), csvFormat)){
            nextEntry: for(Iterator<CSVRecord> records = csv.iterator(); records.hasNext();){
                CSVRecord record = records.next();
                VocabularyEntry entry = null;
                for(String name : record){
                    String normName = normalize(name);
                    if(entry == null){ //lookup/create a record for this name
                        if(normName == null){
                            log.warn("Line {}: Invalid name '{}' (record data: {})",record.getRecordNumber(), name, record);
                            continue nextEntry;
                        }
                        entry = entries.get(normName);
                        if(entry == null){
                            entry = new VocabularyEntry(name);
                            entries.put(normName, entry);
                        } else {
                            log.info("Line {}: Entry with normalized name '{}' already present - will merge synoms to existing entry",
                                    record.getRecordNumber(), normName);
                        }
                    } else { //record already present add as synonym
                        if(normName != null){
                            entry.addSynonym(name);
                        } //empty synonym
                    }
                }
            }
        }
        return entries.values();
    }
    
    protected abstract Reader readFrom();

}
