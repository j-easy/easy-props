package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.annotations.SystemProperty;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemPropertyAnnotationProcessorTest extends AbstractAnnotationProcessorTest {

    private Bean bean;

    @Before
    public void setUp() throws Exception {
        System.setProperty("threshold", "30");
        super.setUp();
    }

    @Test
    public void testSystemPropertyInjection() {
        //given
        bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getUserHome()).isEqualTo(System.getProperty("user.home"));
    }

    @Test
    public void testSystemPropertyDefaultValueInjection() {
        //given
        bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getValue()).isEqualTo("default");
    }

    @Test
    public void testSystemPropertyInjectionWithTypeConversion() {
        //given
        bean = new Bean();

        //when
        propertiesInjector.injectProperties(bean);

        //then
        assertThat(bean.getThreshold()).isEqualTo(30);
    }

    public class Bean {

        @SystemProperty("user.home")
        private String userHome;

        @SystemProperty(value = "blah", defaultValue = "default")
        private String value;

        @SystemProperty("threshold")
        private int threshold;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getUserHome() {
            return userHome;
        }

        public void setUserHome(String userHome) {
            this.userHome = userHome;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }
    }

}
