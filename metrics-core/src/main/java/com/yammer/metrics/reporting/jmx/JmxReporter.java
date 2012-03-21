/*
 * Copyright (c) 2012. Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.yammer.metrics.reporting.jmx;

import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.AbstractReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: gorzell
 * Date: 3/16/12
 * Time: 1:27 PM
 */
public class JmxReporter extends AbstractReporter implements MetricsRegistryListener,
        MetricProcessor<JmxReporter.Context> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmxReporter.class);

    private final Map<MetricName, ObjectName> registeredBeans;
    private final MBeanServer server;

    static final class Context {
        private final MetricName metricName;
        private final MetricsRegistry registry;

        public Context(final MetricName metricName, final MetricsRegistry registry) {
            this.metricName = metricName;
            this.registry = registry;
        }

        MetricName getMetricName() {
            return metricName;
        }

        MetricsRegistry getRegistry(){
            return registry;
        }
    }

    /**
     * Creates a new {@link JmxReporter} for the given registry.
     *
     * @param registry a {@link com.yammer.metrics.core.MetricsRegistry}
     */
    public JmxReporter(MetricsRegistry registry) {
        super(registry);
        this.registeredBeans = new ConcurrentHashMap<MetricName, ObjectName>(100);
        this.server = ManagementFactory.getPlatformMBeanServer();
        
        for(Map.Entry<MetricName, Metric> metric : registry.allMetrics().entrySet()){
            onMetricAdded(registry, metric.getKey(), metric.getValue());
        }
    }

    @Override
    public void onMetricAdded(MetricsRegistry registry, MetricName name, Metric metric) {
        if (metric != null) {
            try {
                metric.processWith(this, name, new Context(name, registry));
            } catch (Exception e) {
                LOGGER.warn("Error processing {}", name, e);
            }
        }
    }

    @Override
    public void onMetricRemoved(MetricsRegistry registry, MetricName name) {
        unregisterBean(name);
    }

    @Override
    public void processMeter(MetricName name, Metered meter, Context context) throws Exception {
        registerBean(context.getRegistry().getName(), name, meter);
    }

    @Override
    public void processCounter(MetricName name, Counter counter, Context context) throws Exception {
        registerBean(context.getRegistry().getName(), name, counter);
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, Context context) throws Exception {
        registerBean(context.getRegistry().getName(), name, histogram);
    }

    @Override
    public void processTimer(MetricName name, Timer timer, Context context) throws Exception {
        registerBean(context.getRegistry().getName(), name, timer);
    }

    @Override
    public void processGauge(MetricName name, Gauge<?> gauge, Context context) throws Exception {
        registerBean(context.getRegistry().getName(), name, gauge);
    }

    @Override
    public void shutdown() {
        getMetricsRegistry().removeListener(this);
        for (MetricName name : registeredBeans.keySet()) {
            unregisterBean(name);
        }
        registeredBeans.clear();
    }

    /**
     * Starts the reporter.
     */
    public final void start() {
        getMetricsRegistry().addListener(this);
    }

    private void registerBean(String registryName, MetricName name, Metric metric)
            throws MBeanRegistrationException, OperationsException {
        try{
            JmxMetric jmxMetric = new JmxMetric(registryName, name, metric);
            server.registerMBean(jmxMetric, jmxMetric.getObjectName());
            registeredBeans.put(name, jmxMetric.getObjectName());
        } catch (Exception e){
            throw new MBeanRegistrationException(e, "Problem creating mBean.");
        }
    }

    private void unregisterBean(MetricName name) {
        final ObjectName objectName = registeredBeans.remove(name);

        if (objectName != null) {
            try {
                server.unregisterMBean(objectName);
            } catch (InstanceNotFoundException e) {
                // This is often thrown when the process is shutting down. An application with lots of
                // metrics will often begin unregistering metrics *after* JMX itself has cleared,
                // resulting in a huge dump of exceptions as the process is exiting.
                LOGGER.trace("Error unregistering {}", name, e);
            } catch (MBeanRegistrationException e) {
                LOGGER.debug("Error unregistering {}", name, e);
            }
        }
    }
}
