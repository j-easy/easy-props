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

    /**
     * A map holding pom.properties file with corresponding Properties object serving as a cache.
     */
    private Map<String, Properties> mavenMap = new HashMap<>();

    @Override
    public Object processAnnotation(final MavenProperty mavenAnnotation, final Field field) throws AnnotationProcessingException {

        String key = mavenAnnotation.key().trim();
        String source = mavenAnnotation.source().trim();
        String groupId = mavenAnnotation.groupId().trim();
        String artifactId = mavenAnnotation.artifactId().trim();

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

        return mavenMap.get(pomFile).getProperty(key);

    }

    private void loadMavenProperties(final String pomFile) throws AnnotationProcessingException {
        java.util.Properties properties = new java.util.Properties();
        try {
            InputStream inputStream = getResourceAsStream(pomFile);
            if (inputStream != null) {
                properties.load(inputStream);
                mavenMap.put(pomFile, properties);
            } else {
                throw new AnnotationProcessingException(format("Unable to load pom file from %s", pomFile));
            }
        } catch (IOException e) {
            throw new AnnotationProcessingException(format("Unable to load pom file from %s", pomFile), e);
        }
    }

}
