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
import org.apache.commons.lang3.StringUtils;

import io.redlink.nlp.model.ner.NerTag;
import io.redlink.nlp.regex.ner.NamedRegexDetector;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Variant of the {@link VocabularyDetector} that reads a Vocabulary from a
 * CSV file where the first column is the name and all further columns are
 * synonyms
 * 
 * @author Rupert Westenthaler
 *
 */
public abstract class CsvNamedPatternNerDetector extends NamedRegexDetector {

    private int flags = 0;
    private final String lang;
    
    protected CsvNamedPatternNerDetector(NerTag type, String lang, boolean caseSensitive){
        super(type);
        if(!caseSensitive){
            flags = flags | Pattern.CASE_INSENSITIVE;
        }
        this.lang = lang;
    }
    
    protected final Map<String, List<NamedPattern>> loadPatterns() throws IOException {
        List<NamedPattern> patterns = new LinkedList<>();
        try (CSVParser csv = new CSVParser(readFrom(),CSVFormat.DEFAULT)){
            nextPattern: for(Iterator<CSVRecord> records = csv.iterator(); records.hasNext();){
                CSVRecord record = records.next();
                String patternName = null;
                for(String column : record){
                    if(patternName == null){ //lookup/create a record for this name
                        if(StringUtils.isBlank(column)){
                            log.warn("Line {}: Invalid name '{}' (record data: {})",record.getRecordNumber(), column, record);
                            continue nextPattern;
                        } else {
                            patternName = StringUtils.trim(column);
                        }
                    } else { //name already present ... this is a regex pattern
                        try{
                            NamedPattern pattern = new NamedPattern(patternName, Pattern.compile(column, flags));
                            patterns.add(pattern);
                        } catch(IllegalArgumentException e){
                            log.warn("Unable to parse Regex Pattern form '{}' (row: {} | name: {})", 
                                    column, record.getRecordNumber(),patternName);
                        }
                    }
                }
            }
        }
        return Collections.singletonMap(lang, patterns);
    }

    protected abstract Reader readFrom();
    

}
