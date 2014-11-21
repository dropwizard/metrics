package com.codahale.metrics.graphite.deadqueue;

public class Entry {

    private String name;
    private String value;
    private long timestamp;

    public Entry(String name, String value, long timestamp) {
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
