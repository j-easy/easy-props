/*
 *
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

import io.github.benas.easyproperties.annotations.ManifestProperty;
import io.github.benas.easyproperties.api.AnnotationProcessingException;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * An annotation processor that loads a header value from META-INF/MANIFEST.MF file.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class ManifestPropertyAnnotationProcessor extends AbstractAnnotationProcessor<ManifestProperty> {

    private static final Logger LOGGER = Logger.getLogger(ManifestPropertyAnnotationProcessor.class.getName());

    private static final String CLASSPATH = System.getProperty("java.class.path");

    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    /**
     * A map of jar / manifest entries pairs.
     */
    private Map<String, Manifest> manifestEntries = new HashMap<>();

    @Override
    public Object processAnnotation(final ManifestProperty manifestPropertyAnnotation, final Field field) throws AnnotationProcessingException {

        String jar = manifestPropertyAnnotation.jar().trim();
        String header = manifestPropertyAnnotation.header().trim();

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
            LOGGER.log(Level.WARNING, "Header ''{0}'' not found in manifest of jar ''{1}''", new Object[]{header, jar});
            return null;
        }
        if (value.isEmpty()) {
            LOGGER.log(Level.WARNING, "Header ''{0}'' in manifest of jar ''{1}'' is empty", new Object[]{header, jar});
            return null;
        }

        return value;

    }

    private void rejectIfNotFound(String jar) throws AnnotationProcessingException {
        if (manifestEntries.get(jar) == null) {
            throw new AnnotationProcessingException(format("Unable to find jar '%s' in classpath: %s", jar, CLASSPATH));
        }
    }

    private void loadManifestFromJar(final String jar) throws AnnotationProcessingException {
        JarInputStream jarStream = null;
        try {
            final String classPath = CLASSPATH;
            final String[] classPathElements = classPath.split(PATH_SEPARATOR);
            for (final String element : classPathElements) {
                if (element.endsWith(jar)) {
                    jarStream = new JarInputStream(new FileInputStream(element));
                    manifestEntries.put(jar, jarStream.getManifest());
                    break;
                }
            }
        } catch (IOException e) {
            throw new AnnotationProcessingException(format("Unable to load manifest file from jar: %s", jar), e);
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
            LOGGER.log(Level.WARNING, "Unable to close jar stream", e);
        }
    }

}
