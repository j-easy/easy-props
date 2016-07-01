package io.github.benas.easyproperties.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HotReload {

    /**
     * The period (in ms) of configuration reloading.
     *
     * @return period (in ms) of reloading
     */
    long period() default 60 * 1000;

}
