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

import org.jeasy.props.annotations.Property;
import org.jeasy.props.api.PropertyInjectionException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    @Test
    public void testPropertyInjection() {
        //given
        class Bean {
            @Property(source = "classpath:myProperties.properties", key = "bean.name")
            private String beanName;
            @Property(source = "file:src/test/resources/fileSystemProperties.properties", key = "bean.age")
            private int beanAge;
        }
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.beanName).isEqualTo("Foo");
        assertThat(bean.beanAge).isEqualTo(30);
    }

    @Test
    public void testPropertyInjectionOfMissingKeyWithDefaultValue() {
        //given
        class Bean {
            @Property(source = "classpath:myProperties.properties", key = "missing.key", defaultValue = "default")
            private String beanName;
            @Property(source = "file:src/test/resources/fileSystemProperties.properties", key = "missing.key", defaultValue = "10")
            private int beanAge;
        }
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.beanName).isEqualTo("default");
        assertThat(bean.beanAge).isEqualTo(10);
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenKeyIsMissingAndFailFast_thenShouldThrowException() {
        class Bean {
            @Property(source = "classpath:myProperties.properties", key = "missing.key", failFast = true)
            private String beanName;

        }
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);
    }

    @Test
    public void testPropertyInjectionOfMissingKeyWithoutDefaultValue() {
        //given
        class Bean {
            @Property(source = "classpath:myProperties.properties", key = "missing.key")
            private String beanName;
            @Property(source = "file:src/test/resources/fileSystemProperties.properties", key = "missing.key")
            private int beanAge;
        }
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.beanName).isNull();
        assertThat(bean.beanAge).isEqualTo(0);
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenPropertiesFileIsInvalid_thenShouldThrowAnException() {
        //given
        class BeanWithInvalidPropertiesFile {
            @Property(source = "blah.properties", key = "bean.name")
            private String beanName;
        }
        BeanWithInvalidPropertiesFile bean = new BeanWithInvalidPropertiesFile();

        //when
        propertiesInjector.injectProperties(bean);

        //then should throw an exception
    }

    @Test
    public void whenKeyIsMissing_thenShouldIgnoreField() {
        //given
        class BeanWithMissingKey {
            @Property(source = "myProperties.properties", key = "unknown.key")
            private String unknownField;
        }
        BeanWithMissingKey bean = new BeanWithMissingKey();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.unknownField).isNull();
    }

    @Test
    public void whenKeyIsEmpty_thenShouldIgnoreField() {
        //given
        class BeanWithEmptyKey {
            @Property(source = "myProperties.properties", key = "empty.key")
            private String emptyField;
        }

        BeanWithEmptyKey bean = new BeanWithEmptyKey();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.emptyField).isNull();
    }

}
