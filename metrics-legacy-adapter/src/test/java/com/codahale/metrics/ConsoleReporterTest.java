package com.codahale.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
class ConsoleReporterTest {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final String dateHeader = SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_1_8) ?
            "3/17/13 6:04:36 PM =============================================================" :
            // https://bugs.openjdk.org/browse/JDK-8304925
            SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_20) ?
            "3/17/13, 6:04:36\u202FPM ============================================================" :
            "3/17/13, 6:04:36 PM ============================================================";

    @AfterEach
    void tearDown() throws Exception {
        executor.shutdownNow();
    }

    @Test
    void testCreateConsoleReporter() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.timer("test-timer");
        metricRegistry.meter("test-meter");
        metricRegistry.histogram("test-histogram");
        metricRegistry.counter("test-counter");
        metricRegistry.register("test-gauge", (Gauge<Integer>) () -> 20);

        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry)
                .shutdownExecutorOnStop(false)
                .scheduleOn(executor)
                .outputTo(new PrintStream(byteArrayOutputStream))
                .formattedFor(Locale.ENGLISH)
                .withClock(new Clock() {

                    @Override
                    public long getTime() {
                        return 1363568676000L;
                    }

                    @Override
                    public long getTick() {
                        return 0;
                    }
                }).formattedFor(TimeZone.getTimeZone("America/Los_Angeles"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .disabledMetricAttributes(EnumSet.of(MetricAttribute.P98, MetricAttribute.P99))
                .build();
        consoleReporter.report();

        assertThat(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8))
                .isEqualToNormalizingNewlines(dateHeader + "\n" +
                        "\n" +
                        "-- Gauges ----------------------------------------------------------------------\n" +
                        "test-gauge\n" +
                        "             value = 20\n" +
                        "\n" +
                        "-- Counters --------------------------------------------------------------------\n" +
                        "test-counter\n" +
                        "             count = 0\n" +
                        "\n" +
                        "-- Histograms ------------------------------------------------------------------\n" +
                        "test-histogram\n" +
                        "             count = 0\n" +
                        "               sum = 0\n" +
                        "               min = 0\n" +
                        "               max = 0\n" +
                        "              mean = 0.00\n" +
                        "            stddev = 0.00\n" +
                        "            median = 0.00\n" +
                        "              75% <= 0.00\n" +
                        "              95% <= 0.00\n" +
                        "            99.9% <= 0.00\n" +
                        "\n" +
                        "-- Meters ----------------------------------------------------------------------\n" +
                        "test-meter\n" +
                        "             count = 0\n" +
                        "               sum = 0\n" +
                        "         mean rate = 0.00 events/second\n" +
                        "     1-minute rate = 0.00 events/second\n" +
                        "     5-minute rate = 0.00 events/second\n" +
                        "    15-minute rate = 0.00 events/second\n" +
                        "\n" +
                        "-- Timers ----------------------------------------------------------------------\n" +
                        "test-timer\n" +
                        "             count = 0\n" +
                        "               sum = 0.00\n" +
                        "         mean rate = 0.00 calls/second\n" +
                        "     1-minute rate = 0.00 calls/second\n" +
                        "     5-minute rate = 0.00 calls/second\n" +
                        "    15-minute rate = 0.00 calls/second\n" +
                        "               min = 0.00 milliseconds\n" +
                        "               max = 0.00 milliseconds\n" +
                        "              mean = 0.00 milliseconds\n" +
                        "            stddev = 0.00 milliseconds\n" +
                        "            median = 0.00 milliseconds\n" +
                        "              75% <= 0.00 milliseconds\n" +
                        "              95% <= 0.00 milliseconds\n" +
                        "            99.9% <= 0.00 milliseconds\n" +
                        "\n" +
                        "\n"
                );
    }
}
