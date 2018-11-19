package com.codahale.metrics;

/**
 * A triple of moving averages (one-, five-, and fifteen-minute
 * moving average) as needed by {@link Meter}.
 * <p>
 * Included implementations are:
 * <ul>
 * <li>{@link ExponentialMovingAverages} exponential decaying average similar to the {@code top} Unix command.
 * <li>{@link SlidingTimeWindowMovingAverages} simple (unweighted) moving average
 * </ul>
 */
public interface MovingAverages {

    /**
     * Tick the internal clock of the MovingAverages implementation if needed
     * (according to the internal ticking interval)
     */
    void tickIfNecessary();

    /**
     * Update all three moving averages with n events having occurred since the last update.
     *
     * @param n
     */
    void update(long n);

    /**
     * Returns the one-minute moving average rate
     *
     * @return the one-minute moving average rate
     */
    double getM1Rate();

    /**
     * Returns the five-minute moving average rate
     *
     * @return the five-minute moving average rate
     */
    double getM5Rate();

    /**
     * Returns the fifteen-minute moving average rate
     *
     * @return the fifteen-minute moving average rate
     */
    double getM15Rate();
}
