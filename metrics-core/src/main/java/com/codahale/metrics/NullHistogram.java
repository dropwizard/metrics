package com.codahale.metrics;

/**
 * A {@link Histogram} metric that cannot be changed from its initial value
 */
public class NullHistogram extends Histogram {
	private final Snapshot snapshot;

	/**
	 * Initializes the histogram's value to contain a single 0.
	 */
	public NullHistogram() {
		this(0);
	}

	/**
	 * Initializes the histogram's value to contain a single initialValue.
	 * 
	 * @param initialValue will be the histogram's lone value
	 */
	public NullHistogram(long initialValue) {
		this(new long[] { initialValue });
	}

	/**
	 * Initializes the histogram's values to initialValues.
	 * 
	 * @param initialValues will be the histogram's values
	 */
	public NullHistogram(long[] initialValues) {
		super(null);
		this.snapshot = new UniformSnapshot(initialValues);
	}

    /**
     * Does nothing.
     *
     * @param value not used
     */
	@Override
    public void update(int value) {
    }

    /**
     * Does nothing.
     *
     * @param value not used
     */
	@Override
    public void update(long value) {
    }

    /**
     * Returns the histogram's constant number of values recorded.
     *
     * @return the histogram's constant number of values recorded
     */
    @Override
    public long getCount() {
    	return snapshot.size();
    }

    /**
     * Returns a snapshot representing the histogram's constant values.
     *
     * @return a snapshot representing the histogram's constant values
     */
    @Override
    public Snapshot getSnapshot() {
        return snapshot;
    }
}
