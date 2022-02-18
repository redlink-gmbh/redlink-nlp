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

package io.redlink.nlp.langdetect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

@Component
public class LanguageIdentifier {
    
    private static final String PROFILE_PATH = "profiles";

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private Charset UTF8 = Charset.forName("UTF-8");
    
    private final static String[] LANGUAGES = new String [] {"af", "ar", "bg", 
            "bn", "cs", "da", "de", "el", "en", "es", "et", "fa", "fi", "fr", 
            "gu", "he", "hi", "hr", "hu", "id", "it", "ja", "kn", "ko", "lt", 
            "lv", "mk", "ml", "mr", "ne", "nl", "no", "pa", "pl", "pt", "ro", 
            "ru", "sk", "sl", "so", "sq", "sv", "sw", "ta", "te", "th", "tl", 
            "tr", "uk", "ur", "vi", "zh-cn", "zh-tw"};
    
    private final List<String> languageProfiles;
    
    public LanguageIdentifier() {
        languageProfiles = Collections.unmodifiableList(loadProfiles(PROFILE_PATH));
        if(languageProfiles == null || languageProfiles.isEmpty()){
            throw new IllegalStateException("Unable to find any language profiles under '"
                    + PROFILE_PATH +"'");
        }
    }
    
    public void loadProfiles() throws LangDetectException {
        DetectorFactory.clear();
        try {
            DetectorFactory.loadProfile(languageProfiles);
        } catch (Exception e) {
            throw new LangDetectException(null, "Error in Initialization: "+e.getMessage());
        } 
    }
    /**
     * Load the profiles from the classpath
     * @param path where the profiles are
     * @return a list of profiles
     * @throws Exception
     */
    private List<String> loadProfiles(String path) {
        String pathFormat = path+"/%s";
        List<String> profiles = new ArrayList<>(LANGUAGES.length);
        for (String lang: LANGUAGES) {
            String profileFile = String.format(pathFormat, lang);
            InputStream is = getClass().getClassLoader().getResourceAsStream(profileFile);
            if(is != null){
                try {
                    String profile = IOUtils.toString(is, UTF8);
                    if(StringUtils.isNotBlank(profile)){
                        profiles.add(profile);
                    }
                } catch (IOException e) {
                    log.warn("Unable to load Langauge Detection Profile for language '"+lang+"'", e);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            } else {
                log.warn("Missing Language Detection Profile for language profile "
                        + "for language '{}' (resource: {})",lang, profileFile);
            }
        }
        return profiles;
    }
    
    public List<Language> getLanguages(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.getProbabilities();
    }

}
