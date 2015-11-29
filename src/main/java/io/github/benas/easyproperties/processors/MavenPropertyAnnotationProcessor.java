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
import io.github.benas.easyproperties.api.AnnotationProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * An annotation processor that loads all properties from a maven pom.
 *
 * @author lhottois (natlantisprog@gmail.com)
 */
public class MavenPropertyAnnotationProcessor extends AbstractAnnotationProcessor implements AnnotationProcessor<MavenProperty> {

    /**
     * A map holding pom and Properties object serving as a cache.
     */
    private Map<String, String> mavenMap = new HashMap<>();

    @Override
    public void processAnnotation(final MavenProperty mavenAnnotation, final Field field, Object object) throws Exception {

        // We get the key to find into the pom
        String key = mavenAnnotation.key().trim();
        String source = mavenAnnotation.source().trim();
        String groupId = mavenAnnotation.groupId().trim();
        String artifactId = mavenAnnotation.artifactId().trim();

        if (key.isEmpty()) {
            throw new Exception(missingAttributeValue("key", "@MavenProperty", field, object));
        }

        if (groupId.isEmpty()) {
            throw new Exception(missingAttributeValue("groupId", "@MavenProperty", field, object));
        }

        if (artifactId.isEmpty()) {
            throw new Exception(missingAttributeValue("artifactId", "@MavenProperty", field, object));
        }

        //check if the maven value is not already loaded
        String cacheKey = groupId + "." + artifactId + "." + key;
        if (!mavenMap.containsKey(cacheKey)) {
            java.util.Properties properties = new java.util.Properties();
            // We search directly into the META-INF generated property
            String pathToMaven = "META-INF/maven/" + groupId + "/" + artifactId + "/" + source;

            try {
                InputStream inputStream = object.getClass().getClassLoader().getResourceAsStream(pathToMaven);
                if (inputStream != null) {
                    properties.load(inputStream);
                    Object keyValue = properties.get(key);
                    String keyValueAnalyzed = String.valueOf(keyValue);
                    mavenMap.put(cacheKey, keyValueAnalyzed);
                } else {
                    throw new Exception(missingSourceFile(pathToMaven, field, object));
                }
            } catch (IOException ex) {
                throw new Exception(missingSourceFile(pathToMaven, field, object), ex);
            }
        }

        injectProperty(object, field, key, mavenMap.get(cacheKey));

    }

}
