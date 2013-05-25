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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An annotation processor that loads properties from properties files.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class PropertyAnnotationProcessor implements AnnotationProcessor<Property> {

    private Logger logger = Logger.getLogger(getClass().getName());

    private Map<String, Properties> propertiesMap = new HashMap<String, Properties>();

    @Override
    public void processAnnotation(Property property, Field field, Object object) {

        String source = property.source();

        //check if the source file is not already loaded
        if (!propertiesMap.containsKey(source)) {
            Properties properties = new Properties();
            try {
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(source);
                if (inputStream != null) {
                    properties.load(inputStream);
                    propertiesMap.put(source, properties);
                } else {
                    logger.log(Level.WARNING, warnMissingSourceFile(field, object, source));
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, warnMissingSourceFile(field, object, source), ex);
            }
        }

        //get key, convert it to the right type and set it to the field
        String key = property.key();
        if (key != null && !key.isEmpty()) {
            if (propertiesMap.get(source) != null) {
                String value = propertiesMap.get(source).getProperty(key);
                if (value != null && !value.isEmpty()) {
                    Object typedValue = ConvertUtils.convert(value, field.getType());
                    try {
                        PropertyUtils.setProperty(object, field.getName(), typedValue);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Unable to set property " + key + " on field "
                                + field.getName() + " of type " + object.getClass(), e);
                    }
                } else {
                    logger.log(Level.WARNING, "Key " + key + " not found in source " + source);
                }
            } else {
                logger.log(Level.WARNING, "No properties loaded from source " + source);
            }
        } else {
            logger.log(Level.WARNING, "No value specified for attribute 'key' of @Property on field " +
                    field.getName() + " of type " + object.getClass().getName());
        }

    }

    private String warnMissingSourceFile(Field field, Object object, String source) {
        return MessageFormat.format("Unable to load properties from source {0} for field {1} of type {2}",
                source, field.getName(), object.getClass().getName());
    }
}
