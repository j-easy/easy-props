/*
 * The MIT License
 *
 *   Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package org.jeasy.props;

import org.jeasy.props.annotations.Manageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Component responsible for registering a JMX MBean for a given object.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
class MBeanRegistrar {

    private static final String JMX_OBJECT_NAME_PREFIX = "org.jeasy.props:";
    private static final Logger LOGGER = LoggerFactory.getLogger(MBeanRegistrar.class);

    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    void registerMBeanFor(final Object object) {
        if (shouldBeManaged(object)) {
            Manageable manageable = object.getClass().getAnnotation(Manageable.class);
            String name = manageable.name().trim().isEmpty() ? object.getClass().getName() : manageable.name();
            ObjectName objectName;
            try {
                objectName = new ObjectName(JMX_OBJECT_NAME_PREFIX + "name=" + name);
                if (!mBeanServer.isRegistered(objectName)) {
                    mBeanServer.registerMBean(object, objectName);
                }
            } catch (Exception e) {
                LOGGER.error("Unable to register a JMX MBean for object '" + object + "'", e);
            }
        }
    }

    private boolean shouldBeManaged(Object object) {
        return object.getClass().isAnnotationPresent(Manageable.class);
    }

}
