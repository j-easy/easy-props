package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.annotations.Property;
import io.github.benas.easyproperties.api.PropertyInjectionException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    @Test
    public void testPropertyInjection() {
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getBeanName()).isEqualTo("Foo");
        assertThat(bean.getBeanAge()).isEqualTo(30);
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenPropertiesFileIsInvalid_thenShouldThrowAnException() throws Exception {
        //given
        BeanWithInvalidPropertiesFile bean = new BeanWithInvalidPropertiesFile();

        //when
        propertiesInjector.injectProperties(bean);

        //then should throw an exception
    }

    @Test
    public void whenKeyIsMissing_thenShouldIgnoreField() {
        //given
        BeanWithMissingKey bean = new BeanWithMissingKey();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getUnknownField()).isNull();
    }

    @Test
    public void whenKeyIsEmpty_thenShouldIgnoreField() {
        //given
        BeanWithEmptyKey bean = new BeanWithEmptyKey();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getEmptyField()).isNull();
    }

    public class Bean {

        @Property(source = "classpath:myProperties.properties", key = "bean.name")
        private String beanName;

        @Property(source = "file:src/test/resources/fileSystemProperties.properties", key = "bean.age")
        private int beanAge;

        public String getBeanName() { return beanName; }
        public void setBeanName(String beanName) { this.beanName = beanName; }

        public int getBeanAge() {
            return beanAge;
        }

        public void setBeanAge(int beanAge) {
            this.beanAge = beanAge;
        }
    }

    public class BeanWithInvalidPropertiesFile {

        @Property(source = "blah.properties", key = "bean.name")
        private String beanName;

        public String getBeanName() { return beanName; }
        public void setBeanName(String beanName) { this.beanName = beanName; }
    }

    public class BeanWithMissingKey {

        @Property(source = "myProperties.properties", key = "unknown.key")
        private String unknownField;

        public String getUnknownField() {
            return unknownField;
        }
        public void setUnknownField(String unknownField) {
            this.unknownField = unknownField;
        }
    }

    public class BeanWithEmptyKey {

        @Property(source = "myProperties.properties", key = "empty.key")
        private String emptyField;

        public String getEmptyField() { return emptyField; }
        public void setEmptyField(String emptyField) { this.emptyField = emptyField; }
    }

}
