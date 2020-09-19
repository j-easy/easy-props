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
package org.jeasy.props;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.jeasy.props.api.PropertiesInjector;
import org.jeasy.props.api.TypeConverter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.management.ObjectName;

import static org.jeasy.props.PropertiesInjectorBuilder.aNewPropertiesInjector;
import static org.jeasy.props.PropertiesInjectorBuilder.aNewPropertiesInjectorBuilder;
import static java.lang.Thread.sleep;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesInjectorImplTest {

    private PropertiesInjector propertiesInjector;

    @Before
    public void setUp() {
        System.setProperty("date", "01/03/2020");
        propertiesInjector = aNewPropertiesInjectorBuilder()
                .registerAnnotationProcessor(MyCustomAnnotation.class, new MyCustomAnnotationProcessor())
                .registerTypeConverter(LocalDate.class, (TypeConverter<String, LocalDate>) source -> LocalDate.parse(source, DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .build();
    }

    @Test
    public void testCustomAnnotationProcessor() {
        //given
        Config config = new Config();

        //when
        propertiesInjector.injectProperties(config);

        //then
        assertThat(config.getCustom()).isEqualTo("foo");
    }

    @Test
    public void testCustomTypeConverter() {
        //given
        Config config = new Config();

        //when
        propertiesInjector.injectProperties(config);

        //then
        assertThat(config.getDate()).isEqualTo(LocalDate.of(2020, 3, 1));
    }

    @Test
    public void testConfigurationHotReloading() throws Exception {
        //given
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder().setName("test").addScript("database.sql").build();
        System.setProperty("sp", "foo");
        HotReloadableConfig config = new HotReloadableConfig();

        //when
        propertiesInjector.injectProperties(config);

        //then
        assertThat(config.getSystemProperty()).isEqualTo("foo");
        assertThat(config.getName()).isEqualTo("Foo");

        // Properties changes should be reloaded
        System.setProperty("sp","bar");
        new JdbcTemplate(database).update("update ApplicationProperties set value = ? where key = ?", "Bar", "name");
        sleep(2 * 1000);
        assertThat(config.getSystemProperty()).isEqualTo("bar");
        assertThat(config.getName()).isEqualTo("Bar");
        database.shutdown();
    }

    @Test
    public void testManageableConfiguration() throws Exception {
        System.setProperty("sp", "foo");
        ManageableConfig config = new ManageableConfig();
        aNewPropertiesInjector().injectProperties(config);

        ObjectName objectName = new ObjectName("org.jeasy.props:name=myConfig");
        assertThat(getPlatformMBeanServer().isRegistered(objectName)).isTrue();
    }
}
