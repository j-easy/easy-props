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

import org.jeasy.props.annotations.ManifestProperty;
import org.jeasy.props.api.PropertyInjectionException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ManifestPropertyAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    @Test
    public void testManifestPropertyInjection() {
        //given
        class Bean {
            @ManifestProperty(jar = "junit-4.13.1.jar", header = "Created-By")
            private String createdBy;
        }
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.createdBy).isEqualTo("Apache Maven 3.1.1");
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenJarIsMissing_thenShouldThrowAnException() {
        //given
        class BeanWithInvalidJar {
            @ManifestProperty(jar = "blah.jar", header = "Created-By")
            private String createdBy;
        }
        BeanWithInvalidJar bean = new BeanWithInvalidJar();

        //when
        propertiesInjector.injectProperties(bean);

        //then should throw an exception
    }

    @Test
    public void whenHeaderIsMissing_thenShouldSilentlyIgnoreTheField() {
        //given
        class BeanWithInvalidHeader {
            @ManifestProperty(jar = "junit-4.13.1.jar", header = "blah")
            private String createdBy;
        }
        BeanWithInvalidHeader bean = new BeanWithInvalidHeader();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.createdBy).isNull();
    }

    @Test
    public void whenHeaderIsMissingWithDefaultValue_thenShouldInjectDefaultValue() {
        //given
        class BeanWithInvalidHeader {
            @ManifestProperty(jar = "junit-4.13.1.jar", header = "blah", defaultValue = "default")
            private String createdBy;
        }
        BeanWithInvalidHeader bean = new BeanWithInvalidHeader();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.createdBy).isEqualTo("default");
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenKeyIsMissingAndFailFast_thenShouldThrowException() {
        class Bean {
            @ManifestProperty(jar = "junit-4.13.1.jar", header = "blah", failFast = true)
            private String absentValue;

        }
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);
    }

}
