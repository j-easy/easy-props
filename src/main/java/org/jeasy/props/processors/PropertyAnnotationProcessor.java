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

import org.jeasy.props.annotations.Property;
import org.jeasy.props.api.AnnotationProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;

/**
 * An annotation processor that loads properties from properties files.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class PropertyAnnotationProcessor extends AbstractAnnotationProcessor<Property> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyAnnotationProcessor.class);

    /**
     * A map holding source file name and Properties object serving as a cache.
     */
    private final Map<String, Properties> propertiesMap = new HashMap<>();

    @Override
    public Object processAnnotation(final Property property, final Field field) throws AnnotationProcessingException {

        String source = property.source().trim();
        String key = property.key().trim();
        String defaultValue = property.defaultValue().trim();
        boolean failFast = property.failFast();

        //check attributes
        String annotationName = Property.class.getName();
        rejectIfEmpty(source, missingAttributeValue("source", annotationName, field));
        rejectIfEmpty(key, missingAttributeValue("key", annotationName, field));

        //check if the source file is not already loaded
        if (!propertiesMap.containsKey(source)) {
            loadProperties(source);
        }

        //convert key value to the right type and set it to the field
        String value = propertiesMap.get(source).getProperty(key);
        if (value == null) {
            String message = String.format("Property '%s' on field '%s' of type '%s' in class '%s' not found in properties file '%s'",
                    key, field.getName(), field.getType().getName(), field.getDeclaringClass().getName(), source);
            LOGGER.warn(message);
            if (failFast) {
                throw new AnnotationProcessingException(message);
            }
            if (!defaultValue.isEmpty()) {
                value = defaultValue;
            } else {
                return null;
            }
        }
        if (value.isEmpty()) {
            LOGGER.warn("Property '{}' is empty in properties file '{}'", key, source);
            return null;
        }

        return value;

    }

    private void loadProperties(final String source) throws AnnotationProcessingException {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = getResourceAsStream(source);
            if (inputStream != null) {
                properties.load(inputStream);
                propertiesMap.put(source, properties);
            } else {
                throw new AnnotationProcessingException(format("Unable to load properties from source '%s'", source));
            }
        } catch (IOException e) {
            throw new AnnotationProcessingException(format("Unable to load properties from source '%s'", source), e);
        } finally {
            closeInputStream(inputStream);
        }
    }

    private void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.warn("Unable to close input stream", e);
            }
        }
    }

}
