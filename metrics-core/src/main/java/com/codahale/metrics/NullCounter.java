package com.codahale.metrics;

/**
 * A {@link Counter} metric that cannot be changed from its initial value
 */
public class NullCounter extends Counter {
	private final long count;

	/**
	 * Initializes the counter's value to 0
	 */
	public NullCounter() {
		this(0);
	}

	/**
	 * Initializes the counter's value to initialValue
	 * 
	 * @param initialValue will be the counter's value
	 */
	public NullCounter(long initialValue) {
		this.count = initialValue;
	}

	/**
     * Does nothing.
     */
	@Override
    public void inc() {
    }

    /**
     * Does nothing.
     *
     * @param n not used
     */
	@Override
    public void inc(long n) {
    }

    /**
     * Does nothing.
     */
	@Override
    public void dec() {
    }

    /**
     * Does nothing.
     *
     * @param n not used
     */
	@Override
    public void dec(long n) {
    }

    /**
     * Returns the counter's constant value.
     *
     * @return the counter's constant value
     */
    @Override
    public long getCount() {
        return count;
    }
}
