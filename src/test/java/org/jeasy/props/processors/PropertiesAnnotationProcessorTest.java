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

import org.jeasy.props.annotations.Properties;
import org.jeasy.props.api.PropertyInjectionException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    @Test
    public void testPropertiesInjection() {
        //given
        class Bean {
            @Properties("myProperties.properties")
            private java.util.Properties myProperties;
        }
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.myProperties).containsKey("bean.name");
        assertThat(bean.myProperties.getProperty("bean.name")).isEqualTo("Foo");
        assertThat(bean.myProperties).containsKey("empty.key");
        assertThat(bean.myProperties.getProperty("empty.key")).isEmpty();
    }

    @Test
    public void testPropertiesInjectionWithDefaultValue() {
        //given
        class Bean {
            @Properties(value = "missingProperties.properties", defaultValue = "myDefaultProperties.properties")
            private java.util.Properties myProperties;
        }
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.myProperties).containsKey("bean.name");
        assertThat(bean.myProperties.getProperty("bean.name")).isEqualTo("Foo");
        assertThat(bean.myProperties).containsKey("empty.key");
        assertThat(bean.myProperties.getProperty("empty.key")).isEmpty();
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenPropertiesFileIsInvalid_thenShouldThrowAnException() {
        //given
        class BeanWithInvalidProperties {
            @Properties("blah.properties")
            private java.util.Properties myProperties;
        }
        BeanWithInvalidProperties bean = new BeanWithInvalidProperties();

        //when
        propertiesInjector.injectProperties(bean);

        //then should throw an exception
    }

}
