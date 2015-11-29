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

import io.github.benas.easyproperties.annotations.Property;
import io.github.benas.easyproperties.api.AnnotationProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An annotation processor that loads properties from properties files.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class PropertyAnnotationProcessor extends AbstractAnnotationProcessor implements AnnotationProcessor<Property> {

    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * A map holding source file name and Properties object serving as a cache.
     */
    private Map<String, Properties> propertiesMap = new HashMap<>();

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
        if (value == null) {
            logger.log(Level.WARNING, "Property " + key + " on field '" + field.getName() +
                    "' of type '" + object.getClass() + "' not found in properties file: "
                    + source);
            return;
        }
        if (value.isEmpty()) {
            //silently ignore empty values
            return;
        }

        injectProperty(object, field, key, value);

    }

}
