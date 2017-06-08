/**
 * The MIT License
 *
 *   Copyright (c) 2017, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package org.jeasy.props.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Annotation processor interface.
 * Implementations should provide the logic to get the value to inject in the target field annotated with T.
 * Easy Properties will convert the value to the target field's type and set it in the field.
 *
 * @param <T> The annotation type.
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public interface AnnotationProcessor<T extends Annotation> {

    /**
     * Process an annotation of type T.
     *
     * @param annotation the annotation to process.
     * @param field      the target field
     * @return Return the object to set in the annotated field or null if the value should be ignored
     * @throws AnnotationProcessingException thrown if an exception occurs during annotation processing
     */
    Object processAnnotation(final T annotation, final Field field) throws AnnotationProcessingException;

}
