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
package org.jeasy.props;

import org.jeasy.props.api.AnnotationProcessor;
import org.jeasy.props.api.PropertiesInjector;
import org.jeasy.props.api.TypeConverter;

import java.lang.annotation.Annotation;

/**
 * A builder to create {@link PropertiesInjector} instances.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class PropertiesInjectorBuilder {

    /**
     * The properties injector to build.
     */
    private PropertiesInjectorImpl propertiesInjector;

    /**
     * Public constructor.
     */
    public PropertiesInjectorBuilder() {
        propertiesInjector = new PropertiesInjectorImpl();
    }

    /**
     * Create a new {@link PropertiesInjectorBuilder}.
     *
     * @return a new {@link PropertiesInjectorBuilder}
     */
    public static PropertiesInjectorBuilder aNewPropertiesInjectorBuilder() {
        return new PropertiesInjectorBuilder();
    }

    /**
     * Create a new {@link PropertiesInjector} with default parameters.
     *
     * @return a new {@link PropertiesInjector}
     */
    public static PropertiesInjector aNewPropertiesInjector() {
        return new PropertiesInjectorBuilder().build();
    }

    /**
     * Register a custom annotation processor for a given annotation.
     *
     * @param annotation          the annotation type to be processed
     * @param annotationProcessor the annotation processor to register
     * @return this instance of @{link PropertiesInjectorBuilder}
     */
    public PropertiesInjectorBuilder registerAnnotationProcessor(final Class<? extends Annotation> annotation, final AnnotationProcessor annotationProcessor) {
        propertiesInjector.registerAnnotationProcessor(annotation, annotationProcessor);
        return this;
    }

    /**
     * Register a custom type converter.
     * 
     * @param targetType the target field type
     * @param converter  to use
     * @return this instance of @{link PropertiesInjectorBuilder}
     */
    public PropertiesInjectorBuilder registerTypeConverter(Class<?> targetType, TypeConverter converter) {
        propertiesInjector.registerTypeConverter(targetType, converter);
        return this;
    }

    /**
     * Build a {@link PropertiesInjector} instance.
     *
     * @return a {@link PropertiesInjector} instance
     */
    public PropertiesInjector build() {
        return propertiesInjector;
    }

}
