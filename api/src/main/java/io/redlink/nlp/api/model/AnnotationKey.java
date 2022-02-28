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

package io.redlink.nlp.api.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to annotate the annotation key to be used for a Class.
 * This annotation ensures that the key specified as {@link #value()}
 * is used when instances of the annotated class are used with
 * the following {@link Annotated} methods:<ul>
 * <li> {@link Annotated#addAnnotation(Object)}
 * <li> {@link Annotated#setAnnotation(Object)}
 * <li> {@link Annotated#getAnnotation(Class)}
 * <li> {@link Annotated#removeAnnotations(Class)}
 * </ul>
 *
 * @author Rupert Westenthaler
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnotationKey {

    String value();

}
