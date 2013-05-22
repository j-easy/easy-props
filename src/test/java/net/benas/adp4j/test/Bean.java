package net.benas.adp4j.test;

import net.benas.adp4j.annotations.I18NProperty;
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

    @SystemProperty("threshold")
    private int threshold;

    @Property(source = "myProperties.properties", key = "bean.name")
    private String beanName;

    @I18NProperty(bundle = "i18n/messages", key = "my.message")
    private String message;

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
}
