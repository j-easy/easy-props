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

import net.benas.adp4j.annotations.I18NProperty;
import net.benas.adp4j.api.AnnotationProcessor;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An annotation processor that loads properties from I18N resource bundles.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class I18NPropertyAnnotationProcessor implements AnnotationProcessor<I18NProperty> {

    private Logger logger = Logger.getLogger(getClass().getName());

    private Map<String, ResourceBundle> resourceBundlesMap = new HashMap<String, ResourceBundle>();

    @Override
    public void processAnnotation(I18NProperty property, Field field, Object object) {

        String key = property.key();
        String bundle = property.bundle();
        String language = property.language();
        String country = property.country();
        String variant = property.variant();

        Locale locale = Locale.getDefault();
        if (language != null && !language.isEmpty()) {
            locale = new Locale(language);
        }
        if (language != null && !language.isEmpty() && country != null && !country.isEmpty()) {
            locale = new Locale(language, country);
        }
        if (language != null && !language.isEmpty() && country != null && !country.isEmpty()
                && variant != null && !variant.isEmpty()) {
            locale = new Locale(language, country, variant);
        }

        try {

            //check if the resource bundle is not already loaded
            if (!resourceBundlesMap.containsKey(bundle)) {
                ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, locale);
                resourceBundlesMap.put(bundle, resourceBundle);
            }

            //get key, convert it to the right type and set it to the field
            if (key != null && !key.isEmpty()) {
                if (resourceBundlesMap.get(bundle) != null) {
                    String value = resourceBundlesMap.get(bundle).getString(key);
                    if (value != null && !value.isEmpty()) {
                        Object typedValue = ConvertUtils.convert(value, field.getType());
                        try {
                            PropertyUtils.setProperty(object, field.getName(), typedValue);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Unable to set i18n property " + key + " on field " +
                                    field.getName() + " of type " + object.getClass(), e);
                        }
                    } else {
                        logger.log(Level.WARNING, "Key " + key + " not found in resource bundle " + bundle);
                    }
                } else {
                    logger.log(Level.WARNING, "No properties loaded from resource bundle " + bundle);
                }
            } else {
                logger.log(Level.WARNING, "No value specified for attribute 'key' of @I18NProperty on field " +
                        field.getName() + " of type " + object.getClass().getName());
            }
        } catch (MissingResourceException e) {
            logger.log(Level.SEVERE, "Resource bundle " + bundle + " not found", e);
        }

    }

}
