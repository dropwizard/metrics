package io.dropwizard.metrics;

/**
 * A {@link Meter} metric that cannot be changed from its initial value
 */
public class NullMeter extends Meter {
    private final double rate;

    /**
     * Initializes the rate value to 0.0.
     */
    public NullMeter() {
        this(0.0);
    }

    /**
     * Initializes the rate value to initialRate.
     * 
     * @param initialRate will be the meter's rate value
     */
    public NullMeter(double initialRate) {
        rate = initialRate;
    }

    /**
     * Does nothing.
     */
    @Override
    public void mark() {
    }

    /**
     * Does nothing.
     * 
     * @param n not used
     */
    @Override
    public void mark(long n) {
    }

    /**
     * Returns 1.
     */
    @Override
    public long getCount() {
        return 1;
    }

    /**
     * Returns the meter's rate constant.
     * 
     * @return the meter's rate constant
     */
    @Override
    public double getFifteenMinuteRate() {
        return rate;
    }

    /**
     * Returns the meter's rate constant.
     * 
     * @return the meter's rate constant
     */
    @Override
    public double getFiveMinuteRate() {
        return rate;
    }

    /**
     * Returns the meter's rate constant.
     * 
     * @return the meter's rate constant
     */
    @Override
    public double getMeanRate() {
        return rate;
    }

    /**
     * Returns the meter's rate constant.
     * 
     * @return the meter's rate constant
     */
    @Override
    public double getOneMinuteRate() {
        return rate;
    }
}
