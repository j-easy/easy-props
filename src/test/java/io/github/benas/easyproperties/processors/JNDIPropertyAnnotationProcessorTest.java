package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.annotations.JNDIProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;

import static org.assertj.core.api.Assertions.assertThat;

public class JNDIPropertyAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = new InitialContext();
        context.bind("foo.property", "jndi");
    }

    @Test
    public void testJNDIPropertyInjection() {
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getJndiProperty()).isEqualTo("jndi");
    }

    @Test
    public void whenKeyIsMissing_thenShouldSilentlyIgnoreTheField() {
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
