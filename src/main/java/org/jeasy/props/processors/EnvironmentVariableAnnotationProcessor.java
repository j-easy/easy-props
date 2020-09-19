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
package org.jeasy.props.processors;

import java.lang.reflect.Field;

import org.jeasy.props.annotations.EnvironmentVariable;
import org.jeasy.props.api.AnnotationProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An annotation processor that loads properties from environment variables.
 * 
 * @author Greg Schofield (gregs@indellient.com)
 * @author Mahmoud Ben Hassine
 */
public class EnvironmentVariableAnnotationProcessor extends AbstractAnnotationProcessor<EnvironmentVariable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentVariableAnnotationProcessor.class);

    @Override
    public Object processAnnotation(final EnvironmentVariable environmentVariable, final Field field) throws AnnotationProcessingException {

        String key = environmentVariable.value().trim();
        String defaultValue = environmentVariable.defaultValue();
        boolean failFast = environmentVariable.failFast();

        //check attribute
        rejectIfEmpty(key, missingAttributeValue("value", EnvironmentVariable.class.getName(), field));

        //check environment variable
        String value = System.getenv(key);
        if (value == null) {
            String message = String.format("Environment variable '%s' on field '%s' of type '%s' in class '%s' not found in environment variables",
                    key, field.getName(), field.getType().getName(), field.getDeclaringClass().getName());
            LOGGER.warn(message);
            if (failFast) {
                throw new AnnotationProcessingException(message);
            }
            if (!defaultValue.isEmpty()) {
                value = defaultValue.trim();
            } else {
                LOGGER.warn("Default value of environment variable '{}' on field '{}' of type '{}' in class '{}' is empty",
                        key, field.getName(), field.getType().getName(), field.getDeclaringClass().getName());
                return null;
            }
        }

        return value;
    }
}
