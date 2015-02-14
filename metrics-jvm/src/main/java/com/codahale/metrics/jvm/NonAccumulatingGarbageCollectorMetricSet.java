package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This metric set class alters the behavior of the {@link GarbageCollectorMetricSet},
 * which takes readings of GC counts and times that are cumulative over the life of the JVM.
 * This metric set uses gauges that represent data for a specific time interval, rather than all time.
 * It also adds an extra gauge for GC throughput, which is a convenient summary metric for JVM's.
 *
 * To calculate readings for a specific time interval, this metric set needs to maintain state
 * for previous values and non-accumulated values. To do it in a thread safe way, this metric set
 * runs a background process in a scheduled thread that updates and stores gauge readings at
 * user-specified time intervals. When clients call {@link com.codahale.metrics.Gauge#getValue()},
 * the stored readings are read (not modified), rather than calling the underlying gauges directly.
 */
public class NonAccumulatingGarbageCollectorMetricSet implements MetricSet {
    private static final Logger LOG = LoggerFactory.getLogger(NonAccumulatingGarbageCollectorMetricSet.class);
    public static final String GC_THROUGHPUT_METRIC_NAME = "GC-throughput.percent";

    private Map<String, Long> previousValues;
    private Map<String, Long> nonAccumulatingValues;
    private GarbageCollectorMetricSet garbageCollectorMetricSet;
    private ScheduledExecutorService scheduledExecutorService;
    private long interval;

    /**
     * Constructor does not take an executor service, instead deferring
     * to the default scheduled executor service provided by this class.
     *
     * @param garbageCollectorMetricSet a metric set that collects counts and times of garbage collections
     * @param interval the time interval over which to calculate non-accumulating gauge readings
     *                 for all the gauges in {@code garbageCollectorMetricSet}
     */
    public NonAccumulatingGarbageCollectorMetricSet(
            GarbageCollectorMetricSet garbageCollectorMetricSet, long interval) {
        this(garbageCollectorMetricSet, interval, null);
    }

    /**
     * Constructor sets up the scheduled executor service that runs a background task to
     * calculate non-accumulating gauge readings at periodic intervals.
     *
     * @param garbageCollectorMetricSet a metric set that collects counts and times of garbage collections
     * @param interval the time interval over which to calculate non-accumulating gauge readings
     *                 for all the gauges in {@code garbageCollectorMetricSet}
     * @param scheduledExecutorService scheduled executor service that runs the task to calculate
     *                                 non-accumulating gauge readings at a frequency determined by
     *                                 {@code interval}.
     */
    public NonAccumulatingGarbageCollectorMetricSet(
            GarbageCollectorMetricSet garbageCollectorMetricSet, long interval,
            ScheduledExecutorService scheduledExecutorService) {
        this.garbageCollectorMetricSet = garbageCollectorMetricSet;
        this.interval = interval;
        previousValues = new HashMap<String, Long>();
        nonAccumulatingValues = new ConcurrentHashMap<String, Long>();
        if (scheduledExecutorService == null) {
            BasicThreadFactory basicThreadFactory = new BasicThreadFactory.Builder()
                    .namingPattern("metrics-gc-stats-update-%d")
                    .daemon(false)
                    .priority(Thread.NORM_PRIORITY)
                    .build();
            this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(basicThreadFactory);
        } else {
            this.scheduledExecutorService = scheduledExecutorService;
        }
        scheduleBackgroundCollectionOfNonAccumulatingValues();
    }

    /**
     * Initialize the scheduled executor service that runs the task to calculate
     * non-accumulating gauge readings.
     */
    protected void scheduleBackgroundCollectionOfNonAccumulatingValues() {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    getMetricsOnSchedule();
                } catch (RuntimeException ex) {
                    LOG.error("RuntimeException thrown in NonAccumulatingGarbageCollectorMetricSet. Exception was suppressed.");
                }
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * Background process to harvest readings from the {@code garbageCollectorMetricSet},
     * which are cumulative over all time. The process updates two maps: one that stores
     * the previous values of all gauges, and one that stores the non-accumulating values
     * for those gauges (the difference between the last reading and the current reading).
     */
    private void getMetricsOnSchedule() {
        Map<String, Metric> metricMap = garbageCollectorMetricSet.getMetrics();
        for (Map.Entry<String, Metric> metricEntry : metricMap.entrySet()) {
            Gauge currentGauge = (Gauge) metricEntry.getValue();
            String currentKey = metricEntry.getKey();
            Long currentValue = (Long) currentGauge.getValue();

            // first reading of gauges will have no previous value. Simply store the current readings.
            if (previousValues.get(currentKey) == null) {
                previousValues.put(currentKey, currentValue);
                nonAccumulatingValues.put(currentKey, currentValue);
            } else {
                // subtract the previous gauge readings from the current readings, and store result
                Long difference = currentValue - previousValues.get(currentKey);
                nonAccumulatingValues.put(currentKey, difference);
                previousValues.put(currentKey, currentValue);
            }
        }
    }

    /**
     * Creates a map of non-accumulating gauges for the underlying gauges in {@code garbageCollectorMetricSet}
     * Also, add an additional gauge for GC throughput.
     *
     * @return the map of metrics that contains non-cumulative gauges
     */
    @Override
    public Map<String, Metric> getMetrics() {
        Map<String, Metric> metricMap = garbageCollectorMetricSet.getMetrics();
        Map<String, Metric> nonAccumulatingMetricMap = new HashMap<String, Metric>();

        for (final Map.Entry<String, Metric> metricEntry : metricMap.entrySet()) {
            Gauge nonAccumulatingGauge = new Gauge<Long>() {
                /**
                 * Instead of reading from the accumulating gauges in {@code garbageCollectorMetricSet},
                 * we read from the map of non-accumulating values (which is updated by the background
                 * task running in {@code scheduledExecutorService}).
                 *
                 * For reporters that call this method, it makes sense to align the frequency of
                 * those calls with the frequency of the background updates of non-accumulating
                 * gauges. For example, if a reporter calls this method every minute, then the
                 * {@code interval} setting of this metric-set should also be 1 minute. Calling
                 * this method more or less frequently does not impact the operation of this
                 * class--but the caller will either get repeated values (for more frequent calls),
                 * or will miss some values (for less frequent calls).
                 *
                 * @return the gauge value representing only the current reporting interval
                 */
                @Override
                public Long getValue() {
                    Long value = nonAccumulatingValues.get(metricEntry.getKey());
                    return value != null ? value : 0L;
                }
            };
            nonAccumulatingMetricMap.put(metricEntry.getKey(), nonAccumulatingGauge);
        }

        Gauge gcThroughputGauge = new Gauge<Double>() {
            /**
             * This gauge returns readings for garbage collector throughput. GC throughput
             * is derived from the readings of time spent by all garbage collectors in
             * the current {@code interval} of time.
             *
             * Example: for an interval of 1 minute, we have 60,000 ms of total time.
             * If we spent 200 ms on minor GC's and 100 ms on major GC's, then
             * 59,700 ms were available to the application. 59,700 / 60,000 = 99.5% GC throughput.
             *
             * @return a value between 0.0 and 100.0 that represents the garbage collector
             * throughput for the current interval (as defined by {@code interval}
             */
            @Override
            public Double getValue() {
                Double totalGCTime = 0.0;

                for (Map.Entry<String, Long> metricEntry : nonAccumulatingValues.entrySet()) {
                    if (metricEntry.getKey().endsWith("time")) {
                        totalGCTime += metricEntry.getValue();
                    }
                }

                return 100 - ((totalGCTime / interval) * 100);
            }
        };
        nonAccumulatingMetricMap.put(GC_THROUGHPUT_METRIC_NAME, gcThroughputGauge);

        return Collections.unmodifiableMap(nonAccumulatingMetricMap);
    }
}
