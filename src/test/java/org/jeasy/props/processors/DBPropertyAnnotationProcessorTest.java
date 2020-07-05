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

import org.jeasy.props.annotations.DBProperty;
import org.jeasy.props.api.PropertyInjectionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class DBPropertyAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    private EmbeddedDatabase embeddedDatabase;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .setName("test")
                .addScript("database.sql")
                .build();
    }

    @Test
    public void testPropertyInjectionFromDatabase() {
        //given
        class Bean {
            @DBProperty(configuration = "database.properties", key = "name")
            private String name;
        }
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.name).isEqualTo("Foo");
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenConfigurationIsMissing_thenShouldThrowAnException() {
        //given
        class BeanWithInvalidConfiguration {
            @DBProperty(configuration = "blah.properties", key = "name")
            private String name;
        }
        BeanWithInvalidConfiguration bean = new BeanWithInvalidConfiguration();

        //when
        propertiesInjector.injectProperties(bean);

        //then should throw exception
    }

    @Test
    public void whenKeyIsMissing_thenShouldSilentlyIgnoreTheField() {
        //given
        class BeanWithInvalidKey {
            @DBProperty(configuration = "database.properties", key = "blah")
            private String name;
        }
        BeanWithInvalidKey bean = new BeanWithInvalidKey();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.name).isNull();
    }

    @Test
    public void whenKeyIsMissingWithDefaultValue_thenShouldInjectDefaultValue() {
        //given
        class BeanWithInvalidKey {
            @DBProperty(configuration = "database.properties", key = "blah", defaultValue = "default")
            private String name;
        }
        BeanWithInvalidKey bean = new BeanWithInvalidKey();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.name).isEqualTo("default");
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenKeyIsMissingAndFailFast_thenShouldThrowException() {
        class Bean {
            @DBProperty(configuration = "database.properties", key = "blah", failFast = true)
            private String absentValue;

        }
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);
    }

    @After
    public void shutdownEmbeddedDatabase() {
        embeddedDatabase.shutdown();
    }

}
