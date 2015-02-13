package com.codahale.metrics;

/**
 * A filter used to determine if a value should be reported or not
 *
 * See {@link Value}
 */
public interface MetricValueFilter {
    /**
     * Matches all values, regardless of type or name.
     */
    MetricValueFilter ALL = new MetricValueFilter() {
        @Override
        public boolean isReportingOn(Value value) {
            return true;
        }
    };

    /**
     * Returns {@code true} if the value matches the filter; {@code false} otherwise.
     *
     * @param value the {@link Value} to match
     * @return {@code true} if the value matches the filter
     */
    boolean isReportingOn(Value value);
}