/*
 *   The MIT License
 *
 *    Copyright (c) 2016, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy
 *    of this software and associated documentation files (the "Software"), to deal
 *    in the Software without restriction, including without limitation the rights
 *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *    copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in
 *    all copies or substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *    THE SOFTWARE.
 */

package io.github.benas.easyproperties.impl;

import io.github.benas.easyproperties.annotations.*;
import io.github.benas.easyproperties.annotations.Properties;
import io.github.benas.easyproperties.api.AnnotationProcessingException;
import io.github.benas.easyproperties.api.AnnotationProcessor;
import io.github.benas.easyproperties.api.PropertiesInjector;
import io.github.benas.easyproperties.api.PropertyInjectionException;
import io.github.benas.easyproperties.processors.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

import static java.lang.String.format;

/**
 * The core implementation of the {@link io.github.benas.easyproperties.api.PropertiesInjector} interface.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
final class PropertiesInjectorImpl implements PropertiesInjector {

    /**
     * A map holding registered annotations with their processors.
     */
    private Map<Class<? extends Annotation>, AnnotationProcessor> annotationProcessors;

    PropertiesInjectorImpl() {
        annotationProcessors = new HashMap<>();
        //register built-in annotation processors
        annotationProcessors.put(SystemProperty.class, new SystemPropertyAnnotationProcessor());
        annotationProcessors.put(Property.class, new PropertyAnnotationProcessor());
        annotationProcessors.put(I18NProperty.class, new I18NPropertyAnnotationProcessor());
        annotationProcessors.put(Properties.class, new PropertiesAnnotationProcessor());
        annotationProcessors.put(DBProperty.class, new DBPropertyAnnotationProcessor());
        annotationProcessors.put(JNDIProperty.class, new JNDIPropertyAnnotationProcessor());
        annotationProcessors.put(MavenProperty.class, new MavenPropertyAnnotationProcessor());
        annotationProcessors.put(ManifestProperty.class, new ManifestPropertyAnnotationProcessor());
    }

    @Override
    public void injectProperties(final Object object) throws PropertyInjectionException {

        /*
         * Retrieve declared fields
         */
        List<Field> fields = getDeclaredFields(object);

        /*
         * Retrieve inherited fields for all type hierarchy
         */
        fields.addAll(getInheritedFields(object));

        /*
         * Introspect fields for each registered annotation, and delegate its processing to the right annotation processor
         */
        for (Field field : fields) {
            for (Class<? extends Annotation> annotationType : annotationProcessors.keySet()) {
                AnnotationProcessor annotationProcessor = annotationProcessors.get(annotationType);
                if (field.isAnnotationPresent(annotationType) && annotationProcessor != null) {
                    Annotation annotation = field.getAnnotation(annotationType);
                    try {
                        annotationProcessor.processAnnotation(annotation, field, object);
                    } catch (AnnotationProcessingException e) {
                        throw new PropertyInjectionException(format("Unable to inject property %s in field %s of object %s",
                                annotation, field.getName(), object), e);
                    }
                }
            }
        }

    }

    /**
     * Register a custom annotation processor for a given annotation.
     *
     * @param annotation          the annotation type to be processed
     * @param annotationProcessor the annotation processor to register
     */
    public void registerAnnotationProcessor(final Class<? extends Annotation> annotation, final AnnotationProcessor annotationProcessor) {
        annotationProcessors.put(annotation, annotationProcessor);
    }

    private List<Field> getDeclaredFields(final Object object) {
        return new ArrayList<>(Arrays.asList(object.getClass().getDeclaredFields()));
    }

    private List<Field> getInheritedFields(final Object object) {
        List<Field> inheritedFields = new ArrayList<>();
        Class clazz = object.getClass();
        while (clazz.getSuperclass() != null) {
            Class superclass = clazz.getSuperclass();
            inheritedFields.addAll(Arrays.asList(superclass.getDeclaredFields()));
            clazz = superclass;
        }
        return inheritedFields;
    }

}
