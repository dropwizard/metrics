package com.yammer.metrics.core;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * <p>Unbelievably, I had to write this damn class to reach deep into the guts of {@link Logger} and fix a memory leak
 * in the way it handles references to logger instances in Java 1.6.0_24 which get garbage collected. Come and gather
 * all around me and listen to my tale of woe.</p>
 *
 * <p>The {@link VirtualMachineMetrics.GcMonitor} class does rather a lot of JMX-bothering on a regular basis. Every five
 * seconds, it gets GC information via JMX and calculates information about it. That is good. We like that. But as it
 * does so, JMX does all sorts of tracing behind the scenes from
 * {@link com.sun.jmx.mbeanserver.JmxMBeanServer#getAttribute(javax.management.ObjectName, String)}
 * to {@link com.sun.jmx.interceptor.DefaultMBeanServerInterceptor#getAttribute(javax.management.ObjectName, String)}
 * to {@link com.sun.jmx.interceptor.DefaultMBeanServerInterceptor#isTraceOn()}
 * to {@link com.sun.jmx.trace.TraceManager#isSelected(int, int)}
 * to {@link com.sun.jmx.trace.TraceManager#getLogger(int)} and finally to
 * {@link Logger#getLogger(String)}.</p>
 *
 * <p>All of that hoo-hah calls {@link Logger#setParent(java.util.logging.Logger)} rather a lot, which itself calls a
 * private method {@code doSetParent}, which among other things adds a {@link WeakReference} to the current
 * {@link Logger} to an {@link ArrayList} named {@code kids} in the parrent {@link Logger}. This is all fine and good
 * except for one thing: nothing ever removes references whose referents have been garbage-collected from {@code kids}
 * </p>
 *
 * <p>And so, over time, {@code kids} gets bigger and bigger and bigger until I see something like this in the heap of one
 * of my production services:</p>
 *
 * <p><img src="http://codahale.com/logger-memory-leak.png" /></p>
 *
 * <p>Yes, that's a full 10 <i>megabytes</i> of null {@link WeakReference}s in my heap. And it's growing… slowly…
 * unboundedly… constantly. After first spending a lot of time cursing the numbnuts at Oracle who decided that was a
 * totally appropriate way of doing things, I decided to get in touch with my old Ruby roots and just get all up in the
 * guts of someone else's code using reflection.</p>
 *
 * <p>And so, here you have a {@link Runnable} which {@link VirtualMachineMetrics} runs every 10 minutes or so which
 * goes through every single goddamn {@link Logger} instance that {@link LogManager} knows about, cracks open its
 * ribcage and rummages around inside until it finds both the {@code kids} field and the {@code treeLock} object it uses
 * for synchronization. It then locks the array and goes through it and removes all null {@link WeakReference}s,
 * ensuring that your heap will not eventually be replaced by </p>
 *
 * <p>This is a thing that a grown-up had to do. Never forget that.</p>
 *
 * @author Coda Hale, Grade 3
 */
class LoggerMemoryLeakFix implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(LoggerMemoryLeakFix.class.getCanonicalName());
    private final Field kidsField, lockField;

    public LoggerMemoryLeakFix() throws NoSuchFieldException, IllegalAccessException {
        this.kidsField = Logger.class.getDeclaredField("kids");
        kidsField.setAccessible(true);
        kidsField.get(LOGGER); // ensure we can get at this field

        this.lockField = Logger.class.getDeclaredField("treeLock");
        lockField.setAccessible(true);
        lockField.get(LOGGER); // ensure we can also get at the lock
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        final Enumeration<String> names = LogManager.getLogManager().getLoggerNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final Logger logger = LogManager.getLogManager().getLogger(name);
            LOGGER.fine("Removing null kid entries from logger \"" + name + "\"");
            if (logger != null) {
                try {
                    final Object lock = lockField.get(logger);
                    if (lock == null) {
                        throw new IllegalStateException("can't find the lock on logger \"" + name + "\"");
                    }
                    int removed = 0;
                    synchronized (lock) {
                        final ArrayList<WeakReference<Object>> kids = (ArrayList<WeakReference<Object>>) kidsField.get(logger);
                        if (kids != null) {
                            for (int i = kids.size() - 1; i >= 0; i--) {
                                if (kids.get(i).get() == null) {
                                    removed++;
                                    kids.remove(i);
                                }
                            }

                            if (removed > 0) {
                                LOGGER.fine("Removed " + removed + " null WeakReference instances");
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.FINE, "Error cleaning out logger", e);
                }
            }
        }
    }
}
