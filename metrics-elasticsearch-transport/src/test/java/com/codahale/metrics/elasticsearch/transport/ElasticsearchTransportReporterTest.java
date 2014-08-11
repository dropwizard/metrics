package com.codahale.metrics.elasticsearch.transport;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.InternalTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Order;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.elasticsearch.ElasticsearchReporter;
import com.codahale.metrics.elasticsearch.MetricElasticsearchTypes;

public class ElasticsearchTransportReporterTest {
    private final String prefix = "prefix";
    private final MetricRegistry registry = new MetricRegistry();

    private static Node node;
    private static Client client;
    private ElasticsearchReporter reporter;

    @BeforeClass
    public static void beforeClass() {
        NodeBuilder nodeBuilder = nodeBuilder();
        nodeBuilder.settings()
                .put("path.data", System.getProperty("java.io.tmpdir")).build();
        node = nodeBuilder.clusterName("ElasticsearchTransportReporterTest")
                .node();
        client = node.client();
    }

    @AfterClass
    public static void afterClass() {
        client.close();
        node.close();
    }

    @Before
    public void setUp() {
        reporter = ElasticsearchTransportReporter
                .forRegistryAndIndexPrefix(registry, "test")
                .withClock(Clock.defaultClock()).prefixedWith(prefix)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .withBulkRequestLimit(10).filter(MetricFilter.ALL)
                .build(client);
        reporter.start(1, TimeUnit.SECONDS);
    }

    @After
    public void teardown() {
        reporter.stop();
        deleteIndices();
    }

    @Test
    public void testReportsWithBulkRequestLimit() {
        DecimalFormat formatter = new DecimalFormat("00");
        String metricNamePrefix = "com.codahale.metrics.elasticsearch.test.counter";
        for (int i = 0; i < 15; i++) {
            Counter counter = registry.counter(metricNamePrefix
                    + formatter.format(i));
            counter.inc();
        }
        waitForReporter();
        refreshIndices();

        SearchResponse searchResponse = new SearchRequestBuilder(client)
                .setIndices("_all")
                .setTypes(MetricElasticsearchTypes.COUNTER)
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(0)
                .addAggregation(
                        AggregationBuilders
                                .terms("timestamps")
                                .size(50)
                                .field("@timestamp")
                                .order(Order.term(true))
                                .subAggregation(
                                        AggregationBuilders.terms("names")
                                                .size(50).field("@name")
                                                .order(Order.term(true))))
                .execute().actionGet();

        InternalTerms timestamps = searchResponse.getAggregations().get(
                "timestamps");
        Collection<Bucket> timestampBuckets = timestamps.getBuckets();
        Assert.assertEquals(true, timestampBuckets.size() > 0);
        for (Bucket bucket : timestampBuckets) {
            InternalTerms names = bucket.getAggregations().get("names");
            Assert.assertEquals(15, names.getBuckets().size());
            for (int i = 0; i < names.getBuckets().size(); i++) {
                Assert.assertEquals(
                        prefix + "." + metricNamePrefix + formatter.format(i),
                        ((Bucket) names.getBuckets().toArray()[i]).getKey());
            }
            break;
        }
    }

    @Test
    public void testReportsWithAlternateTimestampField() {
        reporter.stop();
        reporter = ElasticsearchTransportReporter
                .forRegistryAndIndexPrefix(registry, "test")
                .withTimestampFieldName("@timeywimey")
                .withClock(Clock.defaultClock()).prefixedWith(prefix)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .withBulkRequestLimit(10).filter(MetricFilter.ALL)
                .build(client);
        reporter.start(1, TimeUnit.SECONDS);

        String metricName = "com.codahale.metrics.elasticsearch.test.counter";

        Counter counter = registry.counter(metricName);
        counter.inc();

        waitForReporter();
        refreshIndices();

        SearchResponse searchResponse = new SearchRequestBuilder(client)
                .setIndices("_all").setTypes(MetricElasticsearchTypes.COUNTER)
                .setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        Assert.assertEquals(true, searchResponse.getHits().getHits().length > 0);
        Map<String, Object> searchHitSource = searchResponse.getHits()
                .getHits()[0].getSource();
        Assert.assertEquals(prefix + "." + metricName,
                searchHitSource.get("@name"));
        Assert.assertEquals(1, searchHitSource.get("count"));
        Assert.assertEquals(true, searchHitSource.containsKey("@timeywimey"));
    }

    @Test
    public void testReportsCounters() {
        String metricName = "com.codahale.metrics.elasticsearch.test.counter";

        Counter counter = registry.counter(metricName);
        counter.inc();

        waitForReporter();
        refreshIndices();

        SearchResponse searchResponse = new SearchRequestBuilder(client)
                .setIndices("_all").setTypes(MetricElasticsearchTypes.COUNTER)
                .setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        Assert.assertEquals(true, searchResponse.getHits().getHits().length > 0);
        Map<String, Object> searchHitSource = searchResponse.getHits()
                .getHits()[0].getSource();
        Assert.assertEquals(prefix + "." + metricName,
                searchHitSource.get("@name"));
        Assert.assertEquals(1, searchHitSource.get("count"));
    }

