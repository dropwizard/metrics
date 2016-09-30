package com.codahale.metrics.jcache;

import com.codahale.metrics.JmxAttributeGauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import javax.cache.management.CacheStatisticsMXBean;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author <a href="mailto:henri.tremblay@softwareag.com">Henri Tremblay</a>
 * @author <a href="mailto:anthony.dahanne@softwareag.com">Anthony Dahanne</a>
 */
public class JCacheGaugeSet implements MetricSet {

    private static final String M_BEAN_COORDINATES = "javax.cache:type=CacheStatistics,CacheManager=*,Cache=*";

    private Set<ObjectInstance> objectInstances;

    public JCacheGaugeSet() {
        try {
            this.objectInstances = ManagementFactory.getPlatformMBeanServer().queryMBeans(ObjectName.getInstance(M_BEAN_COORDINATES), null);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();

        List<String> availableStatsNames = retrieveStatsNames();

        for (ObjectInstance objectInstance : objectInstances) {
            ObjectName objectName = objectInstance.getObjectName();
            String cacheName = objectName.getKeyProperty("Cache");

            for (String statsName : availableStatsNames) {
                JmxAttributeGauge jmxAttributeGauge = new JmxAttributeGauge(objectName, statsName);
                gauges.put(name(cacheName, toDashCase(statsName)), jmxAttributeGauge);
            }
        }

        return Collections.unmodifiableMap(gauges);
    }

    private List<String> retrieveStatsNames() {
        List<String> availableStatsNames = new ArrayList<String>();
        Class c = CacheStatisticsMXBean.class;
        for (Method method : c.getDeclaredMethods()) {
            String methodName = method.getName();
            if(methodName.startsWith("get")) {
                availableStatsNames.add(methodName.substring(3,methodName.length()));
            }
        }
        return availableStatsNames;
    }

    private String toDashCase(String camelCase) {
        return camelCase.replaceAll("(.)(\\p{Upper})", "$1-$2").toLowerCase();
    }


}
