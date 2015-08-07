/*
 *   The MIT License
 *
 *    Copyright (c) 2015, Mahmoud Ben Hassine (mahmoud@benhassine.fr)
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

package io.github.benas.projector;

import io.github.benas.projector.api.PropertiesInjector;
import io.github.benas.projector.impl.PropertiesInjectorBuilder;
import org.junit.*;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Test class for ADP4J {@link io.github.benas.projector.api.PropertiesInjector} implementation.
 *
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */
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
        propertiesInjector = new PropertiesInjectorBuilder().build();
        bean = new Bean();
        propertiesInjector.injectProperties(bean);
    }

    @Test
    public void testSystemPropertyInjection() throws Exception {
        Assert.assertEquals(System.getProperty("user.home"), bean.getUserHome()); //test String property injection
    }

    @Test
    public void testSystemPropertyDefaultValueInjection() throws Exception {
        Assert.assertEquals("default", bean.getValue()); //test default value injection
    }

    @Test
    public void testSystemMavenVersionValueInjection() throws Exception {
        Assert.assertEquals("1.9.2", bean.getPomVersion()); //test maven value injection
    }

    @Test
    public void testSystemPropertyInjectionWithTypeConversion() throws Exception {
        Assert.assertEquals(30, bean.getThreshold()); //test type conversion
    }

    @Test
    public void testI18NPropertyInjection() throws Exception {
        Assert.assertEquals(resourceBundle.getString("my.message") , bean.getMessage());
    }

    @Test
    public void testPropertyInjection() throws Exception {
        Assert.assertEquals("Foo", bean.getBeanName());
    }

    @Test
    public void testPropertiesInjection() throws Exception {
        Assert.assertEquals(properties.getProperty("bean.name"), bean.getMyProperties().getProperty("bean.name"));
    }

    @Test
    public void testDBPropertyInjection() throws Exception {
        Assert.assertEquals("Foo", bean.getName());
    }

    @Test
    public void testJNDIPropertyInjection() throws Exception {
        Assert.assertEquals("jndi", bean.getJndiProperty());
    }

    @Test
    public void testManifestPropertyInjection() throws Exception {
        Assert.assertEquals("Apache Maven 3.0.4", bean.getCreatedBy());
    }

    @Test
    public void testEmptyPropertyInjection() throws Exception {
        Pojo pojo = new Pojo();

        propertiesInjector.injectProperties(pojo);

        Assert.assertEquals("value1", pojo.getField1());
        Assert.assertEquals("value2", pojo.getField2());
        Assert.assertNull(pojo.getField3());
        Assert.assertEquals("value4", pojo.getField4());
    }

    @After
    public void tearDown() throws Exception {
        propertiesInjector = null;
        context.close();
        bean = null;
    }

}
