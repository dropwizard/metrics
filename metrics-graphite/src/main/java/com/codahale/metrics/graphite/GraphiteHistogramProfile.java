package com.codahale.metrics.graphite;

/**
 * Specifies which data points to send to Graphite
 */
public class GraphiteHistogramProfile {

    private final boolean count;
    private final boolean max;
    private final boolean mean;
    private final boolean min;
    private final boolean stdDev;
    private final boolean percentile50th;
    private final boolean percentile75th;
    private final boolean percentile95th;
    private final boolean percentile98th;
    private final boolean percentile99th;
    private final boolean percentile999th;

    private GraphiteHistogramProfile(Builder builder) {
        this.count = builder.count;
        this.max = builder.max;
        this.mean = builder.mean;
        this.min = builder.min;
        this.stdDev = builder.stdDev;
        this.percentile50th = builder.percentile50th;
        this.percentile75th = builder.percentile75th;
        this.percentile95th = builder.percentile95th;
        this.percentile98th = builder.percentile98th;
        this.percentile99th = builder.percentile99th;
        this.percentile999th = builder.percentile999th;
    }

    public boolean isCount() {
        return count;
    }

    public boolean isMax() {
        return max;
    }

    public boolean isMean() {
        return mean;
    }

    public boolean isMin() {
        return min;
    }

    public boolean isStdDev() {
        return stdDev;
    }

    public boolean is50thPercentile() {
        return percentile50th;
    }

    public boolean is75thPercentile() {
        return percentile75th;
    }

    public boolean is95thPercentile() {
        return percentile95th;
    }

    public boolean is98thPercentile() {
        return percentile98th;
    }

    public boolean is99thPercentile() {
        return percentile99th;
    }

    public boolean is999thPercentile() {
        return percentile999th;
    }

    public static class Builder {

        private boolean count = true;
        private boolean max = true;
        private boolean mean;
        private boolean min = true;
        private boolean stdDev;
        private boolean percentile50th;
        private boolean percentile75th;
        private boolean percentile95th = true;
        private boolean percentile98th;
        private boolean percentile99th;
        private boolean percentile999th;

        public Builder count(boolean count) {
            this.count = count;
            return this;
        }

        public Builder max(boolean max) {
            this.max = max;
            return this;
        }

        public Builder mean(boolean mean) {
            this.mean = mean;
            return this;
        }

        public Builder min(boolean min) {
            this.min = min;
            return this;
        }

        public Builder stdDev(boolean stdDev) {
            this.stdDev = stdDev;
            return this;
        }

        public Builder percentile50th(boolean percentile50th) {
            this.percentile50th = percentile50th;
            return this;
        }

        public Builder percentile75th(boolean percentile75th) {
            this.percentile75th = percentile75th;
            return this;
        }

        public Builder percentile95th(boolean percentile95th) {
            this.percentile95th = percentile95th;
            return this;
        }

        public Builder percentile98th(boolean percentile98th) {
            this.percentile98th = percentile98th;
            return this;
        }

        public Builder percentile99th(boolean percentile99th) {
            this.percentile99th = percentile99th;
            return this;
        }

        public Builder percentile999th(boolean percentile999th) {
            this.percentile999th = percentile999th;
            return this;
        }

        public GraphiteHistogramProfile build() {
            if (!count && !max && !mean && !min && !stdDev
                    && !percentile50th && !percentile75th && !percentile95th && !percentile98th && !percentile99th && !percentile999th) {

                throw new IllegalStateException("Must enable at least one data point");
            }
            return new GraphiteHistogramProfile(this);
        }

    }

}
