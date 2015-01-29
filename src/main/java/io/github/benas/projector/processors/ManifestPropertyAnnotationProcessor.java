/*
 *
 *   The MIT License
 *
 *    Copyright (c) 2014, Mahmoud Ben Hassine (md.benhassine@gmail.com)
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

package io.github.benas.projector.processors;

import io.github.benas.projector.annotations.ManifestProperty;
import io.github.benas.projector.api.AnnotationProcessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * An annotation processor that loads a header value from META-INF/MANIFEST.MF file.
 *
 * @author Mahmoud Ben Hassine (md.benhassine@gmail.com)
 */
public class ManifestPropertyAnnotationProcessor extends AbstractAnnotationProcessor implements AnnotationProcessor<ManifestProperty> {

    /**
     * Manifest file location.
     */
    private static final String MANIFEST = "META-INF/MANIFEST.MF";

    /**
     * A map of jar / manifest entries pairs.
     */
    private Map<String, Manifest> manifestEntries = new HashMap<String, Manifest>();

    @Override
    public void processAnnotation(final ManifestProperty manifestPropertyAnnotation, final Field field, Object object) throws Exception {

        String jar = manifestPropertyAnnotation.jar().trim();
        String header = manifestPropertyAnnotation.header().trim();

        if (header.isEmpty()) {
            throw new Exception(missingAttributeValue("header", "@ManifestProperty", field, object));
        }

        //process default jar attribute value, look for manifest in the target object jar
        if (jar.length() == 0) {
            try {
                InputStream inputStream = object.getClass().getClassLoader().getResourceAsStream(MANIFEST);
                if (inputStream != null) {
                    Manifest manifest = new Manifest(inputStream);
                    manifestEntries.put(jar, manifest);
                } else {
                    throw new Exception(missingSourceFile(MANIFEST, field, object));
                }
            } catch (IOException ex) {
                throw new Exception(missingSourceFile(MANIFEST, field, object), ex);
            }
        }

        if (manifestEntries.get(jar) == null) {
            try {
                JarInputStream jarStream;
                final String classPath = System.getProperty("java.class.path");
                final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
                for (final String element : classPathElements) {
                    if (element.endsWith(jar)) {
                        jarStream = new JarInputStream(new FileInputStream(element));
                        manifestEntries.put(jar, jarStream.getManifest());
                        break;
                    }
                }
            } catch (IOException ex) {
                throw new Exception(missingSourceFile(jar, field, object), ex);
            }
        }

        //the jar was not found in the classpath
        if (manifestEntries.get(jar) == null) {
            throw new Exception(missingSourceFile(jar, field, object));
        }

        injectProperty(object, field, header, manifestEntries.get(jar).getMainAttributes().getValue(header));

    }

}
