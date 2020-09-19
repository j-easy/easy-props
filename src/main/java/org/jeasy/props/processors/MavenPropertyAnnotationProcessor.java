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

import org.jeasy.props.annotations.MavenProperty;
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
 * An annotation processor that loads all properties from {@code META-INF/maven/groupId/artifactId/pom.properties} .
 *
 * @author lhottois (natlantisprog@gmail.com)
 */
public class MavenPropertyAnnotationProcessor extends AbstractAnnotationProcessor<MavenProperty> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenPropertyAnnotationProcessor.class);

    /**
     * A map holding pom.properties file with corresponding Properties object serving as a cache.
     */
    private final Map<String, Properties> mavenMap = new HashMap<>();

    @Override
    public Object processAnnotation(final MavenProperty mavenAnnotation, final Field field) throws AnnotationProcessingException {

        String key = mavenAnnotation.key().trim();
        String source = mavenAnnotation.source().trim();
        String groupId = mavenAnnotation.groupId().trim();
        String artifactId = mavenAnnotation.artifactId().trim();
        String defaultValue = mavenAnnotation.defaultValue().trim();
        boolean failFast = mavenAnnotation.failFast();

        //check attributes
        String annotationName = MavenProperty.class.getName();
        rejectIfEmpty(key, missingAttributeValue("key", annotationName, field));
        rejectIfEmpty(groupId, missingAttributeValue("groupId", annotationName, field));
        rejectIfEmpty(artifactId, missingAttributeValue("artifactId", annotationName, field));

        //check if the maven properties for the given coordinates are not already loaded
        String pomFile = "META-INF/maven/" + groupId + "/" + artifactId + "/" + source;
        if (!mavenMap.containsKey(pomFile)) {
            loadMavenProperties(pomFile);
        }

        String value = mavenMap.get(pomFile).getProperty(key);
        if (value == null) {
            String message = String.format("Maven property '%s' on field '%s' of type '%s' in class '%s' not found in pom file '%s'",
                    key, field.getName(), field.getType().getName(), field.getDeclaringClass().getName(), pomFile);
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
            LOGGER.warn("Maven property '{}' is empty in pom file '{}'", key, pomFile);
            return null;
        }
        return value;

    }

    private void loadMavenProperties(final String pomFile) throws AnnotationProcessingException {
        java.util.Properties properties = new java.util.Properties();
        try {
            InputStream inputStream = getResourceAsStream(pomFile);
            if (inputStream != null) {
                properties.load(inputStream);
                mavenMap.put(pomFile, properties);
            } else {
                throw new AnnotationProcessingException(format("Unable to load pom file from '%s'", pomFile));
            }
        } catch (IOException e) {
            throw new AnnotationProcessingException(format("Unable to load pom file from '%s'", pomFile), e);
        }
    }

}
