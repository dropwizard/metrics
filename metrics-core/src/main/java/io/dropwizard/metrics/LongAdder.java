package io.dropwizard.metrics;

interface LongAdder {

    void set(long x);

    void add(long x);

    void increment();

    void decrement();

    long sum();

    void reset();

    long sumThenReset();

    long longValue();

    int intValue();

    float floatValue();

    double doubleValue();

}
