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

import net.benas.adp4j.annotations.Properties;
import net.benas.adp4j.api.AnnotationProcessor;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An annotation processor that loads all properties from a properties file.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class PropertiesAnnotationProcessor implements AnnotationProcessor<Properties> {

    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * A map holding source file name and Properties object serving as a cache.
     */
    private Map<String, java.util.Properties> propertiesMap = new HashMap<String, java.util.Properties>();

    @Override
    public void processAnnotation(Properties propertiesAnnotation, Field field, Object object) {

        if (!field.getType().equals(java.util.Properties.class)) {
            logger.log(Level.WARNING, "@Properties declared on field " + field.getName() + " of type " +
                    object.getClass() + " is incompatible with type " + field.getType() );
            return;
        }

        String source = propertiesAnnotation.value();

        if (source != null && !source.isEmpty()) {

            //check if the source file is not already loaded
            if (!propertiesMap.containsKey(source)) {
                java.util.Properties properties = new java.util.Properties();
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

            if (propertiesMap.get(source) != null) {
                java.util.Properties properties = propertiesMap.get(source);
                try {
                    PropertyUtils.setProperty(object, field.getName(), properties);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Unable to set Properties on field "
                            + field.getName() + " of type " + object.getClass(), e);
                }
            } else {
                logger.log(Level.WARNING, "No properties loaded from source " + source);
            }
        } else {
            logger.log(Level.WARNING, "No value specified for @Properties on field " +
                    field.getName() + " of type " + object.getClass().getName());
        }

    }

    private String warnMissingSourceFile(Field field, Object object, String source) {
        return MessageFormat.format("Unable to load properties from source {0} for field {1} of type {2}",
                source, field.getName(), object.getClass().getName());
    }

}
