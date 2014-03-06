package com.codahale.metrics;

import java.util.Iterator;
import java.util.LinkedList;

/** Sliding window increment only counter.
 * Inspired by <a>https://github.com/boundary/folsom#spiral-meter</a>
 */
public class Spiral implements Metric {
    private final long window;
    private final LinkedList<Long> marks;
    private final Clock clock;

    /**
     * Creates a new {@link Spiral} with a one minute window and the default
     * {@link Clock}.
     */
    public Spiral() {
        this(60 * 1000L, Clock.defaultClock());
    }
    /** Creates a new Spiral with given sliding window and {@link Clock}.
     *
     * @param window in milliseconds.
     * @param clock  the {@link Clock} implementation the spiral should use
     */
    public Spiral(long window, Clock clock) {
        this.window = window;
        this.marks = new LinkedList<Long>();
        this.clock = clock;
    }

    /**
     * Increment the spiral by one.
     */
    public void mark() {
        mark(1);
    }
    /**
     * Increment the spiral by {@code n}.
     *
     * @param n the amount by which the spiral will be increased
     */
    public synchronized void mark(int n) {
        final long time = clock.getTime();
        trim(time);
        for (int i=0; i<n; i++)
            marks.add(time);
    }

    /**
     * Returns the spiral's current value.
     *
     * @return the spiral's current value
     */
    public synchronized int getValue() {
        trim(clock.getTime());
        return marks.size();
    }

    private void trim(long time) {
        if (marks.isEmpty())
            return;
        final long oldest = time - window;
        if (marks.getLast() < oldest) {
            marks.clear();
            return;
        }
        for (Iterator<Long> i = marks.iterator(); i.hasNext();)
            if (i.next() < oldest)
                i.remove();
            else
                break;
    }
}
