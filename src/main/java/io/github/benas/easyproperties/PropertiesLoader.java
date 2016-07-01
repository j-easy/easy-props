package io.github.benas.easyproperties;

import io.github.benas.easyproperties.api.PropertiesInjector;
import io.github.benas.easyproperties.api.PropertyInjectionException;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesLoader extends TimerTask {

    private static Logger LOGGER = Logger.getLogger(PropertiesLoader.class.getName());

    private PropertiesInjector injector;

    private Object target;

    public PropertiesLoader(PropertiesInjector injector, Object target) {
        this.injector = injector;
        this.target = target;
    }

    @Override
    public void run() {
        try {
            injector.injectProperties(target);
        } catch (PropertyInjectionException e) {
            LOGGER.log(Level.SEVERE, "Unable to inject properties in object: " + target, e);
        }
    }
}
