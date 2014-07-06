package com.codahale.metrics.stub;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;

/**
 * A metric which calculates the distribution of a value.
 *
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately computing running
 *      variance</a>
 */
public class HistogramStub extends Histogram {

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    public void update(int value) {
    }

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    public void update(long value) {
    }

    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public Snapshot getSnapshot() {
        return null;
    }
}
