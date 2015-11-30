package io.github.benas.easyproperties.api;

/**
 * Exception thrown when an error occurs during the processing an injection annotation.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class AnnotationProcessingException extends Exception {

    public AnnotationProcessingException(final String message) {
        super(message);
    }

    public AnnotationProcessingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
