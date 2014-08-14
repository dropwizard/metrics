package com.codahale.metrics.elasticsearch.transport;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Clock;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.elasticsearch.ElasticsearchReporter;

/**
 * A reporter which publishes Kibana-friendly metric values to an Elasticsearch
 * cluster through the Elasticsearch transport protocol
 * 
 * @see <a href="http://www.elasticsearch.org/">Elasticsearch</a>
 * @see <a href="http://www.elasticsearch.org/overview/kibana/">Kibana</a>
 * @see <a
 *      href="http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/modules-transport.html">Elasticsearch
 *      Transport API</a>
 */
public class ElasticsearchTransportReporter extends ElasticsearchReporter {
    /**
     * Returns a new {@link Builder} for
     * {@link ElasticsearchTransportReporter}.
     *
     * @param registry
     *            the registry to report
     * @param elasticsearchIndexPrefix
     *            the indices created will be prefixed with this value, e.g.
     *            "test" will result with indices like "test-2014.08.10"
     * @return a {@link Builder} instance for a
     *         {@link ElasticsearchTransportReporter}
     */
    public static Builder forRegistryAndIndexPrefix(MetricRegistry registry,
            String elasticsearchIndexPrefix) {
        return new Builder(registry, elasticsearchIndexPrefix);
    }

    /**
     * A builder for {@link ElasticsearchTransportReporter} instances.
     * Defaults to not using a prefix, using the default clock, converting rates
     * to events/second, converting durations to milliseconds, and not filtering
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
        private String timestampFieldName;
        private int bulkRequestLimit;

        private Builder(MetricRegistry registry, String elasticsearchIndexPrefix) {
            this.registry = registry;
            this.elasticsearchIndexPrefix = elasticsearchIndexPrefix;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
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
         * Set the number of documents to index per bulk request, defaults to
         * 2500
         * 
         * @param bulkRequestLimit
         *            The number of documents per request
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
         * Builds a {@link ElasticsearchTransportReporter} with the given
         * properties, sending metrics using the given {@link Client}.
         *
         * @param elasticsearchClient
         *            a {@link Client}
         * @return a {@link ElasticsearchTransportReporter}
         */
        public ElasticsearchTransportReporter build(Client elasticsearchClient) {
            return new ElasticsearchTransportReporter(registry,
                    elasticsearchClient, clock, elasticsearchIndexPrefix,
                    timestampFieldName, prefix, rateUnit, durationUnit, filter,
                    bulkRequestLimit);
        }
    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ElasticsearchTransportReporter.class);

    private final Client elasticsearchClient;
    private final int bulkRequestLimit;

    private BulkRequestBuilder bulkRequestBuilder;
    private AtomicInteger requestCount;

    protected ElasticsearchTransportReporter(MetricRegistry registry,
            Client elasticsearchClient, Clock clock,
            String elasticsearchIndexPrefix, String timestampFieldName,
            String metricPrefix, TimeUnit rateUnit, TimeUnit durationUnit,
            MetricFilter filter, int bulkRequestLimit) {
        super(registry, clock, elasticsearchIndexPrefix, timestampFieldName,
                metricPrefix, rateUnit, durationUnit, filter);
        this.bulkRequestLimit = bulkRequestLimit;
        this.elasticsearchClient = elasticsearchClient;
        this.bulkRequestBuilder = new BulkRequestBuilder(elasticsearchClient);
        this.requestCount = new AtomicInteger();
    }

    @Override
    protected void addReportToBulkRequest(String index, String type, String json)
            throws IOException {
        bulkRequestBuilder.add(new IndexRequestBuilder(elasticsearchClient)
                .setIndex(index).setType(type).setSource(json).request());
        if (requestCount.incrementAndGet() >= bulkRequestLimit) {
            sendBulkRequest();
        }
    }

    @Override
    protected void sendBulkRequest() throws IOException {
        BulkResponse response = bulkRequestBuilder.execute().actionGet();
        if (response.hasFailures()) {
            LOGGER.error(response.buildFailureMessage());
        }
        bulkRequestBuilder = new BulkRequestBuilder(elasticsearchClient);
        requestCount.set(0);
    }
}
