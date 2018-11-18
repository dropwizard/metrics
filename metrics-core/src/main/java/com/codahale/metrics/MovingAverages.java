package com.codahale.metrics;

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
     * @return
     */
    double getM1Rate();

    /**
     * Returns the five-minute moving average rate
     *
     * @return
     */
    double getM5Rate();

    /**
     * Returns the fifteen-minute moving average rate
     *
     * @return
     */
    double getM15Rate();
}
