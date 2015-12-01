package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.annotations.ManifestProperty;
import io.github.benas.easyproperties.api.PropertiesInjector;
import io.github.benas.easyproperties.api.PropertyInjectionException;
import org.junit.Before;
import org.junit.Test;

import static io.github.benas.easyproperties.impl.PropertiesInjectorBuilder.aNewPropertiesInjector;
import static org.assertj.core.api.Assertions.assertThat;

public class ManifestPropertyAnnotationProcessorTest {

    private PropertiesInjector propertiesInjector;

    @Before
    public void setUp() throws Exception {
        propertiesInjector = aNewPropertiesInjector().build();
    }

    @Test
    public void testManifestPropertyInjection() throws Exception {
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getCreatedBy()).isEqualTo("Apache Maven 3.0.4");
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenJarIsMissing_thenShouldThrowAnException() throws Exception {
        //given
        BeanWithInvalidJar bean = new BeanWithInvalidJar();

        //when
        propertiesInjector.injectProperties(bean);

        //then should throw an exception
    }

    @Test
    public void whenHeaderIsMissing_thenShouldSilentlyIgnoreTheField() throws Exception {
        //given
        BeanWithInvalidHeader bean = new BeanWithInvalidHeader();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getCreatedBy()).isNull();
    }

    public class Bean {

        @ManifestProperty(jar = "junit-4.12.jar", header = "Created-By")
        private String createdBy;

        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    }

    public class BeanWithInvalidJar {

        @ManifestProperty(jar = "blah.jar", header = "Created-By")
        private String createdBy;

        public String getCreatedBy() {
            return createdBy;
        }
        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }
    }

    public class BeanWithInvalidHeader {

        @ManifestProperty(jar = "junit-4.12.jar", header = "blah")
        private String createdBy;

        public String getCreatedBy() {
            return createdBy;
        }
        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }
    }
}
