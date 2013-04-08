package com.yammer.metrics;

public interface ExtendedMetricRegistryListener extends MetricRegistryListener{
	
	 /**
     * Called when a {@link Gauge} is retrieved from the registry.
     *
     * @param name  the gauge's name
     * @param gauge the gauge
     */
    void onGaugeGet(String name, Gauge<?> gauge);
    
    /**
     * Called when a {@link Counter} is retrieved from the registry.
     *
     * @param name the counter's name
     */
    void onCounterGet(String name, Counter counter);
    
    
    /**
     * Called when a {@link Histogram} is retrieved from the registry.
     *
     * @param name the histogram's name
     */
    void onHistogramGet(String name, Histogram histogram);

    
    /**
     * Called when a {@link Meter} is retrieved from the registry.
     *
     * @param name  the meter's name
     * @param meter the meter
     */
    void onMeterGet(String name, Meter meter);
    
    /**
     * Called when a {@link Timer} is retrieved from the registry.
     *
     * @param name  the timer's name
     * @param timer the timer
     */
    void onTimerGet(String name, Timer timer);

}
