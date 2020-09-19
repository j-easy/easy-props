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

import org.jeasy.props.annotations.DBProperty;
import org.jeasy.props.annotations.EnvironmentVariable;
import org.jeasy.props.annotations.I18NProperty;
import org.jeasy.props.annotations.JNDIProperty;
import org.jeasy.props.annotations.ManifestProperty;
import org.jeasy.props.annotations.MavenProperty;
import org.jeasy.props.annotations.Properties;
import org.jeasy.props.annotations.Property;
import org.jeasy.props.annotations.SystemProperty;
import org.jeasy.props.api.AnnotationProcessor;
import org.jeasy.props.api.PropertyInjectionException;
import org.jeasy.props.api.TypeConverter;
import org.jeasy.props.converters.AtomicIntegerTypeConverter;
import org.jeasy.props.converters.AtomicLongTypeConverter;
import org.jeasy.props.converters.BigDecimalTypeConverter;
import org.jeasy.props.converters.BigIntegerTypeConverter;
import org.jeasy.props.converters.BooleanTypeConverter;
import org.jeasy.props.converters.ByteTypeConverter;
import org.jeasy.props.converters.CharacterTypeConverter;
import org.jeasy.props.converters.DateTypeConverter;
import org.jeasy.props.converters.DoubleTypeConverter;
import org.jeasy.props.converters.FloatTypeConverter;
import org.jeasy.props.converters.GregorianCalendarTypeConverter;
import org.jeasy.props.converters.IntegerTypeConverter;
import org.jeasy.props.converters.LocalDateConverter;
import org.jeasy.props.converters.LocalDateTimeConverter;
import org.jeasy.props.converters.LocalTimeConverter;
import org.jeasy.props.converters.LongTypeConverter;
import org.jeasy.props.converters.ShortTypeConverter;
import org.jeasy.props.converters.SqlDateTypeConverter;
import org.jeasy.props.converters.SqlTimeTypeConverter;
import org.jeasy.props.converters.SqlTimestampTypeConverter;
import org.jeasy.props.converters.StringTypeConverter;
import org.jeasy.props.processors.DBPropertyAnnotationProcessor;
import org.jeasy.props.processors.EnvironmentVariableAnnotationProcessor;
import org.jeasy.props.processors.I18NPropertyAnnotationProcessor;
import org.jeasy.props.processors.JNDIPropertyAnnotationProcessor;
import org.jeasy.props.processors.ManifestPropertyAnnotationProcessor;
import org.jeasy.props.processors.MavenPropertyAnnotationProcessor;
import org.jeasy.props.processors.PropertiesAnnotationProcessor;
import org.jeasy.props.processors.PropertyAnnotationProcessor;
import org.jeasy.props.processors.SystemPropertyAnnotationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Central component responsible for injecting a declared property in the corresponding field.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
@SuppressWarnings("unchecked,rawtypes")
class PropertyInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyInjector.class);
    private static final String WARNING = "Unable to inject value from annotation '%s' on field '%s' of type '%s' in class '%s'";
    private static final List<Class<? extends Annotation>> builtinAnnotations = Arrays.asList(
            SystemProperty.class, Property.class, I18NProperty.class, Properties.class, DBProperty.class,
            JNDIProperty.class, MavenProperty.class, ManifestProperty.class, EnvironmentVariable.class);
    private final Map<Class<? extends Annotation>, AnnotationProcessor> annotationProcessors;
    private final Map<Class<?>, TypeConverter<?, ?>> typeConverters;

    PropertyInjector() {
        annotationProcessors = new HashMap<>();
        typeConverters = new HashMap<>();

        // register built-in type converters
        typeConverters.put(AtomicInteger.class, new AtomicIntegerTypeConverter());
        typeConverters.put(AtomicLong.class, new AtomicLongTypeConverter());
        typeConverters.put(BigDecimal.class, new BigDecimalTypeConverter());
        typeConverters.put(BigInteger.class, new BigIntegerTypeConverter());
        typeConverters.put(Boolean.class, new BooleanTypeConverter());
        typeConverters.put(Boolean.TYPE, new BooleanTypeConverter());
        typeConverters.put(Byte.class, new ByteTypeConverter());
        typeConverters.put(Byte.TYPE, new ByteTypeConverter());
        typeConverters.put(Character.class, new CharacterTypeConverter());
        typeConverters.put(Character.TYPE, new CharacterTypeConverter());
        typeConverters.put(Double.class, new DoubleTypeConverter());
        typeConverters.put(Double.TYPE, new DoubleTypeConverter());
        typeConverters.put(Float.class, new FloatTypeConverter());
        typeConverters.put(Float.TYPE, new FloatTypeConverter());
        typeConverters.put(Integer.class, new IntegerTypeConverter());
        typeConverters.put(Integer.TYPE, new IntegerTypeConverter());
        typeConverters.put(Long.class, new LongTypeConverter());
        typeConverters.put(Long.TYPE, new LongTypeConverter());
        typeConverters.put(Short.class, new ShortTypeConverter());
        typeConverters.put(Short.TYPE, new ShortTypeConverter());
        typeConverters.put(java.util.Date.class, new DateTypeConverter());
        typeConverters.put(java.util.Calendar.class, new GregorianCalendarTypeConverter());
        typeConverters.put(java.util.GregorianCalendar.class, new GregorianCalendarTypeConverter());
        typeConverters.put(java.sql.Date.class, new SqlDateTypeConverter());
        typeConverters.put(java.sql.Time.class, new SqlTimeTypeConverter());
        typeConverters.put(java.sql.Timestamp.class, new SqlTimestampTypeConverter());
        typeConverters.put(java.time.LocalDate.class, new LocalDateConverter());
        typeConverters.put(java.time.LocalTime.class, new LocalTimeConverter());
        typeConverters.put(java.time.LocalDateTime.class, new LocalDateTimeConverter());
        typeConverters.put(String.class, new StringTypeConverter());

        //register built-in annotation processors
        annotationProcessors.put(SystemProperty.class, new SystemPropertyAnnotationProcessor());
        annotationProcessors.put(Property.class, new PropertyAnnotationProcessor());
        annotationProcessors.put(I18NProperty.class, new I18NPropertyAnnotationProcessor());
        annotationProcessors.put(Properties.class, new PropertiesAnnotationProcessor());
        annotationProcessors.put(DBProperty.class, new DBPropertyAnnotationProcessor());
        annotationProcessors.put(JNDIProperty.class, new JNDIPropertyAnnotationProcessor());
        annotationProcessors.put(MavenProperty.class, new MavenPropertyAnnotationProcessor());
        annotationProcessors.put(ManifestProperty.class, new ManifestPropertyAnnotationProcessor());
        annotationProcessors.put(EnvironmentVariable.class, new EnvironmentVariableAnnotationProcessor());
    }

    void injectProperty(final Field field, final Object object) throws PropertyInjectionException {
        List<? extends Annotation> sortedAnnotations = sortAnnotationsByOrder(field);
        for (Annotation annotation : sortedAnnotations) {
            AnnotationProcessor annotationProcessor = annotationProcessors.get(annotation.annotationType());
            Object value = getValue(field, object, annotation, annotationProcessor);
            if (value != null) {
                doInjectProperty(value, field, object);
                break;
            } else {
                LOGGER.warn(String.format(WARNING, annotation, field.getName(), field.getType().getName(), object.getClass().getName()));
            }
        }
    }

    private List<? extends Annotation> sortAnnotationsByOrder(Field field) {
        class AnnotationWithOrder implements Comparable<AnnotationWithOrder> {
            final Annotation annotation;
            final int order;

            AnnotationWithOrder(Annotation annotation, int order) {
                this.annotation = annotation;
                this.order = order;
            }

            @Override
            public int compareTo(AnnotationWithOrder o) {
                return Integer.compare(this.order, o.order);
            }
        }
        return Arrays.stream(field.getDeclaredAnnotations())
                .filter(annotation -> !builtinAnnotations.contains(annotation.getClass()))
                .map(annotation -> new AnnotationWithOrder(annotation, getOrder(annotation)))
                .sorted()
                .map(annotationWithOrder -> annotationWithOrder.annotation)
                .collect(Collectors.toList());
    }

    private int getOrder(Annotation annotation) {
        // FIXME No inheritance in Java annotations.. is there a better way to do that?
        if (annotation instanceof Property) return ((Property) annotation).order();
        if (annotation instanceof Properties) return ((Properties) annotation).order();
        if (annotation instanceof SystemProperty) return ((SystemProperty) annotation).order();
        if (annotation instanceof EnvironmentVariable) return ((EnvironmentVariable) annotation).order();
        if (annotation instanceof MavenProperty) return ((MavenProperty) annotation).order();
        if (annotation instanceof I18NProperty) return ((I18NProperty) annotation).order();
        if (annotation instanceof ManifestProperty) return ((ManifestProperty) annotation).order();
        if (annotation instanceof DBProperty) return ((DBProperty) annotation).order();
        if (annotation instanceof JNDIProperty) return ((JNDIProperty) annotation).order();
        return 0;
    }

    private <A extends Annotation> Object getValue(Field field, Object object, A annotation, AnnotationProcessor<A> annotationProcessor) throws PropertyInjectionException {
        try {
            return annotationProcessor.processAnnotation(annotation, field);
        } catch (Exception e) {
            throw new PropertyInjectionException(format(WARNING, annotation, field.getName(), field.getType().getName(), object.getClass().getName()), e);
        }
    }
    
    private void doInjectProperty(Object value, Field field, Object object) throws PropertyInjectionException {
        try {
        Object typedValue = convert(value, field.getType());
        setProperty(typedValue, field, object);
        } catch (Exception e) {
            throw new PropertyInjectionException(format(WARNING, value, field.getName(), field.getType().getName(), object.getClass().getName()), e);
        }
    }

    private Object convert(Object value, Class<?> type) {
        TypeConverter converter = typeConverters.get(type);
        if (converter != null) {
            return converter.convert(value);
        }
        return value;
    }

    private void setProperty(Object value, Field field, Object targetObject) throws Exception {
        boolean access = field.isAccessible();
        field.setAccessible(true);
        field.set(targetObject, value);
        field.setAccessible(access);
    }

    void addAnnotationProcessor(final Class<? extends Annotation> annotation, final AnnotationProcessor annotationProcessor) {
        annotationProcessors.put(annotation, annotationProcessor);
    }

    void addTypeConverter(final Class<?> type, final TypeConverter typeConverter) {
        typeConverters.put(type, typeConverter);
    }

}
