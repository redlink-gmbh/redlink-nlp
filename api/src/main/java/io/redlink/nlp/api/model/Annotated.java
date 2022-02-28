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

import io.redlink.nlp.api.annotation.Annotations;
import io.redlink.nlp.api.annotation.Keyword;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base class for objects that can be {@link Annotated} with {@link Value}s
 * <p>
 * This class allows to add annotations: <ol>
 * <li> by {@link Annotation} definitions. Those are typically defined as constants (e.g
 * {@link Annotations#KEYWORD} defines an annotation providing {@link Keyword}s extracted
 * form a document). This is the preferred way to work with annotations as it provides
 * type safety while working with annotations.
 * <li> by the type of the annotation value. In this case class level {@link AnnotationKey}
 * annotation are used to determine the key. If such an annotation is not present the full
 * qualified {@link Class#getName() name} is used instead. While this option also provides
 * type safety it is limited to a single key per annotation value type.
 * <li> finally {@link String} keys can be used to store any kind of values. This method of
 * managing annotations does not provide any kind of type safety and SHOULD BE only use as
 * fallback (e.g. for methods that need to process any annotation)
 * </ol>
 * <p>
 * This class allows to add annotations with and without an probability:<ul>
 * <li> all <code>#addAnnotations?()</code> methods allow to directly add the annotation values. Those will be
 * wrapped with an {@link Value} and have an {@link Value#UNKNOWN_PROBABILITY}.
 * <li> all <code>#addValues?()</code> methods accept {@link Value}s. Those need to be used if
 * the annotation does have a given probability (e.g. annotations originating from a machine learning component)
 * </ul>
 * <p>
 * This class supports single and multi valued annotations:<ul>
 * <li> all plural name methods do accept/return a {@link List} of annotation values.
 * <li> all singular name methods do accept/return a single annotation value
 * <li> values are ordered by {@link Value#probability()}. The highest probability comes first and
 * {@link Value#UNKNOWN_PROBABILITY} is returned last. For values with the same probability the
 * ordering of the addition is kept.
 * </ul>
 * <p>
 * This class <code>add**(..)</code> methods will append parsed values. The <code>set**(..)</code>
 * methods will replace existing values. Calling a <code>set**(..)</code> with a <code>null</code>
 * value will remove an annotation. The exception is {@link #setAnnotation(Object)} as this method
 * can not determine the key to remove if the parsed value is <code>null</code>. Therefore a
 * {@link #removeAnnotations(Class)}
 *
 * @author Rupert Westenthaler
 */
public abstract class Annotated {

    private Map<String, Object> annotations;

    /**
     * Getter for all keys used by Annotations
     *
     * @return the Set with all keys. An empty Set if none
     */
    @SuppressWarnings("unchecked")
    public Set<String> getKeys() {
        return annotations == null ? Collections.EMPTY_SET : annotations.keySet();
    }

    /**
     * Method for requesting the annotation of a given Key. This allows to request
     * Values without an {@link Annotation}.
     *
     * @param key the Key
     * @return the annotation or <code>null</code> if not present
     */
    public final Object getAnnotation(String key) {
        Value<?> value = getValue(key);
        return value == null ? null : value.value();
    }

    /**
     * Method for requesting Values of a given Key. This allows to request
     * Values without an {@link Annotation}.
     *
     * @param key the Key
     * @return the Value with the highest probability
     */
    public final Value<?> getValue(String key) {
        if (annotations == null) {
            return null;
        }
        Object value = annotations.get(key);
        if (value instanceof Value<?>) {
            return (Value<?>) value;
        } else if (value != null) {
            return ((List<Value<?>>) value).get(0);
        } else {
            return null;
        }
    }

    /**
     * Method for requesting annotations for a given key.
     *
     * @param key the Key
     * @return all Value sorted by probability
     */
    public final List<Object> getAnnotations(String key) {
        return getValues(key).stream().map(v -> v.value()).collect(Collectors.toList());
    }

    /**
     * Method for requesting Values of a given Key. This allows to request
     * Values without an {@link Annotation}.
     *
     * @param key the Key
     * @return all Value sorted by probability
     */
    @SuppressWarnings("unchecked")
    public final List<Value<?>> getValues(String key) {
        if (annotations == null) {
            return Collections.emptyList();
        }
        Object value = annotations.get(key);
        if (value instanceof Value<?>) {
            List<?> singleton = Collections.singletonList((Value<?>) value);
            return (List<Value<?>>) singleton;
        } else if (value != null) {
            return Collections.unmodifiableList((List<Value<?>>) value);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Method for requesting the annotation.
     *
     * @param annotation the requested {@link Annotation}
     * @return the annotation with the highest probability
     * @throws ClassCastException if values of {@link Annotation#getKey()} are
     *                            not of type V
     */
    public final <V> V getAnnotation(Annotation<V> annotation) {
        Value<V> value = getValue(annotation);
        return value == null ? null : value.value();
    }

    /**
     * Method for requesting the Value of an Annotation.
     *
     * @param annotation the requested {@link Annotation}
     * @return the Value with the highest probability
     * @throws ClassCastException if values of {@link Annotation#getKey()} are
     *                            not of type V
     */
    @SuppressWarnings("unchecked")
    public final <V> Value<V> getValue(Annotation<V> annotation) {
        if (annotations == null) {
            return null;
        }
        Object value = annotations.get(annotation.getKey());
        if (value instanceof Value<?>) {
            return (Value<V>) value;
        } else if (value != null) {
            return ((List<Value<V>>) value).get(0);
        } else {
            return null;
        }
    }

    /**
     * Method for requesting all annotations.
     *
     * @param annotation the requested {@link Annotation}
     * @return all Values sorted by probability
     * @throws ClassCastException if the returned value of
     *                            {@link Annotation#getKey()} is not of type V
     */
    public final <V> List<V> getAnnotations(Annotation<V> annotation) {
        return getValues(annotation).stream().map(v -> v.value()).collect(Collectors.toList());
    }

    /**
     * Method for requesting all values of an Annotation.
     *
     * @param annotation the requested {@link Annotation}
     * @return all Values sorted by probability
     * @throws ClassCastException if the returned value of
     *                            {@link Annotation#getKey()} is not of type V
     */
    @SuppressWarnings("unchecked")
    public final <V> List<Value<V>> getValues(Annotation<V> annotation) {
        if (annotations == null) {
            return Collections.emptyList();
        }
        Object value = annotations.get(annotation.getKey());
        if (value instanceof Value<?>) {
            List<?> singleton = Collections.singletonList((Value<?>) value);
            return (List<Value<V>>) singleton;
        } else if (value != null) {
            return Collections.unmodifiableList((List<Value<V>>) value);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Appends annotations to eventually already existing values. Parsed
     * values will be wrapped in {@link Value}s with unknown probability
     *
     * @param annotation the annotation
     * @param values     the annotation to append
     */
    public <V> void addAnnotations(Annotation<V> annotation, List<V> values) {
        addValuesInternal(annotation.getKey(), values.stream().map(Value::value).collect(Collectors.toList()));
    }

    /**
     * Appends annotation {@link Value}s to eventually already existing one
     *
     * @param annotation the annotation
     * @param values     the values to append
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <V> void addValues(Annotation<V> annotation, List<Value<V>> values) {
        addValuesInternal(annotation.getKey(), (List) values);
    }

    /**
     * Appends the parsed values to the key. This method is intended for internal use (
     * e.g. parsers). Users are encouraged to define type save
     * {@link Annotation} objects and use {@link #addAnnotations(Annotation, List)}
     * instead.
     *
     * @param key    the key
     * @param values the values
     */
    public void addAnnotations(String key, List<?> values) {
        addValuesInternal(key, values.stream().map(Value::value).collect(Collectors.toList()));
    }

    /**
     * Appends the parsed values to the key. This method is intended for internal use (
     * e.g. parsers). Users are encouraged to define type save
     * {@link Annotation} objects and use {@link #addAnnotations(Annotation, List)}
     * instead.
     *
     * @param key    the key
     * @param values the values
     */
    public void addValues(String key, List<Value<?>> values) {
        addValuesInternal(key, values);
    }

    /**
     * Just here because of Java generics combined with Collections ...
     *
     * @param key    the key
     * @param values the values
     */
    @SuppressWarnings("unchecked")
    private void addValuesInternal(String key, List<Value<?>> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        Map<String, Object> map = initAnnotations();
        Object currentValue = annotations.get(key);
        Object newValues;
        if (currentValue == null) {
            if (values.size() == 1) {
                newValues = values.get(0);
            } else {
                values = new ArrayList<>(values); //copy
                Collections.sort(values, Value.PROBABILITY_COMPARATOR); //sort
                newValues = values;
            }
        } else if (currentValue instanceof Value<?>) {
            List<Value<?>> newValuesList = new ArrayList<>(Math.max(4, values.size() + 1));
            newValuesList.add((Value<?>) currentValue);
            newValuesList.addAll(values);
            Collections.sort(newValuesList, Value.PROBABILITY_COMPARATOR); //sort
            newValues = newValuesList;
        } else { //an ArrayList
            ((List<Value<?>>) currentValue).addAll(values);
            Collections.sort((List<Value<?>>) currentValue, Value.PROBABILITY_COMPARATOR); //sort
            newValues = null; //no need to put new values
        }
        if (newValues != null) {
            map.put(key, newValues);
        }
    }

    /**
     * Replaces existing Annotations with the parsed
     *
     * @param annotation the annotation
     * @param values     the values for the annotation
     */
    public <V> void setAnnotations(Annotation<V> annotation, List<V> values) {
        setValuesInternal(annotation.getKey(), values == null ? null : values.stream().map(Value::value).collect(Collectors.toList()));
    }

    /**
     * Replaces existing Annotations with the parsed values
     *
     * @param annotation the annotation
     * @param values     the values for the annotation
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <V> void setValues(Annotation<V> annotation, List<Value<V>> values) {
        setValuesInternal(annotation.getKey(), (List) values);
    }

    /**
     * Replaces existing Values for a key with the parsed one. While this
     * method allows for setting values for String keys users are encouraged
     * to define type save {@link Annotation} and use {@link #setAnnotations(Annotation, List)} instead.
     *
     * @param key    the key
     * @param values the values
     */
    public void setAnnotations(String key, List<?> values) {
        setValuesInternal(key, values == null ? null : values.stream().map(Value::value).collect(Collectors.toList()));
    }

    /**
     * Replaces existing Values for a key with the parsed one. While this
     * method allows for setting values for String keys users are encouraged
     * to define type save {@link Annotation} and use {@link #setValues(Annotation, List)} instead.
     *
     * @param key    the key
     * @param values the values
     */
    public void setValues(String key, List<Value<?>> values) {
        setValuesInternal(key, values);
    }

    /**
     * Just here because of Java generics combined with Collections ...
     *
     * @param key    the key
     * @param values the values
     */
    private void setValuesInternal(String key, List<Value<?>> values) {
        Map<String, Object> map = initAnnotations();
        if (values == null || values.isEmpty()) {
            map.remove(key);
        } else if (values.size() == 1) {
            map.put(key, values.get(0));
        } else {
            //we need to copy, because users might change the parsed Array!
            values = new ArrayList<>(values);
            Collections.sort(values, Value.PROBABILITY_COMPARATOR);
            map.put(key, values);
        }

    }

    private Map<String, Object> initAnnotations() {
        if (annotations == null) { //avoid sync for the typical case
            annotations = new HashMap<String, Object>();
        }
        return annotations;
    }

    /**
     * Adds the object as Annotation. In case the object is
     * annotated with {@link AnnotationKey} the value of this
     * annotation is used as key. If not the full qualified name
     * of the values {@link Class} will be used as key.
     * <p>
     * Use this method only for annotation types that do use a unique key. For
     * types that are used with multiple keys separate {@link Annotation}s
     * with different annotation keys need to be defined and
     * {@link #addAnnotation(Annotation, Object)} needs to be used for adding
     * annotation values.
     *
     * @param annotation the annotation to be added
     */
    public void addAnnotation(Object value) {
        if (value == null) {
            return;
        }
        addValue(getAnnoKey(value), Value.value(value));
    }

    /**
     * Appends an Annotation to eventually already existing values.
     * Parsed <code>V</code> values will be wrapped in {@link Value}
     * with {@link Value#UNKNOWN_PROBABILITY}.
     *
     * @param annotation the annotation
     * @param value      the value to append
     */
    public <V> void addAnnotation(Annotation<V> annotation, V value) {
        addValue(annotation.getKey(), Value.value(value));
    }

    /**
     * Appends an Annotation to eventually already existing values
     *
     * @param annotation the annotation
     * @param value      the value to append
     */
    public <V> void addValue(Annotation<V> annotation, Value<V> value) {
        addValue(annotation.getKey(), value);
    }

    /**
     * Appends an Value to the key. While this
     * method allows for setting values for String keys users are encouraged
     * to define type save {@link Annotation} and use {@link #addAnnotation(Annotation, Object)} instead.
     * instead.
     *
     * @param key   the key
     * @param value the value
     */
    public void addAnnotation(String key, Object value) {
        addValue(key, Value.value(value));
    }

    /**
     * Appends an Value to the key. While this
     * method allows for setting values for String keys users are encouraged
     * to define type save {@link Annotation} and use {@link #addValue(Annotation, Value)} instead.
     *
     * @param key   the key
     * @param value the value
     */
    public void addValue(String key, Value<?> value) {
        if (value != null) {
            Map<String, Object> map = initAnnotations();
            Object currentValue = map.get(key);
            if (currentValue == null) {
                map.put(key, value);
            } else if (currentValue instanceof Value<?>) {
                List<Value<?>> newValues = new ArrayList<>(4);
                newValues.add((Value<?>) currentValue);
                newValues.add(value);
                Collections.sort(newValues, Value.PROBABILITY_COMPARATOR);
                map.put(key, newValues);
            } else { //list
                List<Value<?>> currentValueList = (List<Value<?>>) currentValue;
                //insert the new value at the correct position
                int pos = Collections.binarySearch(currentValueList, value, Value.PROBABILITY_COMPARATOR);
                currentValueList.add(pos >= 0 ? pos : Math.abs(pos + 1), value);
                //no put required
            }
        }
    }

    /**
     * Replaces existing Annotations with the parsed one
     *
     * @param annotation the annotation
     * @param value      the value for the annotation
     */
    public <V> void setAnnotation(Annotation<V> annotation, V value) {
        setValue(annotation, value == null ? null : Value.value(value));
    }

    /**
     * Replaces existing Annotations with the parsed one
     *
     * @param annotation the annotation value
     * @param value      the value for the annotation
     */
    public <V> void setValue(Annotation<V> annotation, Value<V> value) {
        setValue(annotation.getKey(), value);
    }

    /**
     * Sets the annotation to the parsed value. In case the object is
     * annotated with {@link AnnotationKey} the value of this
     * annotation is used as key. If not the full qualified name
     * of the values {@link Class} will be used as key.
     * <p>
     * Use this method only for annotation types that do use a unique key. For
     * types that are used with multiple keys separate {@link Annotation}s
     * with different annotation keys need to be defined and
     * {@link #setAnnotation(Annotation, Object)} needs to be used for adding
     * annotation values.
     *
     * @param value the value
     */
    public void setAnnotation(Object value) {
        if (value == null) {
            return;
        }
        setValue(getAnnoKey(value), Value.value(value));
    }

    /**
     * Replaces existing Values for a key with the parsed one. While this
     * method allows for setting values for String keys users are encouraged
     * to define type save {@link Annotation} and use {@link #setAnnotations(Annotation, Value)} instead.
     *
     * @param key   the key
     * @param value the annotation
     */
    public void setAnnotation(String key, Object value) {
        setValue(key, value == null ? null : Value.value(value));
    }

    /**
     * Replaces existing Values for a key with the parsed one. While this
     * method allows for setting values for String keys users are encouraged
     * to define type save {@link Annotation} and use {@link #setValue(Annotation, Value)} instead.
     *
     * @param key   the key
     * @param value the annotation value
     */
    public void setValue(String key, Value<?> value) {
        if (annotations == null && value == null) {
            return;
        }
        Map<String, Object> map = initAnnotations();
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    /**
     * Getter for the Annotation of the parsed type. This Method considers
     * {@link AnnotationKey} on the parsed type if present. If not the
     * full qualified name of the parsed {@link Class} will be used as key.
     * <p>
     * Use this method only for annotation types that do use a unique key. For
     * types that are used with multiple keys separate {@link Annotation}s need
     * to be defined and {@link #getAnnotation(Annotation)} MUST BE used to
     * retrieve those.
     *
     * @param type the type of the returned annotation.
     * @return the first annotation of the parsed type.
     */
    public <V> V getAnnotation(Class<V> type) {
        return getAnnotation(new Annotation<>(getAnnoKey(type), type));
    }

    /**
     * Getter for the Annotations of the parsed type. This Method considers
     * {@link AnnotationKey} on the parsed type if present. If not the
     * full qualified name of the parsed {@link Class} will be used as key.
     * <p>
     * Use this method only for annotation types that do use a unique key. For
     * types that are used with multiple keys separate {@link Annotation}s need
     * to be defined and {@link #getAnnotations(Annotation)} MUST BE used to
     * retrieve those.
     *
     * @param type the type of the returned annotation.
     * @return the first annotation of the parsed type.
     */
    public <V> List<V> getAnnotations(Class<V> type) {
        return getAnnotations(new Annotation<>(getAnnoKey(type), type));
    }

    /**
     * Removes all Annotations of the parsed type. This Method will only
     * remove the key mapped to the parsed type but not annotations using
     * other keys with this type. The mapped key is the key registered
     * with the {@link AnnotationKey} annotation. If no such annotation
     * is present for the parsed type the full qualified {@link Class#getName() name}
     * is used instead.
     *
     * @param type the type of the annotation to be removed.
     */
    public <V> void removeAnnotations(Class<V> type) {
        setAnnotations(new Annotation<>(getAnnoKey(type), type), null);
    }


    private String getAnnoKey(Object value) {
        return getAnnoKey(value.getClass());
    }

    private String getAnnoKey(Class<?> annoClass) {
        AnnotationKey annoValue = annoClass.getAnnotation(AnnotationKey.class);
        return annoValue != null ? annoValue.value() : annoClass.getName();
    }

}
