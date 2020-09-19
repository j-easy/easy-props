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

import org.jeasy.props.annotations.ManifestProperty;
import org.jeasy.props.api.AnnotationProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import static java.lang.String.format;

/**
 * An annotation processor that loads a header value from {@code META-INF/MANIFEST.MF} file.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class ManifestPropertyAnnotationProcessor extends AbstractAnnotationProcessor<ManifestProperty> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestPropertyAnnotationProcessor.class);
    private static final String CLASSPATH = System.getProperty("java.class.path");
    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    /**
     * A map of jar / manifest entries pairs.
     */
    private final Map<String, Manifest> manifestEntries = new HashMap<>();

    @Override
    public Object processAnnotation(final ManifestProperty manifestPropertyAnnotation, final Field field) throws AnnotationProcessingException {

        String jar = manifestPropertyAnnotation.jar().trim();
        String header = manifestPropertyAnnotation.header().trim();
        String defaultValue = manifestPropertyAnnotation.defaultValue().trim();
        boolean failFast = manifestPropertyAnnotation.failFast();

        //check attributes
        String annotationName = ManifestProperty.class.getName();
        rejectIfEmpty(jar, missingAttributeValue("jar", annotationName, field));
        rejectIfEmpty(header, missingAttributeValue("header", annotationName, field));

        if (manifestEntries.get(jar) == null) {
            loadManifestFromJar(jar);
        }

        //the jar was not found in the classpath
        rejectIfNotFound(jar);

        String value = manifestEntries.get(jar).getMainAttributes().getValue(header);
        if (value == null) {
            String message = String.format("Header '%s' not found in manifest of jar '%s'", header, jar);
            LOGGER.warn(message);
            if (failFast) {
                throw new AnnotationProcessingException(message);
            }
            if (!defaultValue.isEmpty()) {
                value = defaultValue;
            } else {
                return null;
            }
        }
        if (value.isEmpty()) {
            LOGGER.warn("Header '{}' in manifest of jar '{}' is empty", header, jar);
            return null;
        }

        return value;

    }

    private void rejectIfNotFound(String jar) throws AnnotationProcessingException {
        if (manifestEntries.get(jar) == null) {
            throw new AnnotationProcessingException(format("Unable to find jar '%s' in classpath '%s'", jar, CLASSPATH));
        }
    }

    private void loadManifestFromJar(final String jar) throws AnnotationProcessingException {
        JarInputStream jarStream = null;
        try {
            final String[] classPathElements = CLASSPATH.split(PATH_SEPARATOR);
            for (final String element : classPathElements) {
                if (element.endsWith(jar)) {
                    jarStream = new JarInputStream(new FileInputStream(element));
                    manifestEntries.put(jar, jarStream.getManifest());
                    break;
                }
            }
        } catch (IOException e) {
            throw new AnnotationProcessingException(format("Unable to load manifest file from jar '%s'", jar), e);
        } finally {
            closeJarStream(jarStream);
        }
    }

    private void closeJarStream(final JarInputStream jarStream) {
        try {
            if (jarStream != null) {
                jarStream.close();
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to close jar stream", e);
        }
    }

}
