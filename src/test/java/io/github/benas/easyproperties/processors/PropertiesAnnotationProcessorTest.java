package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.annotations.Properties;
import io.github.benas.easyproperties.api.PropertyInjectionException;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    private java.util.Properties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        properties = new java.util.Properties();
        properties.load(getResourceAsStream("myProperties.properties"));
    }

    @Test
    public void testPropertiesInjection() throws Exception {
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getMyProperties()).containsKey("bean.name");
        assertThat(bean.getMyProperties().getProperty("bean.name")).isEqualTo(properties.getProperty("bean.name"));
        assertThat(bean.getMyProperties()).containsKey("empty.key");
        assertThat(bean.getMyProperties().getProperty("empty.key")).isEqualTo(properties.getProperty("empty.key"));
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenPropertiesFileIsInvalid_thenShouldThrowAnException() throws Exception {
        //given
        BeanWithInvalidProperties bean = new BeanWithInvalidProperties();

        //when
        propertiesInjector.injectProperties(bean);

        //then should throw an exception
    }

    public class Bean {

        @Properties("myProperties.properties")
        private java.util.Properties myProperties;

        public java.util.Properties getMyProperties() { return myProperties; }
        public void setMyProperties(java.util.Properties myProperties) { this.myProperties = myProperties; }
    }

    public class BeanWithInvalidProperties {

        @Properties("blah.properties")
        private java.util.Properties myProperties;

        public java.util.Properties getMyProperties() { return myProperties; }
        public void setMyProperties(java.util.Properties myProperties) { this.myProperties = myProperties; }
    }

    private InputStream getResourceAsStream(final String resource) {
        return this.getClass().getClassLoader().getResourceAsStream(resource);
    }

}
