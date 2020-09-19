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
import org.jeasy.props.api.PropertyInjectionException;
import org.jeasy.props.api.TypeConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * The core implementation of the {@link PropertiesInjector} interface.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
final class PropertiesInjectorImpl implements PropertiesInjector {

    private final PropertyInjector propertyInjector;
    private final MBeanRegistrar mBeanRegistrar;
    private final HotReloadingRegistrar hotReloadingRegistrar;

    PropertiesInjectorImpl() {
        propertyInjector = new PropertyInjector();
        mBeanRegistrar = new MBeanRegistrar();
        hotReloadingRegistrar = new HotReloadingRegistrar();
    }

    @Override
    public void injectProperties(final Object object) throws PropertyInjectionException {
        // Retrieve declared and inherited fields
        List<Field> fields = ReflectionUtils.getAllFields(object);

        // Inject properties in each field
        for (Field field : fields) {
            propertyInjector.injectProperty(field, object);
        }

        // Register a hot reloading background task and a JMX MBean if needed
        hotReloadingRegistrar.registerHotReloadingTask(this, object);
        mBeanRegistrar.registerMBeanFor(object);
    }

    void registerAnnotationProcessor(final Class<? extends Annotation> annotation, final AnnotationProcessor annotationProcessor) {
        propertyInjector.addAnnotationProcessor(annotation, annotationProcessor);
    }

    void registerTypeConverter(Class<?> type, TypeConverter typeConverter) {
        propertyInjector.addTypeConverter(type, typeConverter);
    }

}
