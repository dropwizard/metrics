package com.yammer.metrics.librato;

import com.ning.http.client.*;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * User: mihasya
 * Date: 6/14/12
 * Time: 1:08 PM
 * A reporter for publishing metrics to <a href="http://metrics.librato.com/">Librato Metrics</a>
 */
public class LibratoReporter extends AbstractPollingReporter implements MetricProcessor<MetricsLibratoBatch> {
    private final String source;

    private final Realm authRealm;
    private final String apiUrl;
    private final long timeout;
    private final TimeUnit timeoutUnit;

    private final LibratoUtil.Sanitizer sanitizer;

    protected final MetricsRegistry registry;
    protected final MetricPredicate predicate;
    protected final Clock clock;
    protected final VirtualMachineMetrics vm;
    protected final boolean reportVmMetrics;

    private final AsyncHttpClient httpClient = new AsyncHttpClient();

    private static final LibratoUtil util = new LibratoUtil();

    private static final Logger LOG = LoggerFactory.getLogger(LibratoReporter.class);

    /**
     * private to prevent someone from accidentally actually using this constructor. see .builder()
     */
    private LibratoReporter(Realm authRealm, String apiUrl, String name, final LibratoUtil.Sanitizer customSanitizer,
                            String source, long timeout, TimeUnit timeoutUnit, MetricsRegistry registry,
                            MetricPredicate predicate, Clock clock, VirtualMachineMetrics vm, boolean reportVmMetrics) {
        super(registry, name);
        this.authRealm = authRealm;
        this.sanitizer = new LibratoUtil.Sanitizer() {
            @Override
            public String apply(String name) {
                return LibratoUtil.lastPassSanitizer.apply(customSanitizer.apply(name));
            }
        };
        this.apiUrl = apiUrl;
        this.source = source;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.registry = registry;
        this.predicate = predicate;
        this.clock = clock;
        this.vm = vm;
        this.reportVmMetrics = reportVmMetrics;
    }

    @Override
    public void run() {
        // accumulate all the metrics in the batch, then post it allowing the LibratoBatch class to break up the work
        MetricsLibratoBatch batch = new MetricsLibratoBatch(LibratoBatch.DEFAULT_BATCH_SIZE, timeout, timeoutUnit);
        if (reportVmMetrics) {
            reportVmMetrics(batch);
        }
        reportRegularMetrics(batch);
        AsyncHttpClient.BoundRequestBuilder builder = httpClient.preparePost(apiUrl);
        builder.addHeader("Content-Type", "application/json");
        builder.setRealm(authRealm);

        try {
            batch.post(builder, source, TimeUnit.MILLISECONDS.toSeconds(Clock.defaultClock().getTime()));
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("Librato post failed: ", e);
        }
    }

    protected void reportVmMetrics(MetricsLibratoBatch batch) {
        util.addVmMetricsToBatch(vm, batch);
    }

    protected void reportRegularMetrics(MetricsLibratoBatch batch) {
        for (Map.Entry<String,SortedMap<MetricName,Metric>> entry :
                getMetricsRegistry().getGroupedMetrics(predicate).entrySet()) {

            for (Map.Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
                final Metric metric = subEntry.getValue();
                if (metric != null) {
                    try {
                        metric.processWith(this, subEntry.getKey(), batch);
                    } catch (Exception e) {
                        LOG.error("Error reporting regular metrics:", e);
                    }
                }
            }
        }
    }

    private String getStringName(MetricName fullName) {
        return sanitizer.apply(util.nameToString(fullName));
    }

    @Override
    public void processMeter(MetricName name, Metered meter, MetricsLibratoBatch batch) throws Exception {
        batch.addMetered(getStringName(name), meter);
    }

    @Override
    public void processCounter(MetricName name, Counter counter, MetricsLibratoBatch batch) throws Exception {
         batch.addCounterMeasurement(getStringName(name), counter.getCount());
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, MetricsLibratoBatch batch) throws Exception {
        String sanitizedName = getStringName(name);
        batch.addSummarizable(sanitizedName, histogram);
        batch.addSampling(sanitizedName, histogram);
    }

    @Override
    public void processTimer(MetricName name, Timer timer, MetricsLibratoBatch batch) throws Exception {
        String sanitizedName = getStringName(name);
        batch.addMetered(sanitizedName, timer);
        batch.addSummarizable(sanitizedName, timer);
        batch.addSampling(sanitizedName, timer);
    }

    @Override
    public void processGauge(MetricName name, Gauge<?> gauge, MetricsLibratoBatch batch) throws Exception {
        if (gauge.getValue() instanceof Number) {
            batch.addGauge(util.nameToString(name), gauge);
        }
    }

    /**
     * a builder for the LibratoReporter class that requires things that cannot be inferred and uses
     * sane default values for everything else.
     */
    public static class Builder {
        private final String username;
        private final String token;
        private final String source;

        private String apiUrl = "https://metrics-api.librato.com/v1/metrics";

        private LibratoUtil.Sanitizer sanitizer = LibratoUtil.noopSanitizer;

        private long timeout = 5;
        private TimeUnit timeoutUnit = TimeUnit.SECONDS;

        private String name = "librato-reporter";
        private MetricsRegistry registry = Metrics.defaultRegistry();
        private MetricPredicate predicate = MetricPredicate.ALL;
        private Clock clock = Clock.defaultClock();
        private VirtualMachineMetrics vm = VirtualMachineMetrics.getInstance();
        private boolean reportVmMetrics = true;

        public Builder(String username, String token, String source) {
            this.username = username;
            this.token = token;
            this.source = source;
        }

        public Builder setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        public Builder setTimeout(long timeout, TimeUnit timeoutUnit) {
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSanitizer(LibratoUtil.Sanitizer sanitizer) {
            this.sanitizer = sanitizer;
            return this;
        }

        public Builder setRegistry(MetricsRegistry registry) {
            this.registry = registry;
            return this;
        }

        public Builder setPredicate(MetricPredicate predicate) {
            this.predicate = predicate;
            return this;
        }

        public Builder setClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder setVm(VirtualMachineMetrics vm) {
            this.vm = vm;
            return this;
        }

        public Builder setReportVmMetrics(boolean reportVmMetrics) {
            this.reportVmMetrics = reportVmMetrics;
            return this;
        }

        public LibratoReporter build() {
            return new LibratoReporter(new Realm.RealmBuilder().setPrincipal(username).setPassword(token).build(),
                    apiUrl, name, sanitizer, source, timeout, timeoutUnit,
                    registry, predicate, clock, vm, reportVmMetrics);
        }
    }

    /**
     * convenience method for creating a Builder
     */
    public static Builder builder(String username, String token, String source) {
        return new Builder(username, token, source);
    }

    /**
     * @param builder a LibratoReporter.Builder
     * @param interval the interval at which the metrics are to be reporter
     * @param unit the timeunit for interval
     *
     * the timeout is set to be almost as long as the interval, thus allowing excessive time for publishing in most
     * cases without causing a hung http request to prevent further metrics from being published
     */
    public static void enable(Builder builder, long interval, TimeUnit unit) {
        builder.setTimeout((long)Math.floor(interval * .9), unit).build().start(interval, unit);
    }
}
