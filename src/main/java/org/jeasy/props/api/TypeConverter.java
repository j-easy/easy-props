package org.jeasy.props.api;

/**
 * Interface for type conversion.
 *
 * @param <S> The source type.
 * @param <T> The target type.
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
@FunctionalInterface
public interface TypeConverter<S, T> {

	/**
	 * Convert a value from a source type to a target type.
	 *
	 * @param source the input value to convert
	 * @return The converted value
	 * @throws TypeConversionException if an error occurs during type conversion
	 */
	T convert(S source) throws TypeConversionException;
	
}
