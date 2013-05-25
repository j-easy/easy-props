package net.benas.adp4j.processors;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Field;
import java.text.MessageFormat;

/**
 * Base class for {@link net.benas.adp4j.api.AnnotationProcessor} implementations providing common methods.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public abstract class AbstractAnnotationProcessor {

    /**
     * Convert the value to field type and set it in the target object.
     *
     * @param target the target object
     * @param field the annotated field
     * @param key the annotation property attribute
     * @param value the value to inject
     * @throws Exception thrown if an exception occurs when trying to set the field value
     */
    protected void injectProperty(Object target, Field field, String key, Object value) throws Exception {

        Object typedValue = ConvertUtils.convert(value, field.getType());
        try {
            PropertyUtils.setProperty(target, field.getName(), typedValue);
        } catch (Exception e) {
            throw new Exception("Unable to set property " + key + " on field " + field.getName() + " of type " +
                    target.getClass() + ". A setter may be missing for this field.", e);
        }

    }

    /**
     * Constructs an error message to signal missing properties file.
     *
     * @param source the source file name
     * @param field the target field
     * @param object the target object
     * @return the formatted error message
     */
    protected String missingSourceFile(String source, Field field, Object object) {
        return MessageFormat.format("Unable to load properties from source {0} for field {1} of type {2}",
                source, field.getName(), object.getClass().getName());
    }

    /**
     * Constructs an error message to signal missing annotation attribute value.
     *
     * @param attribute the target attribute
     * @param annotation the annotation concerned
     * @param field the target field
     * @param object the target object
     * @return the formatted error message
     */
    protected String missingAttributeValue(String attribute, String annotation,  Field field, Object object) {
        return MessageFormat.format("No value specified for attribute {0} of {1} annotation on field {2} of type {3}",
                attribute, annotation, field.getName(), object.getClass().getName());
    }

}