/*
 *   The MIT License
 *
 *    Copyright (c) 2015, Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy
 *    of this software and associated documentation files (the "Software"), to deal
 *    in the Software without restriction, including without limitation the rights
 *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *    copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in
 *    all copies or substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *    THE SOFTWARE.
 */

package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.annotations.JNDIProperty;
import io.github.benas.easyproperties.api.AnnotationProcessor;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.lang.reflect.Field;

/**
 * An annotation processor that loads properties from a JNDI context.
 *
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */
public class JNDIPropertyAnnotationProcessor extends AbstractAnnotationProcessor implements AnnotationProcessor<JNDIProperty> {

    /**
     * The JNDI context.
     */
    private Context context;

    @Override
    public void processAnnotation(final JNDIProperty jndiPropertyAnnotation, final Field field, Object object) throws Exception {

        if (context == null) {
            context = new InitialContext(); // not in constructor cause throw NamingException
        }

        String name = jndiPropertyAnnotation.value().trim();

        //check name attribute value
        if (name.isEmpty()) {
            throw new Exception(missingAttributeValue("name", "@JNDIProperty", field, object));
        }

        //get object from JNDI context
        Object value = context.lookup(name);

        //check object obtained from JNDI context
        if (value == null) {
            throw new Exception("JNDI object " + name + " not found in JNDI context.");
        }

        //inject object in annotated field
        injectProperty(object, field, name, value);

    }

}
