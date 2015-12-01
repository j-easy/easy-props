package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.annotations.JNDIProperty;
import io.github.benas.easyproperties.api.PropertiesInjector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;

import static io.github.benas.easyproperties.impl.PropertiesInjectorBuilder.aNewPropertiesInjector;
import static org.assertj.core.api.Assertions.assertThat;

public class JNDIPropertyAnnotationProcessorTest {

    private Context context;

    private PropertiesInjector propertiesInjector;

    @Before
    public void setUp() throws Exception {
        context = new InitialContext();
        context.bind("foo.property", "jndi");
        propertiesInjector = aNewPropertiesInjector().build();
    }

    @Test
    public void testJNDIPropertyInjection() throws Exception {
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getJndiProperty()).isEqualTo("jndi");
    }

    @Test
    public void whenKeyIsMissing_thenShouldSilentlyIgnoreTheField() throws Exception {
        //given
        BeanWithInvalidKey bean = new BeanWithInvalidKey();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getJndiProperty()).isNull();
    }

    @After
    public void tearDown() throws Exception {
        context.close();
    }

    public class Bean {

        @JNDIProperty("foo.property")
        private String jndiProperty;

        public String getJndiProperty() { return jndiProperty; }
        public void setJndiProperty(String jndiProperty) { this.jndiProperty = jndiProperty; }
    }

    public class BeanWithInvalidKey {

        @JNDIProperty("blah")
        private String jndiProperty;

        public String getJndiProperty() { return jndiProperty; }
        public void setJndiProperty(String jndiProperty) { this.jndiProperty = jndiProperty; }
    }
}
