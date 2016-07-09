package io.github.benas.easyproperties;

import io.github.benas.easyproperties.annotations.HotReload;
import io.github.benas.easyproperties.api.PropertiesInjector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.benas.easyproperties.DaemonThreadFactory.newDaemonThreadFactory;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Component responsible for registering hot reloading tasks for a given object.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
class HotReloadingRegistrar {

    private Map<Object, Runnable> hotReloadingTasks = new HashMap<>();
    private ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor(newDaemonThreadFactory());

    public void registerHotReloadingTask(final PropertiesInjector propertiesInjector, final Object target) {
        if (shouldBeHotReloaded(target)) {
            HotReload hotReload = target.getClass().getAnnotation(HotReload.class);
            long period = hotReload.period();
            TimeUnit unit = hotReload.unit();
            PropertiesInjectionTask propertiesInjectionTask = new PropertiesInjectionTask(propertiesInjector, target);
            scheduledExecutorService.scheduleAtFixedRate(propertiesInjectionTask, 0, period, unit);
            hotReloadingTasks.put(target, propertiesInjectionTask);
        }
    }

    private boolean shouldBeHotReloaded(final Object target) {
        return target.getClass().isAnnotationPresent(HotReload.class) && !hotReloadingTasks.containsKey(target);
    }

}
