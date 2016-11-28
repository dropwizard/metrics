package io.dropwizard.metrics;

import java.util.concurrent.atomic.LongAdder;

class Java8LongAdderImpl implements io.dropwizard.metrics.LongAdder {

    private final LongAdder delegate;

    public Java8LongAdderImpl() {
        this.delegate = new LongAdder();
    }

	@Override
	public void add(long x) {
		delegate.add(x);
	}

	@Override
	public void increment() {
		delegate.increment();
	}

	public byte byteValue() {
		return delegate.byteValue();
	}

	@Override
	public void decrement() {
		delegate.decrement();
	}

	@Override
	public long sum() {
		return delegate.sum();
	}

	public short shortValue() {
		return delegate.shortValue();
	}

	@Override
	public void reset() {
		delegate.reset();
	}

	@Override
	public long sumThenReset() {
		return delegate.sumThenReset();
	}

	@Override
	public long longValue() {
		return delegate.longValue();
	}

	@Override
	public int intValue() {
		return delegate.intValue();
	}

	@Override
	public float floatValue() {
		return delegate.floatValue();
	}

	@Override
	public double doubleValue() {
		return delegate.doubleValue();
	}

	@Override
    public int hashCode() {
        return delegate.hashCode();
    }

	@Override
	public String toString() {
		return delegate.toString();
	}

}
