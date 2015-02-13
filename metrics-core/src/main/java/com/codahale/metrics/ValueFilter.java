package com.codahale.metrics;

import java.util.EnumSet;

/**
 * Implementation of MetricValueFilter.
 */
public class ValueFilter implements MetricValueFilter {


    private EnumSet<Value> reportedValues;

    /**
     * Construct a ValueFilter with reporting on for all values
     */
    public ValueFilter() {
        reportedValues = EnumSet.allOf(Value.class);
    }

    /**
     * Turns reporting off for the given value
     * @return this
     */
    public ValueFilter excludeValue(Value value) {
        reportedValues.remove(value);
        return this;
    }

    /**
     * Turns reporting on for the given value
     * @return this
     */
    public ValueFilter includeValue(Value value) {
        reportedValues.add(value);
        return this;
    }

    /**
     * Turns reporting off for all timer, meter and histogram values
     * @return this
     */
    public ValueFilter excludeAllValues() {
        reportedValues.clear();
        return this;
    }

    @Override
    public boolean isReportingOn(Value value) {
        return reportedValues.contains(value);
    }
}