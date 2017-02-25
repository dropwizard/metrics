package com.codahale.metrics;

/**
 * A metric which calculates the distribution of a value.
 *
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately computing running
 *      variance</a>
 */
public class Histogram implements Metric, Sampling, Counting {
    private final String name;
    private final MeasurementPublisher measurementPublisher;
    private final Reservoir reservoir;
    private final LongAdderAdapter count;

    /**
     * Creates a new {@link Histogram} with the given reservoir.
     *
     * @param name The name of the metric
     * @param measurementPublisher a publisher which the metric should notify of any measurements
     * @param reservoir the reservoir to create a histogram from
     */
    public Histogram(String name, MeasurementPublisher measurementPublisher, Reservoir reservoir) {
        this.name = name;
        this.measurementPublisher = measurementPublisher;
        this.reservoir = reservoir;
        this.count = LongAdderProxy.create();
    }

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    public void update(int value) {
        update((long) value);
    }

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    public void update(long value) {
        count.increment();
        reservoir.update(value);
        measurementPublisher.histogramUpdated(name, value);
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

    @Override
    public Snapshot getSnapshot() {
        return reservoir.getSnapshot();
    }
}
