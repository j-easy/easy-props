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

import io.github.benas.easyproperties.annotations.I18NProperty;
import io.github.benas.easyproperties.api.AnnotationProcessingException;
import io.github.benas.easyproperties.api.AnnotationProcessor;

import java.lang.reflect.Field;
import java.util.*;

/**
 * An annotation processor that loads properties from I18N resource bundles.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class I18NPropertyAnnotationProcessor extends AbstractAnnotationProcessor implements AnnotationProcessor<I18NProperty> {

    /**
     * A map holding bundle file name and resource bundle object serving as a cache.
     */
    private Map<String, ResourceBundle> resourceBundlesMap = new HashMap<>();

    @Override
    public void processAnnotation(final I18NProperty property, final Field field, final Object object) throws AnnotationProcessingException {

        String key = property.key().trim();
        String bundle = property.bundle().trim();
        String language = property.language().trim();
        String country = property.country().trim();
        String variant = property.variant().trim();

        //check bundle attribute value
        if (bundle.isEmpty()) {
            throw new AnnotationProcessingException(missingAttributeValue("bundle", "I18NProperty", field, object));
        }

        //check key attribute value
        if (key.isEmpty()) {
            throw new AnnotationProcessingException(missingAttributeValue("key", "I18NProperty", field, object));
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
            try {
                ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, locale);
                resourceBundlesMap.put(bundle, resourceBundle);
            } catch (MissingResourceException e) {
                throw new AnnotationProcessingException("Resource bundle " + bundle + " not found", e);
            }
        }

        //get key value, convert it to the right type and set it to the field
        String value = resourceBundlesMap.get(bundle).getString(key);
        if (value != null && !value.isEmpty()) {
            processAnnotation(object, field, key, value);
        } else {
            throw new AnnotationProcessingException("Key " + key + " not found or empty in resource bundle " + bundle);
        }

    }

}
