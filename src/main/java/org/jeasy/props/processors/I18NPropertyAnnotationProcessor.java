/*
 * The MIT License
 *
 *   Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package org.jeasy.props.processors;

import org.jeasy.props.annotations.I18NProperty;
import org.jeasy.props.api.AnnotationProcessingException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * An annotation processor that loads properties from I18N resource bundles.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class I18NPropertyAnnotationProcessor extends AbstractAnnotationProcessor<I18NProperty> {

    private static final Logger LOGGER = Logger.getLogger(I18NPropertyAnnotationProcessor.class.getName());

    /**
     * A map holding bundle file name and resource bundle object serving as a cache.
     */
    private final Map<String, ResourceBundle> resourceBundlesMap = new HashMap<>();

    @Override
    public Object processAnnotation(final I18NProperty property, final Field field) throws AnnotationProcessingException {

        String key = property.key().trim();
        String bundle = property.bundle().trim();
        String language = property.language().trim();
        String country = property.country().trim();
        String variant = property.variant().trim();
        String defaultValue = property.defaultValue().trim();

        //check attributes
        String annotationName = I18NProperty.class.getName();
        rejectIfEmpty(bundle, missingAttributeValue("bundle", annotationName, field));
        rejectIfEmpty(key, missingAttributeValue("key", annotationName, field));

        Locale locale = getLocale(language, country, variant);

        //check if the resource bundle is not already loaded
        if (!resourceBundlesMap.containsKey(bundle)) {
            loadResourceBundle(bundle, locale);
        }

        String value = null;
        try {
            value = resourceBundlesMap.get(bundle).getString(key);
            if (value.isEmpty()) {
                LOGGER.log(Level.WARNING, "Key ''{0}'' is empty in resource bundle ''{1}''", new Object[]{key, bundle});
                return null;
            }
        } catch (MissingResourceException e) {
            LOGGER.log(Level.WARNING, format("Key '%s' not found in resource bundle '%s'", key, bundle), e);
            if (!defaultValue.isEmpty()) {
                value = defaultValue;
            }
        }

        return value;
    }

    private Locale getLocale(String language, String country, String variant) {
        Locale locale = Locale.getDefault();
        if (!language.isEmpty()) {
            locale = new Locale(language);
        }
        if (!language.isEmpty() && !country.isEmpty()) {
            locale = new Locale(language, country);
        }
        if (!language.isEmpty() && !country.isEmpty() && !variant.isEmpty()) {
            locale = new Locale(language, country, variant);
        }
        return locale;
    }

    private void loadResourceBundle(final String bundle, final Locale locale) throws AnnotationProcessingException {
        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, locale);
            resourceBundlesMap.put(bundle, resourceBundle);
        } catch (MissingResourceException e) {
            throw new AnnotationProcessingException(format("Resource bundle '%s' not found", bundle), e);
        }
    }

}
