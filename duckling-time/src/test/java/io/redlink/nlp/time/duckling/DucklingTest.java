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
package io.redlink.nlp.time.duckling;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.LazySeq;
import clojure.lang.PersistentArrayMap;

/**
 *
 */
public class DucklingTest {

    public static void main(String [] args) throws IOException {
        final IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("duckling.core"));

        final IFn load = Clojure.var("duckling.core", "load!");
        load.invoke();
        
        final IFn parse = Clojure.var("duckling.core", "parse");

        // Reference-Time
        final IFn time = Clojure.var("duckling.time.obj", "t");
        final Object reftime = time.invoke(-2, 2013, 2, 13, 4, 30, 0);

        final IPersistentMap context = PersistentArrayMap.create(new HashMap<>());

        final IPersistentMap assoc = context.assoc(Clojure.read(":reference-time"), reftime);

        final Object o = parse.invoke("de$core", "now", Clojure.read("[:time]"), assoc);

        final Iterator<PersistentArrayMap> map = ((LazySeq)o).iterator();

        while(map.hasNext()) {
            PersistentArrayMap m = map.next();
            if (":time".equals(String.valueOf(m.valAt(Clojure.read(":dim"))))) {
                System.out.println(m);
            }
        }


        final long now = System.currentTimeMillis();
        System.out.printf("Done%n");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.printf("Shutdown after %d millis%n", System.currentTimeMillis() - now);
            }
        });
    }
}

