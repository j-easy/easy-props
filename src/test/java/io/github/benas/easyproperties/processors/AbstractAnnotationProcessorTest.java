package io.github.benas.easyproperties.processors;

import io.github.benas.easyproperties.api.PropertiesInjector;
import org.junit.Before;

import static io.github.benas.easyproperties.impl.PropertiesInjectorBuilder.aNewPropertiesInjector;

public abstract class AbstractAnnotationProcessorTest {

    protected PropertiesInjector propertiesInjector;

    @Before
    public void setUp() throws Exception {
        propertiesInjector = aNewPropertiesInjector().build();
    }

}
