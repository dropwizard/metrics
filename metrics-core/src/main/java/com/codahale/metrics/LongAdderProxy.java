package com.codahale.metrics;

/**
 * Proxy for creating long adders depending on the runtime. By default it tries to
 * the JDK's implementation and fallbacks to the internal one if the JDK doesn't provide
 * any. The JDK's LongAdder and the internal one don't have a common interface, therefore
 * we adapten them to {@link InternalLongAdderProvider}, which serves as a common interface for
 * long adders.
 */
class LongAdderProxy {

    private interface Provider {
        LongAdderAdapter get();
    }

    /**
     * To avoid NoClassDefFoundError during loading {@link LongAdderProxy}
     */
    private static class JdkProvider implements Provider {

        @Override
        public LongAdderAdapter get() {
            return new LongAdderAdapter() {
                private final java.util.concurrent.atomic.LongAdder longAdder =
                        new java.util.concurrent.atomic.LongAdder();

                @Override
                public void add(long x) {
                    longAdder.add(x);
                }

                @Override
                public long sum() {
                    return longAdder.sum();
                }

                @Override
                public void increment() {
                    longAdder.increment();
                }

                @Override
                public void decrement() {
                    longAdder.decrement();
                }

                @Override
                public long sumThenReset() {
                    return longAdder.sumThenReset();
                }
            };
        }
    }

    /**
     * Backed by the internal LongAdder
     */
    private static class InternalLongAdderProvider implements Provider {

        @Override
        public LongAdderAdapter get() {
            return new LongAdderAdapter() {
                private final LongAdder longAdder = new LongAdder();

                @Override
                public void add(long x) {
                    longAdder.add(x);
                }

                @Override
                public long sum() {
                    return longAdder.sum();
                }

                @Override
                public void increment() {
                    longAdder.increment();
                }

                @Override
                public void decrement() {
                    longAdder.decrement();
                }

                @Override
                public long sumThenReset() {
                    return longAdder.sumThenReset();
                }
            };
        }

    }

    private static final Provider INSTANCE = getLongAdderProvider();
    private static Provider getLongAdderProvider() {
        try {
            final JdkProvider jdkProvider = new JdkProvider();
            jdkProvider.get(); // To trigger a possible `NoClassDefFoundError` exception
            return jdkProvider;
        } catch (Throwable e) {
            return new InternalLongAdderProvider();
        }
    }

    public static LongAdderAdapter create() {
        return INSTANCE.get();
    }
}
