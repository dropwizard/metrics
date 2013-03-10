package com.yammer.metrics;

/**
 * The type of sampling the histogram should be performing.
 */
public enum SampleType {
    /**
     * Uses a uniform sample of 1028 elements, which offers a 99.9% confidence level with a 5%
     * margin of error assuming a normal distribution.
     */
    UNIFORM {
        @Override
        public Sample newSample() {
            return new UniformSample(DEFAULT_SAMPLE_SIZE);
        }
    },

    /**
     * Uses an exponentially decaying sample of 1028 elements, which offers a 99.9% confidence
     * level with a 5% margin of error assuming a normal distribution, and an alpha factor of
     * 0.015, which heavily biases the sample to the past 5 minutes of measurements.
     */
    BIASED {
        @Override
        public Sample newSample() {
            return new ExponentiallyDecayingSample(DEFAULT_SAMPLE_SIZE, DEFAULT_ALPHA);
        }
    };

    private static final int DEFAULT_SAMPLE_SIZE = 1028;
    private static final double DEFAULT_ALPHA = 0.015;

    public abstract Sample newSample();
}
