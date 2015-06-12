package sandbox;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codehale.metrics.influxdb.InfluxDbHttpSender;
import com.codehale.metrics.influxdb.InfluxDbReporter;

public final class SendToLocalInfluxDB {
    private SendToLocalInfluxDB() {

    }

    public static void main(String[] args) {
        InfluxDbReporter influxDbReporter = null;
        ScheduledReporter consoleReporter = null;
        Timer.Context context = null;
        try {
            final MetricRegistry registry = new MetricRegistry();
            consoleReporter = startConsoleReporter(registry);
            influxDbReporter = startInfluxDbReporter(registry);

            final Meter myMeter = registry.meter("testMetric");
            final Map<String, String> tags = new HashMap<>();
            tags.put("env", "test");
            influxDbReporter.putTagsForMetric("testMetric", tags);

            final Timer myTimer = registry.timer("testTimer");
            context = myTimer.time();
            for (int i = 0; i < 5; i++) {
                myMeter.mark();
                myMeter.mark(Math.round(Math.random() * 100.0));
                Thread.sleep(2000);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            System.exit(1);
        } finally {
            if (context != null) {
                context.stop();
            }
            if (influxDbReporter != null) {
                influxDbReporter.report();
                influxDbReporter.stop();
            }
            if (consoleReporter != null) {
                consoleReporter.report();
                consoleReporter.stop();
            }
            System.out.println("Finished");
        }
    }

    private static InfluxDbReporter startInfluxDbReporter(MetricRegistry registry) throws Exception {
        final InfluxDbHttpSender influxDb = new InfluxDbHttpSender("127.0.0.1", 8086, "dropwizard", "root:root");
        final Map<String, String> tags = new HashMap<>();
        tags.put("host", "localhost");
        final InfluxDbReporter reporter = InfluxDbReporter
                .forRegistry(registry)
                .withTags(tags)
                .skipIdleMetrics(true)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(influxDb);
        reporter.start(10, TimeUnit.SECONDS);
        return reporter;
    }

    private static ConsoleReporter startConsoleReporter(MetricRegistry registry) throws Exception {
        final ConsoleReporter reporter = ConsoleReporter
                .forRegistry(registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.MINUTES);
        return reporter;
    }
}
