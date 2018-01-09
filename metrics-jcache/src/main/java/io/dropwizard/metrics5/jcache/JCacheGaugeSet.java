package io.dropwizard.metrics5.jcache;

import io.dropwizard.metrics5.Metric;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricSet;
import io.dropwizard.metrics5.jvm.JmxAttributeGauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.dropwizard.metrics5.MetricRegistry.name;

/**
 * Gauge set retrieving JCache JMX attributes
 *
 * @author <a href="mailto:henri.tremblay@softwareag.com">Henri Tremblay</a>
 * @author <a href="mailto:anthony.dahanne@softwareag.com">Anthony Dahanne</a>
 */
public class JCacheGaugeSet implements MetricSet {

    private static final String M_BEAN_COORDINATES = "javax.cache:type=CacheStatistics,CacheManager=*,Cache=*";

    private static final Logger LOGGER = LoggerFactory.getLogger(JCacheGaugeSet.class);

    @Override
    public Map<MetricName, Metric> getMetrics() {
        Set<ObjectInstance> cacheBeans = getCacheBeans();
        List<String> availableStatsNames = retrieveStatsNames();

        Map<MetricName, Metric> gauges = new HashMap<>(cacheBeans.size() * availableStatsNames.size());

        for (ObjectInstance cacheBean : cacheBeans) {
            ObjectName objectName = cacheBean.getObjectName();
            String cacheName = objectName.getKeyProperty("Cache");

            for (String statsName : availableStatsNames) {
                JmxAttributeGauge jmxAttributeGauge = new JmxAttributeGauge(objectName, statsName);
                gauges.put(name(cacheName, toSpinalCase(statsName)), jmxAttributeGauge);
            }
        }

        return Collections.unmodifiableMap(gauges);
    }

    private Set<ObjectInstance> getCacheBeans() {
        try {
            return ManagementFactory.getPlatformMBeanServer().queryMBeans(ObjectName.getInstance(M_BEAN_COORDINATES), null);
        } catch (MalformedObjectNameException e) {
            LOGGER.error("Unable to retrieve {}. Are JCache statistics enabled?", M_BEAN_COORDINATES);
            throw new RuntimeException(e);
        }
    }

    private List<String> retrieveStatsNames() {
        Method[] methods = CacheStatisticsMXBean.class.getDeclaredMethods();
        List<String> availableStatsNames = new ArrayList<>(methods.length);

        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("get")) {
                availableStatsNames.add(methodName.substring(3));
            }
        }
        return availableStatsNames;
    }

    private static String toSpinalCase(String camelCase) {
        return camelCase.replaceAll("(.)(\\p{Upper})", "$1-$2").toLowerCase(Locale.US);
    }

}
