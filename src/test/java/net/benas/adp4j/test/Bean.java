package net.benas.adp4j.test;

import net.benas.adp4j.annotations.I18NProperty;
import net.benas.adp4j.annotations.Properties;
import net.benas.adp4j.annotations.Property;
import net.benas.adp4j.annotations.SystemProperty;

/**
 * A dummy object to be used in test.
 *
 * @author benas (md.benhassine@gmail.com)
 */
public class Bean {

    @SystemProperty("user.home")
    private String userHome;

    @SystemProperty(value = "blah", defaultValue = "default")
    private String value;

    @SystemProperty("threshold")
    private int threshold;

    @Property(source = "myProperties.properties", key = "bean.name")
    private String beanName;

    @I18NProperty(bundle = "i18n/messages", key = "my.message")
    private String message;

    @Properties("myProperties.properties")
    private java.util.Properties myProperties;

    public void setUserHome(String userHome) {
        this.userHome = userHome;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMyProperties(java.util.Properties myProperties) {
        this.myProperties = myProperties;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUserHome() {
        return userHome;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getMessage() {
        return message;
    }

    public java.util.Properties getMyProperties() {
        return myProperties;
    }
}
