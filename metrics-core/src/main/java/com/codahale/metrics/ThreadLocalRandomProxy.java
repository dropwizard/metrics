package com.codahale.metrics;

import java.util.Random;

/**
 * Proxy for creating thread local {@link Random} instances depending on the runtime.
 * By default it tries to use the JDK's implementation and fallbacks to the internal
 * one if the JDK doesn't provide any.
 */
class ThreadLocalRandomProxy {

    private interface Provider {
        Random current();
    }

    /**
     * To avoid NoClassDefFoundError during loading {@link ThreadLocalRandomProxy}
     */
    private static class JdkProvider implements Provider {

        @Override
        public Random current() {
            return java.util.concurrent.ThreadLocalRandom.current();
        }
    }

    private static class InternalProvider implements Provider {

        @Override
        public Random current() {
            return ThreadLocalRandom.current();
        }
    }

    private static final Provider INSTANCE = getThreadLocalProvider();
    private static Provider getThreadLocalProvider() {
        try {
            return new JdkProvider();
        } catch (NoClassDefFoundError e) {
            return new InternalProvider();
        }
    }

    public static Random current() {
        return INSTANCE.current();
    }

}
