package com.codahale.metrics;

@Deprecated
public abstract class Clock {

    public abstract long getTick();

    public long getTime() {
        return System.currentTimeMillis();
    }

    public io.dropwizard.metrics5.Clock getDelegate() {
        if (this instanceof DelegateClock) {
            return ((DelegateClock) this).delegate;
        }
        final Clock original = this;
        return new io.dropwizard.metrics5.Clock() {
            @Override
            public long getTick() {
                return original.getTick();
            }

            @Override
            public long getTime() {
                return original.getTime();
            }
        };
    }

    public static Clock of(io.dropwizard.metrics5.Clock delegate) {
        return new DelegateClock(delegate);
    }

    public static Clock defaultClock() {
        return of(io.dropwizard.metrics5.Clock.defaultClock());
    }

    public static class UserTimeClock extends Clock {

        private final io.dropwizard.metrics5.Clock delegate = new io.dropwizard.metrics5.Clock.UserTimeClock();

        @Override
        public long getTick() {
            return delegate.getTick();
        }

        @Override
        public long getTime() {
            return delegate.getTime();
        }
    }

    private static class DelegateClock extends Clock {

        private final io.dropwizard.metrics5.Clock delegate;

        private DelegateClock(io.dropwizard.metrics5.Clock delegate) {
            this.delegate = delegate;
        }

        @Override
        public long getTick() {
            return delegate.getTick();
        }
    }
}
