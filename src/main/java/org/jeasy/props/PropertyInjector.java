/**
 * The MIT License
 *
 *   Copyright (c) 2017, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
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
import org.jeasy.props.processors.*;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;

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
class PropertyInjector {

    private Map<Class<? extends Annotation>, AnnotationProcessor> annotationProcessors;

    public PropertyInjector() {
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
                Object typedValue = ConvertUtils.convert(value, field.getType());
                PropertyUtils.setProperty(object, field.getName(), typedValue);
            }
        } catch (Exception e) {
            throw new PropertyInjectionException(format("Unable to inject value from annotation '%s' in field '%s' of object '%s'",
                    annotation, field.getName(), object), e);
        }
    }

    void addAnnotationProcessor(final Class<? extends Annotation> annotation, final AnnotationProcessor annotationProcessor) {
        annotationProcessors.put(annotation, annotationProcessor);
    }

}
