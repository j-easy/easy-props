/*
 * The MIT License
 *
 *   Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
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
package org.jeasy.props.processors;

import org.jeasy.props.annotations.I18NProperty;
import org.jeasy.props.api.PropertyInjectionException;
import org.junit.Before;
import org.junit.Test;

import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;
import static org.assertj.core.api.Assertions.assertThat;

public class I18NPropertyAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    private ResourceBundle resourceBundle;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        resourceBundle = getBundle("i18n/messages");
    }

    @Test
    public void testI18NPropertyInjection() {
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getMessage()).isEqualTo(resourceBundle.getString("my.message"));
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenBundleIsMissing_thenShouldThrowAnException() throws Exception {
        //given
        BeanWithInvalidBundle bean = new BeanWithInvalidBundle();

        //when
        propertiesInjector.injectProperties(bean);

        //then should throw exception
    }

    @Test
    public void whenKeyIsMissing_thenShouldSilentlyIgnoreTheField() {
        //given
        BeanWithInvalidKey bean = new BeanWithInvalidKey();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getMessage()).isNull();
    }

    public class Bean {

        @I18NProperty(bundle = "i18n/messages", key = "my.message")
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public class BeanWithInvalidBundle {

        @I18NProperty(bundle = "blah", key = "my.message")
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public class BeanWithInvalidKey {

        @I18NProperty(bundle = "i18n/messages", key = "blah")
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
