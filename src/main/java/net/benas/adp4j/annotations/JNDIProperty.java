package net.benas.adp4j.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation injects a property from a JNDI context in the annotated field.
 *
 * @author benas (md.benhassine@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JNDIProperty {

    /**
     * The object name in JNDI context.
     * @return The object name in JNDI context
     */
    public String value();

}
