/*
 * The MIT License
 *
 *   Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package org.jeasy.props;

import org.jeasy.props.annotations.*;
import org.jeasy.props.api.AnnotationProcessor;
import org.jeasy.props.api.PropertyInjectionException;
import org.jeasy.props.converters.TypeConverter;
import org.jeasy.props.processors.*;
import org.apache.commons.beanutils.ConvertUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * Central component responsible for injecting a declared property in the corresponding field.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
@SuppressWarnings("unchecked,rawtypes")
class PropertyInjector {

    private final Map<Class<? extends Annotation>, AnnotationProcessor> annotationProcessors;
    private final Map<Class<?>, TypeConverter<?, ?>> typeConverters;

    PropertyInjector() {
        annotationProcessors = new HashMap<>();
        typeConverters = new HashMap<>();
        // TODO the day we decide to remove the dependency to apache commons-beanutils, register built-in converters here

        //register built-in annotation processors
        annotationProcessors.put(SystemProperty.class, new SystemPropertyAnnotationProcessor());
        annotationProcessors.put(Property.class, new PropertyAnnotationProcessor());
        annotationProcessors.put(I18NProperty.class, new I18NPropertyAnnotationProcessor());
        annotationProcessors.put(Properties.class, new PropertiesAnnotationProcessor());
        annotationProcessors.put(DBProperty.class, new DBPropertyAnnotationProcessor());
        annotationProcessors.put(JNDIProperty.class, new JNDIPropertyAnnotationProcessor());
        annotationProcessors.put(MavenProperty.class, new MavenPropertyAnnotationProcessor());
        annotationProcessors.put(ManifestProperty.class, new ManifestPropertyAnnotationProcessor());
        annotationProcessors.put(EnvironmentVariable.class, new EnvironmentVariableAnnotationProcessor());
    }

    void injectProperty(final Field field, final Object object) throws PropertyInjectionException {
        //Introspect the field for each registered annotation, and delegate its processing to the corresponding annotation processor
        for (Class<? extends Annotation> annotationType : annotationProcessors.keySet()) {
            AnnotationProcessor annotationProcessor = annotationProcessors.get(annotationType);
            if (field.isAnnotationPresent(annotationType) && annotationProcessor != null) {
                Annotation annotation = field.getAnnotation(annotationType);
                doInjectProperty(field, object, annotation, annotationProcessor);
            }
        }
    }

    private <A extends Annotation> void doInjectProperty(Field field, Object object, A annotation, AnnotationProcessor<A> annotationProcessor) throws PropertyInjectionException {
        try {
            Object value = annotationProcessor.processAnnotation(annotation, field);
            if (value != null) {
                Object typedValue = convert(value, field.getType());
                setProperty(typedValue, field, object);
            }
        } catch (Exception e) {
            throw new PropertyInjectionException(format("Unable to inject value from annotation '%s' in field '%s' of object '%s'",
                    annotation, field.getName(), object), e);
        }
    }

    private Object convert(Object value, Class<?> type) {
        TypeConverter converter = typeConverters.get(type);
        if (converter != null) {
            return converter.convert(value);
        }
        return ConvertUtils.convert(value, type);
    }

    private void setProperty(Object value, Field field, Object targetObject) throws Exception {
        boolean access = field.isAccessible();
        field.setAccessible(true);
        field.set(targetObject, value);
        field.setAccessible(access);
    }

    void addAnnotationProcessor(final Class<? extends Annotation> annotation, final AnnotationProcessor annotationProcessor) {
        annotationProcessors.put(annotation, annotationProcessor);
    }

    void addTypeConverter(final Class<?> type, final TypeConverter typeConverter) {
        typeConverters.put(type, typeConverter);
    }

}
