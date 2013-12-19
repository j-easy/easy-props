/*
 *   The MIT License
 *
 *    Copyright (c) 2013, benas (md.benhassine@gmail.com)
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

package io.github.benas.adp4j.processors;

import io.github.benas.adp4j.annotations.Properties;
import io.github.benas.adp4j.api.AnnotationProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * An annotation processor that loads all properties from a properties file.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class PropertiesAnnotationProcessor extends AbstractAnnotationProcessor implements AnnotationProcessor<Properties> {

    /**
     * A map holding source file name and Properties object serving as a cache.
     */
    private Map<String, java.util.Properties> propertiesMap = new HashMap<String, java.util.Properties>();

    @Override
    public void processAnnotation(final Properties propertiesAnnotation, final Field field, Object object) throws Exception {

        if (!field.getType().equals(java.util.Properties.class)) {
            throw new Exception("@Properties declared on field " + field.getName() + " of type " +
                    object.getClass() + " is incompatible with type " + field.getType());
        }

        String source = propertiesAnnotation.value().trim();

        if (source.isEmpty()) {
            throw new Exception(missingAttributeValue("source", "@Properties", field, object));
        }


        //check if the source file is not already loaded
        if (!propertiesMap.containsKey(source)) {
            java.util.Properties properties = new java.util.Properties();
            try {
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(source);
                if (inputStream != null) {
                    properties.load(inputStream);
                    propertiesMap.put(source, properties);
                } else {
                    throw new Exception(missingSourceFile(source, field, object));
                }
            } catch (IOException ex) {
                throw new Exception(missingSourceFile(source, field, object), ex);
            }
        }

        injectProperty(object, field, source, propertiesMap.get(source));

    }

}
