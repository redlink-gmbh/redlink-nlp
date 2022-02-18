/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.redlink.nlp.model.json.valuetype.impl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.redlink.nlp.model.AnalyzedText;
import io.redlink.nlp.model.json.JsonUtils;
import io.redlink.nlp.model.json.valuetype.ValueTypeParser;
import io.redlink.nlp.model.json.valuetype.ValueTypeSerializer;
import io.redlink.nlp.model.pos.LexicalCategory;
import io.redlink.nlp.model.pos.Pos;
import io.redlink.nlp.model.pos.PosTag;

@Component
public class PosTagSupport implements ValueTypeParser<PosTag>, ValueTypeSerializer<PosTag> {

    public static final String TYPE_VALUE = "org.apache.stanbol.enhancer.nlp.pos.PosTag";

    final Map<PosTagInfo, PosTag> posTagCache;

    public PosTagSupport() {
        posTagCache = Collections.synchronizedMap(
            new LinkedHashMap<PosTagInfo,PosTag>(16,0.75f,true){
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<PosTagInfo,PosTag> arg0) {
                    return size() > 1024;
                }
        });
    }

    @Override
    public String getKey() {
        return TYPE_VALUE;
    }
    
    @Override
    public Class<PosTag> getType() {
        return PosTag.class;
    }
    
    @Override
    public PosTag parse(ObjectNode jValue, AnalyzedText at) {
        PosTagInfo tagInfo = new PosTagInfo();
        JsonNode tag = jValue.path("tag");
        if(!tag.isTextual()){
            throw new IllegalStateException("Unable to parse PosTag. The value of the "
                    +"'tag' field MUST have a textual value (json: "+jValue+")");
        }
        tagInfo.tag = tag.asText();
        if(jValue.has("lc")){
            tagInfo.categories = JsonUtils.parseEnum(jValue, "lc",LexicalCategory.class);
        } else {
            tagInfo.categories = EnumSet.noneOf(LexicalCategory.class);
        }
        if(jValue.has("pos")){
            tagInfo.pos = JsonUtils.parseEnum(jValue, "pos", Pos.class);
        } else {
            tagInfo.pos = EnumSet.noneOf(Pos.class);
        }
        PosTag posTag = posTagCache.get(tagInfo);
        if(posTag == null){
            posTag = new PosTag(tagInfo.tag,tagInfo.categories,tagInfo.pos);
            posTagCache.put(tagInfo, posTag);
        }
        return posTag;
    }



    @Override
    public ObjectNode serialize(ObjectMapper mapper, PosTag value){
        ObjectNode jPosTag = mapper.createObjectNode();
        jPosTag.put("tag", value.getTag());
        if(value.getPos().size() == 1){
            jPosTag.put("pos",value.getPos().iterator().next().ordinal());
        } else if(!value.getPos().isEmpty()){
            ArrayNode jPos = mapper.createArrayNode();
            for(Pos pos : value.getPos()){
                jPos.add(pos.ordinal());
            }
            jPosTag.set("pos", jPos);
        }
        if(!value.getCategories().isEmpty()){
            //we need only the categories not covered by Pos elements
            EnumSet<LexicalCategory> categories = EnumSet.noneOf(LexicalCategory.class);
            categories.addAll(value.getCategories());
            for(Pos pos : value.getPos()){
                categories.removeAll(pos.categories());
            }
            if(categories.size() == 1){
                jPosTag.put("lc",categories.iterator().next().ordinal());
            } else if(!categories.isEmpty()){
                ArrayNode jCategory = mapper.createArrayNode();
                for(LexicalCategory lc : categories){
                    jCategory.add(lc.ordinal());
                }
                jPosTag.set("lc", jCategory);
            }
        }
        return jPosTag;
    }
    
    private class PosTagInfo {
        
        protected String tag;
        protected EnumSet<LexicalCategory> categories;
        protected EnumSet<Pos> pos;
        
        @Override
        public int hashCode() {
            return tag.hashCode()+(categories != null ? categories.hashCode() : 0)+
                    (pos != null ? pos.hashCode() : 0);
        }
        @Override
        public boolean equals(Object obj) {
            return obj instanceof PosTagInfo && tag.equals(tag) && 
                    categories.equals(categories) && pos.equals(pos);
        }
        
    }
    
}
