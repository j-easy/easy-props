/*
 * The MIT License
 * <p>
 * Copyright (c) 2017, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jeasy.props.processors;

import org.jeasy.props.api.AnnotationProcessingException;
import org.jeasy.props.api.AnnotationProcessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static java.lang.String.format;

/**
 * Base class for {@link AnnotationProcessor} implementations providing common methods.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public abstract class AbstractAnnotationProcessor<A extends Annotation> implements AnnotationProcessor<A> {

    private static final String FILE_RESOURCE_PREFIX = "file:";
    private static final String CLASSPATH_RESOURCE_PREFIX = "classpath:";

    /**
     * Constructs an error message to signal missing annotation attribute value.
     *
     * @param attribute  the target attribute
     * @param annotation the annotation concerned
     * @param field      the target field
     * @return the formatted error message
     */
    protected String missingAttributeValue(final String attribute, final String annotation, final Field field) {
        return format("No value specified for attribute '%s' of annotation '%s' on field '%s' of type '%s'",
                attribute, annotation, field.getName(), field.getDeclaringClass().getName());
    }

    /**
     * Reject a value (by throwing a {@link AnnotationProcessingException}) if it is empty.
     *
     * @param value   the value to check
     * @param message the message of the exception
     * @throws AnnotationProcessingException thrown if the value is empty
     */
    protected void rejectIfEmpty(final String value, final String message) throws AnnotationProcessingException {
        if (value.isEmpty()) {
            throw new AnnotationProcessingException(message);
        }
    }

    /**
     * Get an {@link InputStream} from the given resource.
     *
     * @param resource the resource to look for.
     * @return the resource as {@link InputStream}
     * @throws IOException when an error occurs during resource loading
     */
    InputStream getResourceAsStream(final String resource) throws IOException {
        InputStream resourceAsStream;
        if (resource.startsWith(FILE_RESOURCE_PREFIX)) {
            resourceAsStream = new FileInputStream(extractPath(resource));
        } else if (resource.startsWith(CLASSPATH_RESOURCE_PREFIX)) {
            resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream(extractPath(resource));
        } else { // by default, it is a classpath resource
            resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
        }
        return resourceAsStream;
    }

    private String extractPath(String resource) {
        return resource.substring(resource.lastIndexOf(':') + 1);
    }

}
