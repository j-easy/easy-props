package io.github.benas.easyproperties;

import io.github.benas.easyproperties.api.AnnotationProcessingException;
import io.github.benas.easyproperties.processors.AbstractAnnotationProcessor;

import java.lang.reflect.Field;

public class MyCustomAnnotationProcessor extends AbstractAnnotationProcessor<MyCustomAnnotation> {

    public void processAnnotation(MyCustomAnnotation annotation, Field field, Object object) throws AnnotationProcessingException {
        processAnnotation(object, field, "value", annotation.value());
    }

}
