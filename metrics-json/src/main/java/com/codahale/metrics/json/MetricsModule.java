package com.codahale.metrics.json;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MetricsModule extends Module {
    static final Version VERSION = new Version(4, 0, 0, "", "io.dropwizard.metrics", "metrics-json");

    @SuppressWarnings("rawtypes")
    private static class GaugeSerializer extends StdSerializer<Gauge> {

        private static final long serialVersionUID = 1L;
        private final Double scaleFactor;

        private GaugeSerializer(Double scaleFactor) {
            super(Gauge.class);
            this.scaleFactor = scaleFactor;
        }

        @Override
        public void serialize(Gauge gauge,
                              JsonGenerator json,
                              SerializerProvider provider) throws IOException {
            json.writeStartObject();
            final Object value;
            try {
                value = gauge.getValue();
                if (value instanceof Double) {
                    json.writeObjectField("value", round((Double) value, scaleFactor));
                } else {
                    json.writeObjectField("value", value);
                }
            } catch (RuntimeException e) {
                json.writeObjectField("error", e.toString());
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
                              SerializerProvider provider) throws IOException {
            json.writeStartObject();
            json.writeNumberField("count", counter.getCount());
            json.writeEndObject();
        }
    }

    private static class HistogramSerializer extends StdSerializer<Histogram> {

        private static final long serialVersionUID = 1L;

        private final boolean showSamples;
        private final Double scaleFactor;

        private HistogramSerializer(boolean showSamples, Double scaleFactor) {
            super(Histogram.class);
            this.showSamples = showSamples;
            this.scaleFactor = scaleFactor;
        }

        @Override
        public void serialize(Histogram histogram,
                              JsonGenerator json,
                              SerializerProvider provider) throws IOException {
            json.writeStartObject();
            final Snapshot snapshot = histogram.getSnapshot();
            json.writeNumberField("count", histogram.getCount());
            json.writeNumberField("max", snapshot.getMax());
            json.writeNumberField("mean", round(snapshot.getMean(), scaleFactor));
            json.writeNumberField("min", snapshot.getMin());
            json.writeNumberField("p50", round(snapshot.getMedian(), scaleFactor));
            json.writeNumberField("p75", round(snapshot.get75thPercentile(), scaleFactor));
            json.writeNumberField("p95", round(snapshot.get95thPercentile(), scaleFactor));
            json.writeNumberField("p98", round(snapshot.get98thPercentile(), scaleFactor));
            json.writeNumberField("p99", round(snapshot.get99thPercentile(), scaleFactor));
            json.writeNumberField("p999", round(snapshot.get999thPercentile(), scaleFactor));

            if (showSamples) {
                json.writeObjectField("values", snapshot.getValues());
            }

            json.writeNumberField("stddev", round(snapshot.getStdDev(), scaleFactor));
            json.writeEndObject();
        }
    }

    private static class MeterSerializer extends StdSerializer<Meter> {

        private static final long serialVersionUID = 1L;

        private final String rateUnit;
        private final double rateFactor;
        private final Double scaleFactor;

        public MeterSerializer(TimeUnit rateUnit, Double scaleFactor) {
            super(Meter.class);
            this.rateFactor = rateUnit.toSeconds(1);
            this.rateUnit = calculateRateUnit(rateUnit, "events");
            this.scaleFactor = scaleFactor;
        }

        @Override
        public void serialize(Meter meter,
                              JsonGenerator json,
                              SerializerProvider provider) throws IOException {
            json.writeStartObject();
            json.writeNumberField("count", meter.getCount());
            json.writeNumberField("m15_rate", round(meter.getFifteenMinuteRate() * rateFactor, scaleFactor));
            json.writeNumberField("m1_rate", round(meter.getOneMinuteRate() * rateFactor, scaleFactor));
            json.writeNumberField("m5_rate", round(meter.getFiveMinuteRate() * rateFactor, scaleFactor));
            json.writeNumberField("mean_rate", round(meter.getMeanRate() * rateFactor, scaleFactor));
            json.writeStringField("units", rateUnit);
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
        private final Double scaleFactor;

        private TimerSerializer(TimeUnit rateUnit,
                                TimeUnit durationUnit,
                                boolean showSamples,
                                Double scaleFactor) {
            super(Timer.class);
            this.rateUnit = calculateRateUnit(rateUnit, "calls");
            this.rateFactor = rateUnit.toSeconds(1);
            this.durationUnit = durationUnit.toString().toLowerCase(Locale.US);
            this.durationFactor = 1.0 / durationUnit.toNanos(1);
            this.showSamples = showSamples;
            this.scaleFactor = scaleFactor;
        }

        @Override
        public void serialize(Timer timer,
                              JsonGenerator json,
                              SerializerProvider provider) throws IOException {
            json.writeStartObject();
            final Snapshot snapshot = timer.getSnapshot();
            json.writeNumberField("count", timer.getCount());
            json.writeNumberField("max", round(snapshot.getMax() * durationFactor, scaleFactor));
            json.writeNumberField("mean", round(snapshot.getMean() * durationFactor, scaleFactor));
            json.writeNumberField("min", round(snapshot.getMin() * durationFactor, scaleFactor));

            json.writeNumberField("p50", round(snapshot.getMedian() * durationFactor, scaleFactor));
            json.writeNumberField("p75", round(snapshot.get75thPercentile() * durationFactor, scaleFactor));
            json.writeNumberField("p95", round(snapshot.get95thPercentile() * durationFactor, scaleFactor));
            json.writeNumberField("p98", round(snapshot.get98thPercentile() * durationFactor, scaleFactor));
            json.writeNumberField("p99", round(snapshot.get99thPercentile() * durationFactor, scaleFactor));
            json.writeNumberField("p999", round(snapshot.get999thPercentile() * durationFactor, scaleFactor));

            if (showSamples) {
                final long[] values = snapshot.getValues();
                final double[] scaledValues = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    scaledValues[i] = values[i] * durationFactor;
                }
                json.writeObjectField("values", scaledValues);
            }

            json.writeNumberField("stddev", round(snapshot.getStdDev() * durationFactor, scaleFactor));
            json.writeNumberField("m15_rate", round(timer.getFifteenMinuteRate() * rateFactor, scaleFactor));
            json.writeNumberField("m1_rate", round(timer.getOneMinuteRate() * rateFactor, scaleFactor));
            json.writeNumberField("m5_rate", round(timer.getFiveMinuteRate() * rateFactor, scaleFactor));
            json.writeNumberField("mean_rate", round(timer.getMeanRate() * rateFactor, scaleFactor));
            json.writeStringField("duration_units", durationUnit);
            json.writeStringField("rate_units", rateUnit);
            json.writeEndObject();
        }
    }

    private static double round(double value, Double scaleFactor) {
        return scaleFactor == null ? value : Math.round(value * scaleFactor) / scaleFactor;
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
                              SerializerProvider provider) throws IOException {
            json.writeStartObject();
            json.writeStringField("version", VERSION.toString());
            json.writeObjectField("gauges", registry.getGauges(filter));
            json.writeObjectField("counters", registry.getCounters(filter));
            json.writeObjectField("histograms", registry.getHistograms(filter));
            json.writeObjectField("meters", registry.getMeters(filter));
            json.writeObjectField("timers", registry.getTimers(filter));
            json.writeEndObject();
        }
    }

    protected final TimeUnit rateUnit;
    protected final TimeUnit durationUnit;
    protected final boolean showSamples;
    protected final MetricFilter filter;
    protected final Double scaleFactor;

    public MetricsModule(TimeUnit rateUnit, TimeUnit durationUnit, boolean showSamples) {
        this(rateUnit, durationUnit, showSamples, MetricFilter.ALL);
    }

    public MetricsModule(TimeUnit rateUnit, TimeUnit durationUnit, boolean showSamples, MetricFilter filter) {
        this(rateUnit, durationUnit, showSamples, filter, Optional.empty());
    }

    /**
     * Creates a new module for serializing {@link MetricRegistry}
     *
     * @param rateUnit     the time unit in which rates will be represented
     * @param durationUnit the time unit in which durations will be represented
     * @param showSamples  whether data samples in histograms should be returned
     * @param filter       the filter according to which metrics will be filtered
     * @param scale        the optional non-negative number which determines the count of decimal digits in the
     *                     fractional part of floating point values. If not set, no scaling will be performed.
     */
    public MetricsModule(TimeUnit rateUnit, TimeUnit durationUnit, boolean showSamples, MetricFilter filter,
                         Optional<Integer> scale) {
        this.rateUnit = rateUnit;
        this.durationUnit = durationUnit;
        this.showSamples = showSamples;
        this.filter = filter;
        if (!scale.isPresent()) {
            scaleFactor = null;
        } else {
            if (scale.get() < 0) {
                throw new IllegalArgumentException("scale should be >= 0");
            }
            scaleFactor = Math.pow(10, scale.get());
        }
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
                new GaugeSerializer(scaleFactor),
                new CounterSerializer(),
                new HistogramSerializer(showSamples, scaleFactor),
                new MeterSerializer(rateUnit, scaleFactor),
                new TimerSerializer(rateUnit, durationUnit, showSamples, scaleFactor),
                new MetricRegistrySerializer(filter)
        )));
    }

    private static String calculateRateUnit(TimeUnit unit, String name) {
        final String s = unit.toString().toLowerCase(Locale.US);
        return name + '/' + s.substring(0, s.length() - 1);
    }
}
