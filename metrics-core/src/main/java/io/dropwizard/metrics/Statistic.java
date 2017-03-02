package io.dropwizard.metrics;

/**
 * An settable statistic metric.
 */
public class Statistic implements Metric, Counting {
    private final LongAdder count;

    /**
     * Creates a new {@link Statistic}.
     */
    public Statistic() {
        this.count = LongAdderFactory.create();
    }

    /**
     * Sets a statistics value.
     *
     * @param value the value to set
     */
    public void set(int value) {
        count.set(value);
    }

    /**
     * Sets a statistics value.
     *
     * @param value the value to set
     */
    public void set(long value) {
        count.set(value);
    }

    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    @Override
    public long getCount() {
        return count.sum();
    }
}
