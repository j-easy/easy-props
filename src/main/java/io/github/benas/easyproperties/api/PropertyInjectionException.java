package io.github.benas.easyproperties.api;

/**
 * Exception thrown when an error occurs during the injection of a property in a given field.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class PropertyInjectionException extends Exception {

    public PropertyInjectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