    @Test
    public void testReportsTimers() {
        String metricName = "com.codahale.metrics.elasticsearch.test.timer";

        Timer timer = registry.timer(metricName);
        timer.update(1000, TimeUnit.MILLISECONDS);

        waitForReporter();
        refreshIndices();

        SearchResponse searchResponse = new SearchRequestBuilder(client)
                .setIndices("_all").setTypes(MetricElasticsearchTypes.TIMER)
                .setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        Assert.assertEquals(true, searchResponse.getHits().getHits().length > 0);
        Map<String, Object> searchHitSource = searchResponse.getHits()
                .getHits()[0].getSource();
        Assert.assertEquals(prefix + "." + metricName,
                searchHitSource.get("@name"));
        Assert.assertEquals(1, searchHitSource.get("count"));
    }

    @Test
    public void testReportsMeters() {
        String metricName = "com.codahale.metrics.elasticsearch.test.meter";

        Meter meter = registry.meter(metricName);
        meter.mark();

        waitForReporter();
        refreshIndices();

        SearchResponse searchResponse = new SearchRequestBuilder(client)
                .setIndices("_all").setTypes(MetricElasticsearchTypes.METER)
                .setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        Assert.assertEquals(true, searchResponse.getHits().getHits().length > 0);
        Map<String, Object> searchHitSource = searchResponse.getHits()
                .getHits()[0].getSource();
        Assert.assertEquals(prefix + "." + metricName,
                searchHitSource.get("@name"));
        Assert.assertEquals(1, searchHitSource.get("count"));
    }

    @Test
    public void testReportsHistograms() {
        String metricName = "com.codahale.metrics.elasticsearch.test.histogram";

        Histogram histogram = registry.histogram(metricName);
        histogram.update(1);

        waitForReporter();
        refreshIndices();

        SearchResponse searchResponse = new SearchRequestBuilder(client)
                .setIndices("_all")
                .setTypes(MetricElasticsearchTypes.HISTOGRAM)
                .setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        Assert.assertEquals(true, searchResponse.getHits().getHits().length > 0);
        Map<String, Object> searchHitSource = searchResponse.getHits()
                .getHits()[0].getSource();
        Assert.assertEquals(prefix + "." + metricName,
                searchHitSource.get("@name"));
        Assert.assertEquals(1, searchHitSource.get("count"));
    }

    @Test
    public void testReportsGauges() {
        testReportsGaugeOfType(Integer.class, 1);
        testReportsGaugeOfType(Float.class, Float.MAX_VALUE);
        testReportsGaugeOfType(Double.class, Double.MAX_VALUE);
        testReportsGaugeOfType(Byte.class, (byte) 1);
        testReportsGaugeOfType(Short.class, (short) 1);
        testReportsGaugeOfType(Long.class, Long.MAX_VALUE);
        testReportsGaugeOfType(String.class, "test");
    }

    private <T> void testReportsGaugeOfType(Class<T> type, final T value) {
        String metricName = "com.codahale.metrics.elasticsearch.test.guage."
                + type.getSimpleName().toLowerCase();

        registry.register(metricName, new Gauge<T>() {
            @Override
            public T getValue() {
                return value;
            }
        });

        waitForReporter();
        refreshIndices();

        SearchResponse searchResponse = new SearchRequestBuilder(client)
                .setIndices("_all")
                .setTypes(MetricElasticsearchTypes.GAUGE)
                .setQuery(
                        QueryBuilders.matchPhraseQuery("@name", prefix + "."
                                + metricName)).execute().actionGet();
        Assert.assertEquals(true, searchResponse.getHits().getHits().length > 0);
        Map<String, Object> searchHitSource = searchResponse.getHits()
                .getHits()[0].getSource();
        Assert.assertEquals(prefix + "." + metricName,
                searchHitSource.get("@name"));
        if (value instanceof Float) {
            // Note: Elasticsearch returns Float value as Double
            Assert.assertEquals(Double.valueOf(value.toString()),
                    (Double) searchHitSource.get("floatValue"));
        } else if (value instanceof Double) {
            Assert.assertEquals((Double) value,
                    (Double) searchHitSource.get("doubleValue"));
        } else if (value instanceof Byte) {
            // Note: Elasticsearch returns Byte value as Integer
            Assert.assertEquals(((Byte) value).intValue(),
                    ((Integer) searchHitSource.get("byteValue")).intValue());
        } else if (value instanceof Short) {
            // Note: Elasticsearch returns Short value as Integer
            Assert.assertEquals(((Short) value).intValue(),
                    ((Integer) searchHitSource.get("shortValue")).intValue());
        } else if (value instanceof Integer) {
            Assert.assertEquals((Integer) value,
                    (Integer) searchHitSource.get("integerValue"));
        } else if (value instanceof Long) {
            Assert.assertEquals((Long) value,
                    (Long) searchHitSource.get("longValue"));
        } else {
            Assert.assertEquals((String) value,
                    (String) searchHitSource.get("stringValue"));
        }
    }

    private void waitForReporter() {
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
    }

    private void refreshIndices() {
        new RefreshRequestBuilder(client.admin().indices()).get();
    }

    private void deleteIndices() {
        new DeleteIndexRequestBuilder(client.admin().indices(), "test-*")
                .execute().actionGet();
    }
}
