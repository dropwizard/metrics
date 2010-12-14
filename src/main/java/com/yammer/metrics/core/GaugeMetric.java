package com.yammer.metrics.core;

/**
 * A gauge metric is an instantaneous reading of a partiular value. To
 * instrument a qeue's depth, for example:<br>
 * <pre><code>
 * Queue<String> queue = new ConcurrentLinkedQueue<String>();
 * GaugeMetric<Integer> queueDepth = new GaugeMetric<Integer>() {
 *     public Integer value() {
 *         return queue.size();
 *     }
 * };
 *
 * </code></pre>
 * @author coda
 * @param <T> the type of the metric's value
 */
public abstract class GaugeMetric<T> implements Metric {
	/**
	 * Returns the metric's current value.
	 *
	 * @return the metric's current value
	 */
	public abstract T value();
}
