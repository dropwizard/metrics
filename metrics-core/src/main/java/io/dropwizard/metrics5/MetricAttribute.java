package io.dropwizard.metrics5;

/**
 * Represents attributes of metrics which can be reported.
 */
public enum MetricAttribute {

    MAX("max"),
    MEAN("mean"),
    MIN("min"),
    STDDEV("stddev"),
    P50("p50"),
    P75("p75"),
    P95("p95"),
    P98("p98"),
    P99("p99"),
    P999("p999"),
    COUNT("count"),
    SUM("sum"),
    M1_RATE("m1_rate"),
    M5_RATE("m5_rate"),
    M15_RATE("m15_rate"),
    MEAN_RATE("mean_rate");

    private final String code;

    MetricAttribute(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
