package com.codahale.metrics.jvm;

import static com.codahale.metrics.MetricRegistry.name;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.NoCopyGauge;

/**
 * A set of gauges for the counts and elapsed times of garbage collections.
 */
public class GarbageCollectorMetricSet implements MetricSet {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private final List<GarbageCollectorMXBean> garbageCollectors;

    /**
     * Creates a new set of gauges for all discoverable garbage collectors.
     */
    public GarbageCollectorMetricSet() {
        this(ManagementFactory.getGarbageCollectorMXBeans());
    }

    /**
     * Creates a new set of gauges for the given collection of garbage collectors.
     *
     * @param garbageCollectors    the garbage collectors
     */
    public GarbageCollectorMetricSet(Collection<GarbageCollectorMXBean> garbageCollectors) {
        this.garbageCollectors = new ArrayList<GarbageCollectorMXBean>(garbageCollectors);
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();
        for (final GarbageCollectorMXBean gc : garbageCollectors) {
            final String name = WHITESPACE.matcher(gc.getName()).replaceAll("-");
            gauges.put(name(name, "count"), new NoCopyGauge<Long> () {
                @Override
                public Long getValue() {
                    return gc.getCollectionCount();
                }
            });

            gauges.put(name(name, "time"), new NoCopyGauge<Long>() {
                @Override
                public Long getValue() {
                    return gc.getCollectionTime();
                }
            });
        }
        return Collections.unmodifiableMap(gauges);
    }
    private void writeObject(ObjectOutputStream o)
        throws IOException {  
        
      }
      
      private void readObject(ObjectInputStream o)
        throws IOException, ClassNotFoundException {  
        
      }

}
