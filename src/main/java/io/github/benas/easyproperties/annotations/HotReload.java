package io.github.benas.easyproperties.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Annotation to mark an object for hot configuration reloading.
 *
 * This will register a background thread to reload the configuration periodically in the target object.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HotReload {

    /**
     * The period to wait before reloading configuration.
     *
     * @return The period to wait before reloading configuration
     */
    long period() default 15;

    /**
     * Time unit of the waiting period.
     *
     * @return Time unit of the waiting period
     */
    TimeUnit unit() default TimeUnit.MINUTES;

}
