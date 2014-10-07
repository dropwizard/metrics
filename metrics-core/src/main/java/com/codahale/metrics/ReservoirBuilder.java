package com.codahale.metrics;

import java.util.concurrent.TimeUnit;

public abstract class ReservoirBuilder {
    public static final class ExponentiallyDecayingReservoirBuilder extends ReservoirBuilder {
        private final double alpha;
        private final int size;
        private final Clock clock;

        public ExponentiallyDecayingReservoirBuilder(final int size, final double alpha, final Clock clock) {
            this.size = size;
            this.alpha = alpha;
            this.clock = clock;
        }

        public ExponentiallyDecayingReservoirBuilder(final int size, final double alpha) {
            this(size, alpha, Clock.defaultClock());
        }

        @Override
        public Reservoir newReservoir() {
            return new ExponentiallyDecayingReservoir(size, alpha, clock);
        }
    }

    public static final class SlidingWindowReservoirBuilder extends ReservoirBuilder {
        private final int size;

        public SlidingWindowReservoirBuilder(final int size) {
            this.size = size;
        }

        @Override
        public final Reservoir newReservoir() {
            return new SlidingWindowReservoir(size);
        }
    }

    public static final class SlidingTimeWindowReservoirBuilder extends ReservoirBuilder {
        private final long window;
        private final TimeUnit windowUnit;
        private final Clock clock;

        public SlidingTimeWindowReservoirBuilder(final int window, final TimeUnit windowUnit, final Clock clock) {
            this.window = window;
            this.windowUnit = windowUnit;
            this.clock = clock;
        }

        public SlidingTimeWindowReservoirBuilder(final int window, final TimeUnit windowUnit) {
            this(window, windowUnit, Clock.defaultClock());
        }

        @Override
        public final Reservoir newReservoir() {
            return new SlidingTimeWindowReservoir(window, windowUnit, clock);
        }
    }

    public static final class UniformReservoirBuilder extends ReservoirBuilder {
        private final int size;

        public UniformReservoirBuilder(final int size) {
            this.size = size;
        }

        @Override
        public final Reservoir newReservoir() {
            return new UniformReservoir(size);
        }
    }

    public abstract Reservoir newReservoir();
}
