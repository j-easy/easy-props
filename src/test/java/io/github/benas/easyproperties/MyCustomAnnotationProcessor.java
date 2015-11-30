package io.github.benas.easyproperties;

import io.github.benas.easyproperties.api.AnnotationProcessingException;
import io.github.benas.easyproperties.api.AnnotationProcessor;

import java.lang.reflect.Field;

import static java.lang.String.format;

public class MyCustomAnnotationProcessor implements AnnotationProcessor<MyCustomAnnotation> {

    @Override
    public void processAnnotation(MyCustomAnnotation annotation, Field field, Object object) throws AnnotationProcessingException {
        String value = annotation.value();
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true); // or else IllegalAccessException
            field.set(object, value);
            field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            throw new AnnotationProcessingException(format("Unable to inject value '%s' in field '%s' of object '%s'",
                    value, field.getName(), object), e);
        }
    }

}
