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
package org.jeasy.props.processors;

import org.jeasy.props.annotations.SystemProperty;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemPropertyAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    private Bean bean;

    @Before
    public void setUp() throws Exception {
        System.setProperty("threshold", "30");
        super.setUp();
    }

    @Test
    public void testSystemPropertyInjection() {
        //given
        bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getUserHome()).isEqualTo(System.getProperty("user.home"));
    }

    @Test
    public void testSystemPropertyDefaultValueInjection() {
        //given
        bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getValue()).isEqualTo("default");
    }

    @Test
    public void testSystemPropertyInjectionWithTypeConversion() {
        //given
        bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getThreshold()).isEqualTo(30);
    }

    public class Bean {

        @SystemProperty("user.home")
        private String userHome;

        @SystemProperty(value = "blah", defaultValue = "default")
        private String value;

        @SystemProperty("threshold")
        private int threshold;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getUserHome() {
            return userHome;
        }

        public void setUserHome(String userHome) {
            this.userHome = userHome;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }
    }

}
