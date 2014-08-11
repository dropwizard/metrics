package com.codahale.metrics.elasticsearch.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Clock;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.elasticsearch.ElasticsearchReporter;

/**
 * A reporter which publishes Kibana-friendly metric values to an Elasticsearch
 * cluster through the Elasticsearch HTTP API
 * 
 * @see <a href="http://www.elasticsearch.org/">Elasticsearch</a>
 * @see <a href="http://www.elasticsearch.org/overview/kibana/">Kibana</a>
 * @see <a
 *      href="http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/modules-http.html">Elasticsearch
 *      HTTP API</a>
 */
public class ElasticsearchHttpReporter extends ElasticsearchReporter {
    /**
     * Returns a new {@link Builder} for {@link ElasticsearchHttpReporter}.
     *
     * @param registry
     *            the registry to report
     * @param elasticsearchIndexPrefix
     *            the indices created will be prefixed with this value, e.g.
     *            "test" will result with indices like "test-2014.08.10"
     * @return a {@link Builder} instance for a
     *         {@link ElasticsearchHttpReporter}
     */
    public static Builder forRegistryAndIndexPrefix(MetricRegistry registry,
            String elasticsearchIndexPrefix) {
        return new Builder(registry, elasticsearchIndexPrefix);
    }

    /**
     * A builder for {@link ElasticsearchHttpReporter} instances. Defaults to
     * not using a prefix, using the default clock, converting rates to
     * events/second, converting durations to milliseconds, and not filtering
     * metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private final String elasticsearchIndexPrefix;
        private Clock clock;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private int timeout;
        private int bulkRequestLimit;
        private String timestampFieldName;

        private Builder(MetricRegistry registry, String elasticsearchIndexPrefix) {
            this.registry = registry;
            this.elasticsearchIndexPrefix = elasticsearchIndexPrefix;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.timeout = 1000;
            this.bulkRequestLimit = 2500;
            this.timestampFieldName = "@timestamp";
        }

        /**
         * Use the given {@link Clock} instance for the time.
         *
         * @param clock
         *            a {@link Clock} instance
         * @return {@code this}
         */
        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Prefix all metric names with the given string.
         *
         * @param prefix
         *            the prefix for all metric names
         * @return {@code this}
         */
        public Builder prefixedWith(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit
         *            a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit
         *            a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter
         *            a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Set the timeout for connecting to a host
         * 
         * @param timeout
         *            The timeout in milliseconds
         * @return {@code this}
         */
        public Builder withTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Set the number of documents to index per bulk request
         * 
         * @param bulkRequestLimit
         *            The number of documents per request, defaults to 2500
         * @return {@code this}
         */
        public Builder withBulkRequestLimit(int bulkRequestLimit) {
            this.bulkRequestLimit = bulkRequestLimit;
            return this;
        }

        /**
         * Sets the field name for the timestamp, defaults to "@timestamp"
         * 
         * @param timestampFieldName
         *            The field name to log the timestamp to
         * @return {@code this}
         */
        public Builder withTimestampFieldName(String timestampFieldName) {
            this.timestampFieldName = timestampFieldName;
            return this;
        }

        /**
         * Builds a {@link ElasticsearchHttpReporter} with the given properties
         * and the given hosts. The reporter will round-robin through each host
         * for sending metrics.
         *
         * @param elasticsearchHosts
         *            The Elasticsearch hosts to send metrics to
         * @return a {@link ElasticsearchHttpReporter}
         */
        public ElasticsearchHttpReporter build(String... elasticsearchHosts) {
            return new ElasticsearchHttpReporter(registry, clock,
                    elasticsearchIndexPrefix, timestampFieldName, prefix,
                    rateUnit, durationUnit, filter, timeout, bulkRequestLimit,
                    elasticsearchHosts);
        }
    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ElasticsearchHttpReporter.class);

    private final String[] elasticsearchHosts;
    private final int timeout;
    private final int bulkRequestLimit;
    private AtomicInteger nextHost, requestCount;
    private StringWriter bulkRequest;

    protected ElasticsearchHttpReporter(MetricRegistry registry, Clock clock,
            String elasticsearchIndexPrefix, String timestampFieldName,
            String metricPrefix, TimeUnit rateUnit, TimeUnit durationUnit,
            MetricFilter filter, int timeout, int bulkRequestLimit,
            String... elasticsearchHosts) {
        super(registry, clock, elasticsearchIndexPrefix, timestampFieldName,
                metricPrefix, rateUnit, durationUnit, filter);
        this.elasticsearchHosts = elasticsearchHosts;
        this.timeout = timeout;
        this.bulkRequestLimit = bulkRequestLimit;

        nextHost = new AtomicInteger();
        requestCount = new AtomicInteger();
        bulkRequest = new StringWriter();
    }

    @Override
    protected void addReportToBulkRequest(String index, String type, String json)
            throws IOException {
        bulkRequest.append("{ \"index\" : { \"_index\" : \"" + index
                + "\", \"_type\" : \"" + type + "\" } }");
        bulkRequest.append("\n");
        bulkRequest.append(json);
        bulkRequest.append("\n");
        if (requestCount.incrementAndGet() >= bulkRequestLimit) {
            sendBulkRequest();
        }
    }

    @Override
    protected void sendBulkRequest() throws IOException {
        HttpURLConnection connection = openConnection("/_bulk", "POST");
        OutputStreamWriter printWriter = new OutputStreamWriter(
                connection.getOutputStream(), "UTF-8");
        printWriter.write(bulkRequest.toString());
        printWriter.flush();
        printWriter.close();
        closeConnection(connection);

        bulkRequest = new StringWriter();
        requestCount.set(0);
    }

    /**
     * Open a new HttpUrlConnection to the Elasticsearch cluster This operates
     * in a round robin fashion to load balance reporting
     */
    private HttpURLConnection openConnection(String uri, String method) {
        int hostIndex = nextHost.get();
        nextHost.set(nextHost.get() == elasticsearchHosts.length - 1 ? 0
                : nextHost.get() + 1);
        try {
            URL templateUrl = new URL("http://" + elasticsearchHosts[hostIndex]
                    + uri);
            HttpURLConnection connection = (HttpURLConnection) templateUrl
                    .openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(timeout);
            connection.setUseCaches(false);
            if (method.equalsIgnoreCase("POST")
                    || method.equalsIgnoreCase("PUT")) {
                connection.setDoOutput(true);
            }
            connection.connect();

            return connection;
        } catch (IOException e) {
            LOGGER.error("Error connecting to {}: {}",
                    elasticsearchHosts[hostIndex], e);
        }
        return null;
    }

    private void closeConnection(HttpURLConnection connection)
            throws IOException {
        connection.getOutputStream().close();
        connection.disconnect();

        if (connection.getResponseCode() != 200) {
            LOGGER.error("Reporting returned code {} {}: {}",
                    connection.getResponseCode(),
                    connection.getResponseMessage());
        }
    }
}
