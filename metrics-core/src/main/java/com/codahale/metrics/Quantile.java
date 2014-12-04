package com.codahale.metrics;

public class Quantile {
    private final String name;
    private final double value;

    public Quantile(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

}
