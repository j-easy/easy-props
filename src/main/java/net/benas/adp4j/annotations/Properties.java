package net.benas.adp4j.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be declared on a field of type {@link java.util.Properties} in which all properties of a given
 * properties file should be injected.
 *
 * @author benas (md.benhassine@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Properties {

    /**
     * The properties file name.
     * @return The properties file name
     */
    public String value();

}
