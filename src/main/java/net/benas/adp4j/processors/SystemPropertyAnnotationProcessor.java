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

import net.benas.adp4j.annotations.SystemProperty;
import net.benas.adp4j.api.AnnotationProcessor;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An annotation processor that loads properties from system properties.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class SystemPropertyAnnotationProcessor implements AnnotationProcessor<SystemProperty> {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void processAnnotation(SystemProperty systemProperty, Field field, Object object) {

        String key = systemProperty.value().trim();

        if (key != null && !key.isEmpty()) {
            String value = System.getProperty(key);
            if (value != null && !value.isEmpty()) {
                Object typedValue = ConvertUtils.convert(value, field.getType());
                try {
                    PropertyUtils.setProperty(object, field.getName(), typedValue);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Unable to set system property " + key + " on field " +
                            field.getName() + " of type " + object.getClass(), e);
                }
            } else {
                logger.log(Level.WARNING, "System property " + key + " on field " + field.getName() +
                        " of type " + object.getClass() + " not found in system properties: "
                        + System.getProperties());
            }
        } else {
            logger.log(Level.WARNING, "No value specified for @SystemProperty on field " +
                    field.getName() + " of type " + object.getClass().getName());
        }

    }
}
