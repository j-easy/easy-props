package net.benas.adp4j.test;

import net.benas.adp4j.api.PropertiesInjector;
import net.benas.adp4j.impl.PropertiesInjectorBuilder;
import org.junit.*;

import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Test class for ADP4J {@link net.benas.adp4j.api.PropertiesInjector} implementation.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class PropertiesInjectorTest {

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

    @After
    public void tearDown() throws Exception {
        propertiesInjector = null;
        context.close();
        bean = null;
    }

}
