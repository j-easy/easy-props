package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.annotations.DBProperty;
import io.github.benas.easyproperties.api.PropertiesInjector;
import io.github.benas.easyproperties.api.PropertyInjectionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import static io.github.benas.easyproperties.impl.PropertiesInjectorBuilder.aNewPropertiesInjector;
import static org.assertj.core.api.Assertions.assertThat;

public class DBPropertyAnnotationProcessorTest {

    private EmbeddedDatabase embeddedDatabase;

    private PropertiesInjector propertiesInjector;

    @Before
    public void setUp() throws Exception {
        embeddedDatabase = new EmbeddedDatabaseBuilder().setName("test").addScript("database.sql").build();
        propertiesInjector = aNewPropertiesInjector().build();
    }

    @Test
    public void testPropertyInjectionFromDatabase() throws Exception {
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getName()).isEqualTo("Foo");
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenConfigurationIsMissing_thenShouldThrowAnException() throws Exception {
        //given
        BeanWithInvalidConfiguration bean = new BeanWithInvalidConfiguration();

        //when
        propertiesInjector.injectProperties(bean);

        //then should throw exception
    }

    @Test
    public void whenKeyIsMissing_thenShouldSilentlyIgnoreTheField() throws Exception {
        //given
        BeanWithInvalidKey bean = new BeanWithInvalidKey();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getName()).isNull();
    }

    @After
    public void shutdownEmbeddedDatabase() throws Exception {
        embeddedDatabase.shutdown();
    }

    public class Bean {

        @DBProperty(configuration = "database.properties", key = "bean.name")
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public class BeanWithInvalidConfiguration {

        @DBProperty(configuration = "blah.properties", key = "bean.name")
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public class BeanWithInvalidKey {

        @DBProperty(configuration = "database.properties", key = "blah")
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

}
