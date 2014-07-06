package com.codahale.metrics;



/**
 * A timer metric which provide a global timer capabilities. Only one measurement at a time.
 */
public abstract class GlobalTimer implements Metered, Sampling {

	/**
	 * Start the timer. If the timer is currently running, it will discard the measurement.
	 */
	public abstract void start();
	
	/**
	 * Stop the timer. If the timer was not started or was already stopped - do nothing.
	 */
	public abstract void stop();
	
	public abstract Timer getTimer();

}
