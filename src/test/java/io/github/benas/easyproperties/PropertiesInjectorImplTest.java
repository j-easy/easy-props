/*
 *   The MIT License
 *
 *    Copyright (c) 2016, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy
 *    of this software and associated documentation files (the "Software"), to deal
 *    in the Software without restriction, including without limitation the rights
 *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *    copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in
 *    all copies or substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *    THE SOFTWARE.
 */

package io.github.benas.easyproperties;

import io.github.benas.easyproperties.api.PropertiesInjector;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;
import java.util.ResourceBundle;

import static io.github.benas.easyproperties.impl.PropertiesInjectorBuilder.aNewPropertiesInjector;
import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesInjectorImplTest {

    private PropertiesInjector propertiesInjector;

    private Bean bean;

    private Context context;

    private Properties properties;

    private ResourceBundle resourceBundle;

    @BeforeClass
    public static void initEmbeddedDB() throws Exception {
        new EmbeddedDatabaseBuilder().setName("test").addScript("database.sql").build();
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("threshold", "30");
        context = new InitialContext();
        context.bind("foo.property", "jndi");
        properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("myProperties.properties"));
        resourceBundle = ResourceBundle.getBundle("i18n/messages");
        propertiesInjector = aNewPropertiesInjector().build();
        bean = new Bean();
        propertiesInjector.injectProperties(bean);
    }

    @Test
    public void testSystemPropertyInjection() throws Exception {
        assertThat(bean.getUserHome()).isEqualTo(System.getProperty("user.home"));//test String property injection
    }

    @Test
    public void testSystemPropertyDefaultValueInjection() throws Exception {
        assertThat(bean.getValue()).isEqualTo("default");//test default value injection
    }

    @Test
    public void testSystemMavenVersionValueInjection() throws Exception {
        assertThat(bean.getPomVersion()).isEqualTo("1.9.2"); //test maven value injection
    }

    @Test
    public void testSystemPropertyInjectionWithTypeConversion() throws Exception {
        assertThat(bean.getThreshold()).isEqualTo(30); //test type conversion
    }

    @Test
    public void testI18NPropertyInjection() throws Exception {
        assertThat(bean.getMessage()).isEqualTo(resourceBundle.getString("my.message"));
    }

    @Test
    public void testPropertyInjection() throws Exception {
        assertThat(bean.getBeanName()).isEqualTo("Foo");
    }

    @Test
    public void testPropertiesInjection() throws Exception {
        assertThat(bean.getMyProperties().getProperty("bean.name")).isEqualTo(properties.getProperty("bean.name"));
    }

    @Test
    public void testDBPropertyInjection() throws Exception {
        assertThat(bean.getName()).isEqualTo("Foo");
    }

    @Test
    public void testJNDIPropertyInjection() throws Exception {
        assertThat(bean.getJndiProperty()).isEqualTo("jndi");
    }

    @Test
    public void testManifestPropertyInjection() throws Exception {
        assertThat(bean.getCreatedBy()).isEqualTo("Apache Maven 3.0.4");
    }

    @Test
    public void testEmptyPropertyInjection() throws Exception {
        assertThat(bean.getEmptyField()).isNull();
    }

    @After
    public void tearDown() throws Exception {
        propertiesInjector = null;
        context.close();
        bean = null;
    }

}
