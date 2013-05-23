package net.benas.adp4j.test;

import net.benas.adp4j.api.Configurator;
import net.benas.adp4j.impl.ConfiguratorBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Test class for ADP4J {@link Configurator} implementation.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class ConfiguratorTest {

    private Configurator configurator;

    private Bean bean;

    @Before
    public void setUp() throws Exception {
        System.setProperty("threshold", "30");
        configurator = new ConfiguratorBuilder().build();
        bean = new Bean();
        configurator.configure(bean);
    }

    @Test
    public void testSystemPropertyInjection() throws Exception {
        Assert.assertEquals(System.getProperty("user.home"), bean.getUserHome()); //test String property injection
    }

    @Test
    public void testSystemPropertyInjectionWithTypeConversion() throws Exception {
        Assert.assertEquals(30, bean.getThreshold()); //test type conversion
    }

    @Test
    public void testI18NPropertyInjection() throws Exception {
        Assert.assertEquals(ResourceBundle.getBundle("i18n/messages").getString("my.message"), bean.getMessage());
    }

    @Test
    public void testPropertyInjection() throws Exception {
        Assert.assertEquals("Foo", bean.getBeanName());
    }

    @Test
    public void testPropertiesInjection() throws Exception {
        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("myProperties.properties"));
        Assert.assertEquals(properties.getProperty("bean.name"), bean.getMyProperties().getProperty("bean.name"));
    }

    @After
    public void tearDown() throws Exception {
        configurator = null;
        bean = null;
    }

}
