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

package net.benas.adp4j.processors;

import net.benas.adp4j.annotations.Property;
import net.benas.adp4j.api.AnnotationProcessor;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An annotation processor that loads properties from properties files.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class PropertyAnnotationProcessor implements AnnotationProcessor<Property> {

    private Map<String, Properties> propertiesMap = new HashMap<String, Properties>();

    @Override
    public void processAnnotation(Property property, Field field, Object object) throws Exception {

        String source = property.source().trim();
        String key = property.key().trim();

        //check source attribute value
        if (source.isEmpty()) {
            throw new Exception(missingAttributeValue("source", field, object));
        }

        //check key attribute value
        if (key.isEmpty()) {
            throw new Exception(missingAttributeValue("key", field, object));
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
            Object typedValue = ConvertUtils.convert(value, field.getType());
            try {
                PropertyUtils.setProperty(object, field.getName(), typedValue);
            } catch (Exception e) {
                throw new Exception("Unable to set property " + key + " on field " + field.getName() + " of type " +
                        object.getClass() + ". A setter may be missing for this field.", e);
            }
        } else {
            throw new Exception("Key " + key + " not found or empty in source " + source);
        }

    }

    private String missingAttributeValue(String attribute, Field field, Object object) {
        return MessageFormat.format("No value specified for attribute {0} of @Property annotation on field {1} of type {2}",
                attribute, field.getName(), object.getClass().getName());
    }

    private String missingSourceFile(String source, Field field, Object object) {
        return MessageFormat.format("Unable to load properties from source {0} for field {1} of type {2}",
                source, field.getName(), object.getClass().getName());
    }
}
