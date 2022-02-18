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

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = StanzaConfiguration.PREFIX)
public class StanzaConfiguration {

    static final String PREFIX = "nlp.stanza";
    public static final String STANZA_URL = PREFIX + ".url";
    
    private URI url = null;
    private Set<String> langs = new HashSet<>();
    
    
    public URI getUrl() {
        return url;
    }
    
    public void setUrl(URI url) {
        this.url = url;
    }
    
    public Set<String> getLangs() {
        return langs;
    }
    
    public void setLangs(String langs) {
        this.langs = langs == null ? new HashSet<>() : 
            Arrays.stream(StringUtils.split(langs, ','))
                .filter(StringUtils::isNotEmpty)
                .map(l -> l.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public boolean supports(String lang) {
        return lang != null && (langs.isEmpty() || langs.contains(lang.toLowerCase(Locale.ROOT)));
    }
}
