package io.github.benas.easyproperties;

import io.github.benas.easyproperties.processors.AbstractAnnotationProcessor;

import java.lang.reflect.Field;

public class MyCustomAnnotationProcessor extends AbstractAnnotationProcessor<MyCustomAnnotation> {

    public Object processAnnotation(MyCustomAnnotation annotation, Field field) {
        return annotation.value();
    }

}
