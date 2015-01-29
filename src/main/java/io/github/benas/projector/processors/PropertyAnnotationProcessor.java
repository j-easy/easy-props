/*
 *   The MIT License
 *
 *    Copyright (c) 2014, Mahmoud Ben Hassine (md.benhassine@gmail.com)
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

package io.github.benas.projector.processors;

import io.github.benas.projector.annotations.Property;
import io.github.benas.projector.api.AnnotationProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An annotation processor that loads properties from properties files.
 *
 * @author Mahmoud Ben Hassine (md.benhassine@gmail.com)
 */
public class PropertyAnnotationProcessor extends AbstractAnnotationProcessor implements AnnotationProcessor<Property> {

    /**
     * A map holding source file name and Properties object serving as a cache.
     */
    private Map<String, Properties> propertiesMap = new HashMap<String, Properties>();

    @Override
    public void processAnnotation(final Property property, final Field field, Object object) throws Exception {

        String source = property.source().trim();
        String key = property.key().trim();

        //check source attribute value
        if (source.isEmpty()) {
            throw new Exception(missingAttributeValue("source", "@Property", field, object));
        }

        //check key attribute value
        if (key.isEmpty()) {
            throw new Exception(missingAttributeValue("key", "@Property", field, object));
        }

        //check if the source file is not already loaded
        if (!propertiesMap.containsKey(source)) {
            Properties properties = new Properties();
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

        //convert key value to the right type and set it to the field
        String value = propertiesMap.get(source).getProperty(key);
        if (value != null && !value.isEmpty()) {
            injectProperty(object, field, key, value);
        } else {
            throw new Exception("Key " + key + " not found or empty in source " + source);
        }

    }

}
