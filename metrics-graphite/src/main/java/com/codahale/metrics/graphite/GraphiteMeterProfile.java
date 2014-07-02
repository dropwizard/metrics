package com.codahale.metrics.graphite;

/**
 * Specifies which data points to send to Graphite
 */
public class GraphiteMeterProfile {

    private final boolean count;
    private final boolean oneMinuteRate;
    private final boolean fiveMinuteRate;
    private final boolean fifteenMinuteRate;
    private final boolean meanRate;

    private GraphiteMeterProfile(Builder builder) {
        this.count = builder.count;
        this.oneMinuteRate = builder.oneMinuteRate;
        this.fiveMinuteRate = builder.fiveMinuteRate;
        this.fifteenMinuteRate = builder.fifteenMinuteRate;
        this.meanRate = builder.meanRate;
    }

    public boolean isCount() {
        return count;
    }

    public boolean isOneMinuteRate() {
        return oneMinuteRate;
    }

    public boolean isFiveMinuteRate() {
        return fiveMinuteRate;
    }

    public boolean isFifteenMinuteRate() {
        return fifteenMinuteRate;
    }

    public boolean isMeanRate() {
        return meanRate;
    }

    public static class Builder {

        private boolean count = true;
        private boolean oneMinuteRate = true;
        private boolean fiveMinuteRate;
        private boolean fifteenMinuteRate;
        private boolean meanRate;

        public Builder count(boolean count) {
            this.count = count;
            return this;
        }

        public Builder oneMinuteRate(boolean oneMinuteRate) {
            this.oneMinuteRate = oneMinuteRate;
            return this;
        }

        public Builder fiveMinuteRate(boolean fiveMinuteRate) {
            this.fiveMinuteRate = fiveMinuteRate;
            return this;
        }

        public Builder fifteenMinuteRate(boolean fifteenMinuteRate) {
            this.fifteenMinuteRate = fifteenMinuteRate;
            return this;
        }

        public Builder meanRate(boolean meanRate) {
            this.meanRate = meanRate;
            return this;
        }

        public GraphiteMeterProfile build() {
            if (!count && !oneMinuteRate && !fiveMinuteRate && !fifteenMinuteRate && !meanRate) {
                throw new IllegalStateException("Must enable at least one data point");
            }
            return new GraphiteMeterProfile(this);
        }

    }

}
