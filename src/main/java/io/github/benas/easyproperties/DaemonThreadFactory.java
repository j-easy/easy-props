package io.github.benas.easyproperties;

import java.util.concurrent.ThreadFactory;

/**
 * Factory that creates daemon threads.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
class DaemonThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    }

    public static DaemonThreadFactory newDaemonThreadFactory() {
        return new DaemonThreadFactory();
    }
}
