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

package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.api.AnnotationProcessingException;
import io.github.benas.easyproperties.api.AnnotationProcessor;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static java.lang.String.format;

/**
 * Base class for {@link io.github.benas.easyproperties.api.AnnotationProcessor} implementations providing common methods.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public abstract class AbstractAnnotationProcessor<A extends Annotation> implements AnnotationProcessor<A> {

    /**
     * Convert the value to field type and set it in the target object.
     *
     * @param target the target object
     * @param field  the annotated field
     * @param key    the annotation property attribute
     * @param value  the value to inject
     * @throws AnnotationProcessingException thrown if an exception occurs when trying to set the field value
     */
    protected void processAnnotation(final Object target, final Field field, final String key, final Object value) throws AnnotationProcessingException {

        Object typedValue = ConvertUtils.convert(value, field.getType());
        try {
            PropertyUtils.setProperty(target, field.getName(), typedValue);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new AnnotationProcessingException(format("Unable to set property '%s' on field '%s' of type '%s'." +
                    " A setter may be missing for this field.", key, field.getName(), target.getClass()), e);
        }

    }

    /**
     * Constructs an error message to signal missing annotation attribute value.
     *
     * @param attribute  the target attribute
     * @param annotation the annotation concerned
     * @param field      the target field
     * @param object     the target object
     * @return the formatted error message
     */
    protected String missingAttributeValue(final String attribute, final String annotation, final Field field, final Object object) {
        return format("No value specified for attribute '%s' of annotation '%s' on field '%s' of type '%s'",
                attribute, annotation, field.getName(), object.getClass().getName());
    }

    protected void checkIfEmpty(final String value, final String message) throws AnnotationProcessingException {
        if (value.isEmpty()) {
            throw new AnnotationProcessingException(message);
        }
    }

    protected InputStream getResourceAsStream(final String configuration) {
        return this.getClass().getClassLoader().getResourceAsStream(configuration);
    }

}
