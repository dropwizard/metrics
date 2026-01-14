package com.codahale.metrics.jackson3;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.Version;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleSerializers;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MetricsModule extends JacksonModule {
    static final Version VERSION = new Version(4, 0, 0, "", "io.dropwizard.metrics", "metrics-json");

    @SuppressWarnings("rawtypes")
    private static class GaugeSerializer extends StdSerializer<Gauge> {

        private static final long serialVersionUID = 1L;

        private GaugeSerializer() {
            super(Gauge.class);
        }

        @Override
        public void serialize(Gauge gauge,
                              JsonGenerator json,
                              SerializationContext provider) {
            json.writeStartObject();
            final Object value;
            try {
                value = gauge.getValue();
                json.writePOJOProperty("value", value);
            } catch (RuntimeException e) {
                json.writePOJOProperty("error", e.toString());
            }
            json.writeEndObject();
        }
    }

    private static class CounterSerializer extends StdSerializer<Counter> {

        private static final long serialVersionUID = 1L;

        private CounterSerializer() {
            super(Counter.class);
        }

        @Override
        public void serialize(Counter counter,
                              JsonGenerator json,
                              SerializationContext provider) {
            json.writeStartObject();
            json.writeNumberProperty("count", counter.getCount());
            json.writeEndObject();
        }
    }

    private static class HistogramSerializer extends StdSerializer<Histogram> {

        private static final long serialVersionUID = 1L;

        private final boolean showSamples;

        private HistogramSerializer(boolean showSamples) {
            super(Histogram.class);
            this.showSamples = showSamples;
        }

        @Override
        public void serialize(Histogram histogram,
                              JsonGenerator json,
                              SerializationContext provider) {
            json.writeStartObject();
            final Snapshot snapshot = histogram.getSnapshot();
            json.writeNumberProperty("count", histogram.getCount());
            json.writeNumberProperty("max", snapshot.getMax());
            json.writeNumberProperty("mean", snapshot.getMean());
            json.writeNumberProperty("min", snapshot.getMin());
            json.writeNumberProperty("p50", snapshot.getMedian());
            json.writeNumberProperty("p75", snapshot.get75thPercentile());
            json.writeNumberProperty("p95", snapshot.get95thPercentile());
            json.writeNumberProperty("p98", snapshot.get98thPercentile());
            json.writeNumberProperty("p99", snapshot.get99thPercentile());
            json.writeNumberProperty("p999", snapshot.get999thPercentile());

            if (showSamples) {
                json.writePOJOProperty("values", snapshot.getValues());
            }

            json.writeNumberProperty("stddev", snapshot.getStdDev());
            json.writeEndObject();
        }
    }

    private static class MeterSerializer extends StdSerializer<Meter> {

        private static final long serialVersionUID = 1L;

        private final String rateUnit;
        private final double rateFactor;

        public MeterSerializer(TimeUnit rateUnit) {
            super(Meter.class);
            this.rateFactor = rateUnit.toSeconds(1);
            this.rateUnit = calculateRateUnit(rateUnit, "events");
        }

        @Override
        public void serialize(Meter meter,
                              JsonGenerator json,
                              SerializationContext provider) {
            json.writeStartObject();
            json.writeNumberProperty("count", meter.getCount());
            json.writeNumberProperty("m15_rate", meter.getFifteenMinuteRate() * rateFactor);
            json.writeNumberProperty("m1_rate", meter.getOneMinuteRate() * rateFactor);
            json.writeNumberProperty("m5_rate", meter.getFiveMinuteRate() * rateFactor);
            json.writeNumberProperty("mean_rate", meter.getMeanRate() * rateFactor);
            json.writeStringProperty("units", rateUnit);
            json.writeEndObject();
        }
    }

    private static class TimerSerializer extends StdSerializer<Timer> {

        private static final long serialVersionUID = 1L;

        private final String rateUnit;
        private final double rateFactor;
        private final String durationUnit;
        private final double durationFactor;
        private final boolean showSamples;

        private TimerSerializer(TimeUnit rateUnit,
                                TimeUnit durationUnit,
                                boolean showSamples) {
            super(Timer.class);
            this.rateUnit = calculateRateUnit(rateUnit, "calls");
            this.rateFactor = rateUnit.toSeconds(1);
            this.durationUnit = durationUnit.toString().toLowerCase(Locale.US);
            this.durationFactor = 1.0 / durationUnit.toNanos(1);
            this.showSamples = showSamples;
        }

        @Override
        public void serialize(Timer timer,
                              JsonGenerator json,
                              SerializationContext provider) {
            json.writeStartObject();
            final Snapshot snapshot = timer.getSnapshot();
            json.writeNumberProperty("count", timer.getCount());
            json.writeNumberProperty("max", snapshot.getMax() * durationFactor);
            json.writeNumberProperty("mean", snapshot.getMean() * durationFactor);
            json.writeNumberProperty("min", snapshot.getMin() * durationFactor);

            json.writeNumberProperty("p50", snapshot.getMedian() * durationFactor);
            json.writeNumberProperty("p75", snapshot.get75thPercentile() * durationFactor);
            json.writeNumberProperty("p95", snapshot.get95thPercentile() * durationFactor);
            json.writeNumberProperty("p98", snapshot.get98thPercentile() * durationFactor);
            json.writeNumberProperty("p99", snapshot.get99thPercentile() * durationFactor);
            json.writeNumberProperty("p999", snapshot.get999thPercentile() * durationFactor);

            if (showSamples) {
                final long[] values = snapshot.getValues();
                final double[] scaledValues = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    scaledValues[i] = values[i] * durationFactor;
                }
                json.writePOJOProperty("values", scaledValues);
            }

            json.writeNumberProperty("stddev", snapshot.getStdDev() * durationFactor);
            json.writeNumberProperty("m15_rate", timer.getFifteenMinuteRate() * rateFactor);
            json.writeNumberProperty("m1_rate", timer.getOneMinuteRate() * rateFactor);
            json.writeNumberProperty("m5_rate", timer.getFiveMinuteRate() * rateFactor);
            json.writeNumberProperty("mean_rate", timer.getMeanRate() * rateFactor);
            json.writeStringProperty("duration_units", durationUnit);
            json.writeStringProperty("rate_units", rateUnit);
            json.writeEndObject();
        }
    }

    private static class MetricRegistrySerializer extends StdSerializer<MetricRegistry> {

        private static final long serialVersionUID = 1L;

        private final MetricFilter filter;

        private MetricRegistrySerializer(MetricFilter filter) {
            super(MetricRegistry.class);
            this.filter = filter;
        }

        @Override
        public void serialize(MetricRegistry registry,
                              JsonGenerator json,
                              SerializationContext provider) {
            json.writeStartObject();
            json.writeStringProperty("version", VERSION.toString());
            json.writePOJOProperty("gauges", registry.getGauges(filter));
            json.writePOJOProperty("counters", registry.getCounters(filter));
            json.writePOJOProperty("histograms", registry.getHistograms(filter));
            json.writePOJOProperty("meters", registry.getMeters(filter));
            json.writePOJOProperty("timers", registry.getTimers(filter));
            json.writeEndObject();
        }
    }

    protected final TimeUnit rateUnit;
    protected final TimeUnit durationUnit;
    protected final boolean showSamples;
    protected final MetricFilter filter;

    public MetricsModule(TimeUnit rateUnit, TimeUnit durationUnit, boolean showSamples) {
        this(rateUnit, durationUnit, showSamples, MetricFilter.ALL);
    }

    public MetricsModule(TimeUnit rateUnit, TimeUnit durationUnit, boolean showSamples, MetricFilter filter) {
        this.rateUnit = rateUnit;
        this.durationUnit = durationUnit;
        this.showSamples = showSamples;
        this.filter = filter;
    }

    @Override
    public String getModuleName() {
        return "metrics";
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new SimpleSerializers(Arrays.asList(
                new GaugeSerializer(),
                new CounterSerializer(),
                new HistogramSerializer(showSamples),
                new MeterSerializer(rateUnit),
                new TimerSerializer(rateUnit, durationUnit, showSamples),
                new MetricRegistrySerializer(filter)
        )));
    }

    private static String calculateRateUnit(TimeUnit unit, String name) {
        final String s = unit.toString().toLowerCase(Locale.US);
        return name + '/' + s.substring(0, s.length() - 1);
    }
}
