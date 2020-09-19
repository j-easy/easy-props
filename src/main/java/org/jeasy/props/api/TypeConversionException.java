package org.jeasy.props.api;

/**
 * Exception to signal a type conversion error.
 * 
 * @see TypeConverter
 */
public class TypeConversionException extends RuntimeException {

	/**
	 * Create a new {@link TypeConversionException}.
	 * 
	 * @param message of the exception
	 */
	public TypeConversionException(String message) {
		super(message);
	}

	/**
	 * Create a new {@link TypeConversionException}.
	 * 
	 * @param message of the exception
	 * @param cause of the exception
	 */
	public TypeConversionException(String message, Throwable cause) {
		super(message, cause);
	}
}
