package com.codahale.metrics.stub;

import com.codahale.metrics.EWMA;
import com.codahale.metrics.Meter;

/**
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute,
 * as well as one- and three-hour exponentially-weighted moving average throughputs.
 *
 * @see EWMA
 */
public class MeterStub extends Meter {

    /**
     * Mark the occurrence of an event.
     */
    public void mark() {
    }

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    public void mark(long n) {
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public double getFifteenMinuteRate() {
        return 0.0;
    }

    @Override
    public double getFiveMinuteRate() {
        return 0.0;
    }

    @Override
    public double getOneHourRate() {
        return 0.0;
    }

    @Override
    public double getThreeHourRate() {
        return 0.0;
    }
    
    @Override
    public double getMeanRate() {
        return 0.0;
    }

    @Override
    public double getOneMinuteRate() {
    	return 0.0;
    }
}
