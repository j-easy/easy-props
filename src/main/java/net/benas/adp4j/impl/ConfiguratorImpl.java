/*
 *   The MIT License
 *
 *    Copyright (c) 2013, benas (md.benhassine@gmail.com)
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

package net.benas.adp4j.impl;

import net.benas.adp4j.annotations.I18NProperty;
import net.benas.adp4j.annotations.Property;
import net.benas.adp4j.annotations.SystemProperty;
import net.benas.adp4j.api.AnnotationProcessor;
import net.benas.adp4j.api.Configurator;
import net.benas.adp4j.processors.I18NPropertyAnnotationProcessor;
import net.benas.adp4j.processors.PropertyAnnotationProcessor;
import net.benas.adp4j.processors.SystemPropertyAnnotationProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * The core implementation of the {@link Configurator} interface.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class ConfiguratorImpl implements Configurator {

    /**
     * A map holding registered annotations with their processors.
     */
    private Map<Class<? extends Annotation>, AnnotationProcessor> annotationProcessors;

    public ConfiguratorImpl() {
        annotationProcessors = new HashMap<Class<? extends Annotation>, AnnotationProcessor>();
        annotationProcessors.put(SystemProperty.class, new SystemPropertyAnnotationProcessor());
        annotationProcessors.put(Property.class, new PropertyAnnotationProcessor());
        annotationProcessors.put(I18NProperty.class, new I18NPropertyAnnotationProcessor());
    }

    @Override
    public void configure(Object object) {

        /*
         * Retrieve declared fields
         */
        List<Field> declaredFields = Arrays.asList(object.getClass().getDeclaredFields());

        /*
         * Retrieve inherited fields for all type hierarchy
         */
        Class clazz = object.getClass();
        while (clazz.getSuperclass() != null) {
            Class superclass = clazz.getSuperclass();
            declaredFields.addAll(Arrays.asList(superclass.getDeclaredFields()));
            clazz = superclass;
        }

        /*
         * Introspect fields for each registered annotation, and delegate it processing to the right annotation processor
         */
        for (Field field : declaredFields) {
            for (Class<? extends Annotation> annotationType : annotationProcessors.keySet()) {
                AnnotationProcessor annotationProcessor = annotationProcessors.get(annotationType);
                if (field.isAnnotationPresent(annotationType) && annotationProcessor != null) {
                    Annotation annotation = field.getAnnotation(annotationType);
                    annotationProcessor.processAnnotation(annotation, field, object);
                }
            }
        }

    }

    /**
     * Register a custom annotation processor for a given annotation.
     * @param annotation the annotation type to be processed
     * @param annotationProcessor the annotation processor to register
     */
    public void registerAnnotationProcessor(Class<? extends Annotation> annotation, AnnotationProcessor annotationProcessor) {
        annotationProcessors.put(annotation, annotationProcessor);
    }

}
