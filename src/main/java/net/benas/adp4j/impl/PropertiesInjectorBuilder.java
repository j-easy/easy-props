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

package net.benas.adp4j.impl;

import net.benas.adp4j.api.AnnotationProcessor;
import net.benas.adp4j.api.PropertiesInjector;

import java.lang.annotation.Annotation;

/**
 * A builder to create {@link net.benas.adp4j.api.PropertiesInjector} instances.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class PropertiesInjectorBuilder {

    /**
     * The properties injector to build.
     */
    private PropertiesInjectorImpl propertiesInjector;

    public PropertiesInjectorBuilder() {
        propertiesInjector = new PropertiesInjectorImpl();
    }

    /**
     * Register a custom annotation processor for a given annotation.
     * @param annotation the annotation type to be processed
     * @param annotationProcessor the annotation processor to register
     */
    public PropertiesInjectorBuilder registerAnnotationProcessor(Class<? extends Annotation> annotation, AnnotationProcessor annotationProcessor) {
        propertiesInjector.registerAnnotationProcessor(annotation, annotationProcessor);
        return this;
    }

    /**
     * Build a {@link PropertiesInjector} instance.
     * @return a {@link PropertiesInjector} instance
     */
    public PropertiesInjector build() {
        return propertiesInjector;
    }

}
