package org.jeasy.props;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.jeasy.props.annotations.Property;
import org.jeasy.props.annotations.SystemProperty;
import org.jeasy.props.api.AnnotationProcessingException;
import org.jeasy.props.processors.AbstractAnnotationProcessor;
import org.jeasy.props.processors.PropertyAnnotationProcessor;
import org.jeasy.props.processors.SystemPropertyAnnotationProcessor;

public class MyCompositeAnnotationProcessor extends AbstractAnnotationProcessor<MyCompositeAnnotation> {

	private PropertyAnnotationProcessor propertyAnnotationProcessor = new PropertyAnnotationProcessor();
	private SystemPropertyAnnotationProcessor systemPropertyAnnotationProcessor = new SystemPropertyAnnotationProcessor();
	
	@Override
	public Object processAnnotation(MyCompositeAnnotation annotation, Field field) throws AnnotationProcessingException {
		final String key = annotation.key();
		final String source = annotation.source();
		
		Property property = getProperty(source, key);
		SystemProperty systemProperty = getSystemProperty(key);

		// inject value from properties file and fallback to system property if missing
		Object value = propertyAnnotationProcessor.processAnnotation(property, field);
		if (value == null) {
			value = systemPropertyAnnotationProcessor.processAnnotation(systemProperty, field);
		}
		return value;
	}

	private SystemProperty getSystemProperty(String key) {
		return new SystemProperty() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return SystemProperty.class;
			}
			@Override
			public String value() {
				return key;
			}
			@Override
			public String defaultValue() {
				return "";
			}
			@Override
			public boolean failFast() {
				return false;
			}
		};
	}

	private Property getProperty(String source, String key) {
		return new Property() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return Property.class;
			}

			@Override
			public String source() {
				return source;
			}

			@Override
			public String key() {
				return key;
			}

			@Override
			public String defaultValue() {
				return "";
			}

			@Override
			public boolean failFast() {
				return false;
			}
		};
	}
}
