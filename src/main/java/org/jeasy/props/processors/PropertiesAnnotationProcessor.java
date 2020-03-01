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

import org.jeasy.props.annotations.Properties;
import org.jeasy.props.api.AnnotationProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * An annotation processor that loads all properties from a properties file.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class PropertiesAnnotationProcessor extends AbstractAnnotationProcessor<Properties> {

    /**
     * A map holding source file name and Properties object serving as a cache.
     */
    private Map<String, java.util.Properties> propertiesMap = new HashMap<>();

    @Override
    public Object processAnnotation(final Properties propertiesAnnotation, final Field field) throws AnnotationProcessingException {

        rejectIfFieldIsNotOfType(field, java.util.Properties.class);

        String source = propertiesAnnotation.value().trim();
        rejectIfEmpty(source, missingAttributeValue("source", Properties.class.getName(), field));

        //check if the source file is not already loaded
        if (!propertiesMap.containsKey(source)) {
            loadProperties(source);
        }

        return propertiesMap.get(source);

    }

    private void loadProperties(final String source) throws AnnotationProcessingException {
        java.util.Properties properties = new java.util.Properties();
        try {
            InputStream inputStream = getResourceAsStream(source);
            if (inputStream != null) {
                properties.load(inputStream);
                propertiesMap.put(source, properties);
            } else {
                throw new AnnotationProcessingException(format("Unable to load properties from source %s", source));
            }
        } catch (IOException e) {
            throw new AnnotationProcessingException(format("Unable to load properties from source %s", source), e);
        }
    }

    private void rejectIfFieldIsNotOfType(final Field field, final Class type) throws AnnotationProcessingException {
        if (!field.getType().equals(type)) {
            throw new AnnotationProcessingException(format("Annotation %s declared on field '%s' of type '%s' is incompatible with type '%s'",
                    Properties.class.getName(), field.getName(), field.getDeclaringClass().getName(), field.getType()));
        }
    }

}
