package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.annotations.I18NProperty;
import io.github.benas.easyproperties.api.PropertyInjectionException;
import org.junit.Before;
import org.junit.Test;

import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;

public class I18NPropertyAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    private ResourceBundle resourceBundle;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        resourceBundle = ResourceBundle.getBundle("i18n/messages");
    }

    @Test
    public void testI18NPropertyInjection() throws Exception {
        //given
        Bean bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getMessage()).isEqualTo(resourceBundle.getString("my.message"));
    }

    @Test(expected = PropertyInjectionException.class)
    public void whenBundleIsMissing_thenShouldThrowAnException() throws Exception {
        //given
        BeanWithInvalidBundle bean = new BeanWithInvalidBundle();

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
        assertThat(bean.getMessage()).isNull();
    }

    public class Bean {

        @I18NProperty(bundle = "i18n/messages", key = "my.message")
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public class BeanWithInvalidBundle {

        @I18NProperty(bundle = "blah", key = "my.message")
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public class BeanWithInvalidKey {

        @I18NProperty(bundle = "i18n/messages", key = "blah")
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
