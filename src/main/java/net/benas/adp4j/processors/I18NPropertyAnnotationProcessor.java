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
import java.text.MessageFormat;
import java.util.*;

/**
 * An annotation processor that loads properties from I18N resource bundles.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class I18NPropertyAnnotationProcessor implements AnnotationProcessor<I18NProperty> {

    /**
     * A map holding bundle file name and resource bundle object serving as a cache.
     */
    private Map<String, ResourceBundle> resourceBundlesMap = new HashMap<String, ResourceBundle>();

    @Override
    public void processAnnotation(I18NProperty property, Field field, Object object) throws Exception {

        String key = property.key().trim();
        String bundle = property.bundle().trim();
        String language = property.language().trim();
        String country = property.country().trim();
        String variant = property.variant().trim();

        //check bundle attribute value
        if (bundle.isEmpty()) {
            throw new Exception(missingAttributeValue("bundle", field, object));
        }

        //check key attribute value
        if (key.isEmpty()) {
            throw new Exception(missingAttributeValue("key", field, object));
        }

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

        //check if the resource bundle is not already loaded
        if (!resourceBundlesMap.containsKey(bundle)) {
            try{
                ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, locale);
                resourceBundlesMap.put(bundle, resourceBundle);
            } catch (MissingResourceException e) {
                throw new Exception("Resource bundle " + bundle + " not found", e);
            }
        }

        //get key value, convert it to the right type and set it to the field
        String value = resourceBundlesMap.get(bundle).getString(key);
        if (value != null && !value.isEmpty()) {
            Object typedValue = ConvertUtils.convert(value, field.getType());
            try {
                PropertyUtils.setProperty(object, field.getName(), typedValue);
            } catch (Exception e) {
                throw new Exception("Unable to set i18n property " + key + " on field " +
                        field.getName() + " of type " + object.getClass() + ". A setter may be missing for this field.", e);
            }
        } else {
            throw new Exception("Key " + key + " not found or empty in resource bundle " + bundle);
        }

    }


    private String missingAttributeValue(String attribute, Field field, Object object) {
        return MessageFormat.format("No value specified for attribute {0} of @I18NProperty annotation on field {1} of type {2}",
                attribute, field.getName(), object.getClass().getName());
    }

}
