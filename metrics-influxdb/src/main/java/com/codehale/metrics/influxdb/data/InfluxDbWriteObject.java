package com.codehale.metrics.influxdb.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This class contains the request object to be sent to InfluxDb for writing. It contains a collection of points.
 */
public class InfluxDbWriteObject {

    private String database;

    private String precision;

    private Set<InfluxDbPoint> points;

    private Map<String, String> tags = Collections.emptyMap();

    public InfluxDbWriteObject(final String database, final TimeUnit timeUnit) {
        this.points = new HashSet<>();
        this.database = database;
        this.precision = toTimePrecision(timeUnit);
    }

    private static String toTimePrecision(TimeUnit t) {
        switch (t) {
            case HOURS:
                return "h";
            case MINUTES:
                return "m";
            case SECONDS:
                return "s";
            case MILLISECONDS:
                return "ms";
            case MICROSECONDS:
                return "u";
            case NANOSECONDS:
                return "n";
            default:
                throw new IllegalArgumentException(
                        "time precision should be HOURS OR MINUTES OR SECONDS or MILLISECONDS or MICROSECONDS OR NANOSECONDS");
        }
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public Set<InfluxDbPoint> getPoints() {
        return points;
    }

    public void setPoints(Set<InfluxDbPoint> points) {
        this.points = points;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = Collections.unmodifiableMap(tags);
    }
}
