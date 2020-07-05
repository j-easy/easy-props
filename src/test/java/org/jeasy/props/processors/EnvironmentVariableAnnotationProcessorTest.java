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
import org.jeasy.props.api.PropertyInjectionException;
import org.junit.Test;

public class EnvironmentVariableAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    @Test
    public void testEnvironmentVariableInjection() {
        //given
        class Bean {
            @EnvironmentVariable("JAVA_HOME")
            private String javaHome;
        }
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.javaHome).isNotEmpty();
    }

    @Test
    public void testEnvironmentVariableDefaultValueInjection() {
        //given
        class Bean {
            @EnvironmentVariable(value = "blah", defaultValue = "default")
            private String value;
        }
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);
        
        //then
        assertThat(bean.value).isEqualTo("default");
    }

    @Test(expected = PropertyInjectionException.class)
    public void testAbsentEnvironmentVariableInjection() {
        class Bean {
            @EnvironmentVariable(value = "absent", failFast = true)
            private String absentValue;

        }
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);
    }

}
