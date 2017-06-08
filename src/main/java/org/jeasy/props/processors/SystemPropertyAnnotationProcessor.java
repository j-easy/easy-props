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
package org.jeasy.props.processors;

import org.jeasy.props.annotations.SystemProperty;
import org.jeasy.props.api.AnnotationProcessingException;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An annotation processor that loads properties from system properties.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class SystemPropertyAnnotationProcessor extends AbstractAnnotationProcessor<SystemProperty> {

    private static final Logger LOGGER = Logger.getLogger(SystemPropertyAnnotationProcessor.class.getName());

    @Override
    public Object processAnnotation(final SystemProperty systemProperty, final Field field) throws AnnotationProcessingException {

        String key = systemProperty.value().trim();

        //check attribute
        rejectIfEmpty(key, missingAttributeValue("value", SystemProperty.class.getName(), field));

        //check system property
        String value = System.getProperty(key);
        if (value == null) {
            LOGGER.log(Level.WARNING, "System property ''{0}'' on field ''{1}'' of type ''{2}'' not found in system properties: {3}",
                    new Object[]{key, field.getName(), field.getDeclaringClass().getName(), System.getProperties()});

            //Use default value if specified
            String defaultValue = systemProperty.defaultValue();
            if (defaultValue != null && !defaultValue.isEmpty()) {
                value = defaultValue.trim();
            } else {
                LOGGER.log(Level.WARNING, "Default value of system property ''{0}'' on field ''{1}'' of type ''{2}'' is empty",
                        new Object[]{key, field.getName(), field.getDeclaringClass().getName()});
                return null;
            }
        }

        return value;
    }
}
