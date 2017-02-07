package com.codahale.metrics;

/**
 * Interface which exposes the LongAdder functionality. Allows different
 * LongAdder implementations to coexist together.
 */
interface LongAdderAdapter {

    void add(long x);

    long sum();

    void increment();

    void decrement();

    long sumThenReset();
}
