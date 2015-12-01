package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.annotations.MavenProperty;
import io.github.benas.easyproperties.api.PropertiesInjector;
import io.github.benas.easyproperties.api.PropertyInjectionException;
import org.junit.Before;
import org.junit.Test;

import static io.github.benas.easyproperties.impl.PropertiesInjectorBuilder.aNewPropertiesInjector;
import static org.assertj.core.api.Assertions.assertThat;

public class MavenPropertyAnnotationProcessorTest {

    private PropertiesInjector propertiesInjector;

    @Before
    public void setUp() throws Exception {
        propertiesInjector = aNewPropertiesInjector().build();
    }

    @Test
    public void testMavenPropertyInjection() throws Exception {
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getPomVersion()).isEqualTo("1.9.2");
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenSourceIsInvalid_thenShouldThrowAnException() throws Exception {
        //given
        BeanWithInvalidSource bean = new BeanWithInvalidSource();

        //when
        propertiesInjector.injectProperties(bean);

        //then should throw an exception
    }

    @Test
    public void whenKeyIsMissing_thenShouldSilentlyIgnoreTheField() throws Exception {
        //given
        BeanWithInvalidKey bean = new BeanWithInvalidKey();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getPomVersion()).isNull();
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenGroupIdIsMissing_thenShouldThrowAnException() throws Exception {
        //given
        BeanWithInvalidGroupId bean = new BeanWithInvalidGroupId();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getPomVersion()).isNull();
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenArtifactIdIsMissing_thenShouldThrowAnException() throws Exception {
        //given
        BeanWithInvalidArtifactId bean = new BeanWithInvalidArtifactId();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getPomVersion()).isNull();
    }

    public class Bean {

        @MavenProperty(key = "version", groupId = "commons-beanutils", artifactId = "commons-beanutils")
        private String pomVersion;

        public String getPomVersion() { return pomVersion; }
        public void setPomVersion(String pomVersion) { this.pomVersion = pomVersion; }
    }

    public class BeanWithInvalidSource {

        @MavenProperty(source = "blah.properties", key = "version", groupId = "commons-beanutils", artifactId = "commons-beanutils")
        private String pomVersion;

        public String getPomVersion() { return pomVersion; }
        public void setPomVersion(String pomVersion) { this.pomVersion = pomVersion; }
    }

    public class BeanWithInvalidKey {

        @MavenProperty(key = "blah", groupId = "commons-beanutils", artifactId = "commons-beanutils")
        private String pomVersion;

        public String getPomVersion() { return pomVersion; }
        public void setPomVersion(String pomVersion) { this.pomVersion = pomVersion; }
    }

    public class BeanWithInvalidGroupId {

        @MavenProperty(key = "version", groupId = "blah", artifactId = "commons-beanutils")
        private String pomVersion;

        public String getPomVersion() { return pomVersion; }
        public void setPomVersion(String pomVersion) { this.pomVersion = pomVersion; }
    }

    public class BeanWithInvalidArtifactId {

        @MavenProperty(key = "version", groupId = "commons-beanutils", artifactId = "blah")
        private String pomVersion;

        public String getPomVersion() { return pomVersion; }
        public void setPomVersion(String pomVersion) { this.pomVersion = pomVersion; }
    }
}
