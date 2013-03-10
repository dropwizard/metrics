package com.yammer.metrics.jvm;

import com.yammer.metrics.Gauge;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThreadStatesGaugeMap {
    private final ThreadMXBean threads;

    public ThreadStatesGaugeMap(ThreadMXBean threads) {
        this.threads = threads;
    }

    public Map<String, Gauge<?>> getGauges() {
        final Map<String, Gauge<?>> gauges = new HashMap<String, Gauge<?>>();
        for (final Thread.State state : Thread.State.values()) {
            gauges.put("jvm.thread-states." + state.toString().toLowerCase(),
                       new Gauge<Object>() {
                           @Override
                           public Object getValue() {
                               return getThreadCount(state);
                           }
                       });
        }
        return Collections.unmodifiableMap(gauges);
    }

    private int getThreadCount(Thread.State state) {
        final ThreadInfo[] allThreads = threads.getThreadInfo(threads.getAllThreadIds());
        int count = 0;
        for (ThreadInfo info : allThreads) {
            if (info != null) {
                if (info.getThreadState() == state) {
                    count++;
                }
            }
        }

        return count;
    }
}
