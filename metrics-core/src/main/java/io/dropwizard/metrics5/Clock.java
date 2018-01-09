package io.dropwizard.metrics5;

/**
 * An abstraction for how time passes. It is passed to {@link Timer} to track timing.
 */
public abstract class Clock {
    /**
     * Returns the current time tick.
     *
     * @return time tick in nanoseconds
     */
    public abstract long getTick();

    /**
     * Returns the current time in milliseconds.
     *
     * @return time in milliseconds
     */
    public long getTime() {
        return System.currentTimeMillis();
    }

    /**
     * The default clock to use.
     *
     * @return the default {@link Clock} instance
     * @see Clock.UserTimeClock
     */
    public static Clock defaultClock() {
        return UserTimeClockHolder.DEFAULT;
    }

    /**
     * A clock implementation which returns the current time in epoch nanoseconds.
     */
    public static class UserTimeClock extends Clock {
        @Override
        public long getTick() {
            return System.nanoTime();
        }
    }

    private static class UserTimeClockHolder {
        private static final Clock DEFAULT = new UserTimeClock();
    }
}
