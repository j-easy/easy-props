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
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    private java.util.Properties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        properties = new java.util.Properties();
        properties.load(getResourceAsStream("myProperties.properties"));
    }

    @Test
    public void testPropertiesInjection() {
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getMyProperties()).containsKey("bean.name");
        assertThat(bean.getMyProperties().getProperty("bean.name")).isEqualTo(properties.getProperty("bean.name"));
        assertThat(bean.getMyProperties()).containsKey("empty.key");
        assertThat(bean.getMyProperties().getProperty("empty.key")).isEqualTo(properties.getProperty("empty.key"));
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenPropertiesFileIsInvalid_thenShouldThrowAnException() {
        //given
        BeanWithInvalidProperties bean = new BeanWithInvalidProperties();

        //when
        propertiesInjector.injectProperties(bean);

        //then should throw an exception
    }

    public static class Bean {

        @Properties("myProperties.properties")
        private java.util.Properties myProperties;

        public java.util.Properties getMyProperties() { return myProperties; }
        public void setMyProperties(java.util.Properties myProperties) { this.myProperties = myProperties; }
    }

    public static class BeanWithInvalidProperties {

        @Properties("blah.properties")
        private java.util.Properties myProperties;

        public java.util.Properties getMyProperties() { return myProperties; }
        public void setMyProperties(java.util.Properties myProperties) { this.myProperties = myProperties; }
    }

    private InputStream getResourceAsStream(final String resource) {
        return this.getClass().getClassLoader().getResourceAsStream(resource);
    }

}
