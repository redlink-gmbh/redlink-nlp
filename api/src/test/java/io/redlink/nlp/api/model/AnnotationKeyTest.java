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

package io.redlink.nlp.api.model;

import org.junit.Assert;
import org.junit.Test;

public class AnnotationKeyTest {

    static final String TEST_KEY = "test:annotation";

    @AnnotationKey(TEST_KEY)
    public static class AnnotatedTestClass {
    }

    public static class UnannotatedTestClass {
    }


    @Test
    public void testAnnotatedClass() {

        Annotated annotated = new Annotated() {
        };

        annotated.addAnnotation(new AnnotatedTestClass());
        Assert.assertTrue(annotated.getKeys().contains(TEST_KEY));
        Assert.assertNotNull(annotated.getAnnotation(AnnotatedTestClass.class));

        annotated.addAnnotation(new AnnotatedTestClass());
        Assert.assertEquals(2, annotated.getAnnotations(AnnotatedTestClass.class).size());

        annotated.setAnnotation(new AnnotatedTestClass());
        Assert.assertEquals(1, annotated.getAnnotations(AnnotatedTestClass.class).size());

        annotated.removeAnnotations(AnnotatedTestClass.class);
        Assert.assertNull(annotated.getAnnotation(AnnotatedTestClass.class));
    }

    @Test
    public void testUnannotatedClass() {

        Annotated annotated = new Annotated() {
        };

        annotated.addAnnotation(new UnannotatedTestClass());
        Assert.assertTrue(annotated.getKeys().contains(UnannotatedTestClass.class.getName()));
        Assert.assertNotNull(annotated.getAnnotation(UnannotatedTestClass.class));

        annotated.addAnnotation(new UnannotatedTestClass());
        Assert.assertEquals(2, annotated.getAnnotations(UnannotatedTestClass.class).size());

        annotated.setAnnotation(new UnannotatedTestClass());
        Assert.assertEquals(1, annotated.getAnnotations(UnannotatedTestClass.class).size());

        annotated.removeAnnotations(UnannotatedTestClass.class);
        Assert.assertNull(annotated.getAnnotation(UnannotatedTestClass.class));
    }

}
