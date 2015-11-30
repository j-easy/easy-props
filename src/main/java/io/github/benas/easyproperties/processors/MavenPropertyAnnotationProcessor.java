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

import io.github.benas.easyproperties.annotations.MavenProperty;
import io.github.benas.easyproperties.api.AnnotationProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * An annotation processor that loads all properties from a maven pom.
 *
 * @author lhottois (natlantisprog@gmail.com)
 */
public class MavenPropertyAnnotationProcessor extends AbstractAnnotationProcessor<MavenProperty> {

    /**
     * A map holding pom and Properties object serving as a cache.
     */
    private Map<String, String> mavenMap = new HashMap<>();

    @Override
    public void processAnnotation(final MavenProperty mavenAnnotation, final Field field, final Object object) throws AnnotationProcessingException {

        String key = mavenAnnotation.key().trim();
        String source = mavenAnnotation.source().trim();
        String groupId = mavenAnnotation.groupId().trim();
        String artifactId = mavenAnnotation.artifactId().trim();

        //check attributes
        checkIfEmpty(key, missingAttributeValue("key", "@MavenProperty", field, object));
        checkIfEmpty(groupId, missingAttributeValue("groupId", "@MavenProperty", field, object));
        checkIfEmpty(artifactId, missingAttributeValue("artifactId", "@MavenProperty", field, object));

        //check if the maven property is not already loaded
        String property = groupId + "." + artifactId + "." + key;
        if (!mavenMap.containsKey(property)) {
            java.util.Properties properties = new java.util.Properties();
            String pathToMavenPom = "META-INF/maven/" + groupId + "/" + artifactId + "/" + source;
            try {
                InputStream inputStream = getResourceAsStream(pathToMavenPom);
                if (inputStream != null) {
                    properties.load(inputStream);
                    mavenMap.put(property, properties.getProperty(key));
                }
            } catch (IOException e) {
                throw new AnnotationProcessingException(format("Unable to load pom file from %s", pathToMavenPom), e);
            }
        }

        processAnnotation(object, field, key, mavenMap.get(property));

    }

}
