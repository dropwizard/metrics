/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 *
 * http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/src/jsr166e/LongAdder.java?revision=1.14&view=markup
 */

package com.codahale.metrics;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

// CHECKSTYLE:OFF
/**
 * <p><em>amire note: This class used to be implemented using a Striped64 class. We decided to replace
 * it with a simpler LongAdder implementation, since we don't expect a lot of simultaneous updates from
 * many threads in an android application, and since Striped64 used UNSAFE stuff whose availability
 * is questionable in android.
 *
 * @author Amir Eshel
 * @author Doug Lea
 * @since 1.8
 */
@SuppressWarnings("all")
class LongAdder extends AtomicLong implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    /**
     * Version of plus for use in retryUpdate
     */
    final long fn(long v, long x) {
        return v + x;
    }

    /**
     * Creates a new adder with initial sum of zero.
     */
    LongAdder() {
    }

    /**
     * Adds the given value.
     *
     * @param x the value to add
     */
    public void add(long x) {
    	addAndGet(x);
    }

    /**
     * Equivalent to {@code add(1)}.
     */
    public void increment() {
        add(1L);
    }

    /**
     * Equivalent to {@code add(-1)}.
     */
    public void decrement() {
        add(-1L);
    }

    /**
     * Returns the current sum. 
     *
     * @return the sum
     */
    public long sum() {
        return get();
    }

    /**
     * Resets variables maintaining the sum to zero.  This method may be a useful alternative to
     * creating a new adder, but is only effective if there are no concurrent updates.  Because this
     * method is intrinsically racy, it should only be used when it is known that no threads are
     * concurrently updating.
     */
    public void reset() {
        set(0);
    }

    /**
     * Equivalent in effect to {@link #sum} followed by {@link #reset}. This method may apply for
     * example during quiescent points between multithreaded computations.  If there are updates
     * concurrent with this method, the returned value is <em>not</em> guaranteed to be the final
     * value occurring before the reset.
     *
     * @return the sum
     */
    public long sumThenReset() {
        return getAndSet(0);
    }

    /**
     * Returns the String representation of the {@link #sum}.
     *
     * @return the String representation of the {@link #sum}
     */
    public String toString() {
        return Long.toString(sum());
    }

    /**
     * Equivalent to {@link #sum}.
     *
     * @return the sum
     */
    public long longValue() {
        return sum();
    }

    /**
     * Returns the {@link #sum} as an {@code int} after a narrowing primitive conversion.
     */
    public int intValue() {
        return (int) sum();
    }

    /**
     * Returns the {@link #sum} as a {@code float} after a widening primitive conversion.
     */
    public float floatValue() {
        return (float) sum();
    }

    /**
     * Returns the {@link #sum} as a {@code double} after a widening primitive conversion.
     */
    public double doubleValue() {
        return (double) sum();
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject();
        s.writeLong(sum());
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        set(s.readLong());
    }
}
// CHECKSTYLE:ON
