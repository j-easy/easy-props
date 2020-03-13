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

import static org.assertj.core.api.Assertions.assertThat;

import org.jeasy.props.annotations.EnvironmentVariable;
import org.junit.Test;

public class EnvironmentVariableAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    private Bean bean;

    @Test
    public void testEnvironmentVariableInjection() {
        //given
        bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getJavaHome()).isNotEmpty();
    }

    @Test
    public void testEnvironmentVariableDefaultValueInjection() {
        //given
        bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);
        
        //then
        assertThat(bean.getValue()).isEqualTo("default");
    }

    public static class Bean {

        @EnvironmentVariable("JAVA_HOME")
        private String javaHome;

        @EnvironmentVariable(value = "blah", defaultValue = "default")
        private String value;


        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getJavaHome() {
            return javaHome;
        }

        public void setJavaHome(String javaHome) {
            this.javaHome = javaHome;
        }

    }

}
