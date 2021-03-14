package com.codahale.metrics;

import com.codahale.metrics.WeightedDoubleSnapshot.WeightedDoubleSample;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class WeightedDoubleSnapshotTest {

    private static List<WeightedDoubleSample> weightedArray(long[] values, double[] weights) {
        if (values.length != weights.length) {
            throw new IllegalArgumentException("Mismatched lengths: " + values.length + " vs " + weights.length);
        }

        final List<WeightedDoubleSample> samples = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            samples.add(new WeightedDoubleSample(values[i], weights[i]));
        }

        return samples;
    }

    private final DoubleSnapshot snapshot = new WeightedDoubleSnapshot(
            weightedArray(new long[]{5, 1, 2, 3, 4}, new double[]{1, 2, 3, 2, 2}));

    @Test
    public void smallQuantilesAreTheFirstValue() {
        assertThat(snapshot.getValue(0.0))
                .isEqualTo(1.0, offset(0.1));
    }

    @Test
    public void bigQuantilesAreTheLastValue() {
        assertThat(snapshot.getValue(1.0))
                .isEqualTo(5.0, offset(0.1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowsNotANumberQuantile() {
        snapshot.getValue(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowsNegativeQuantile() {
        snapshot.getValue(-0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowsQuantileOverOne() {
        snapshot.getValue(1.5);
    }

    @Test
    public void hasAMedian() {
        assertThat(snapshot.getMedian()).isEqualTo(3.0, offset(0.1));
    }

    @Test
    public void hasAp75() {
        assertThat(snapshot.get75thPercentile()).isEqualTo(4.0, offset(0.1));
    }

    @Test
    public void hasAp95() {
        assertThat(snapshot.get95thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasAp98() {
        assertThat(snapshot.get98thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasAp99() {
        assertThat(snapshot.get99thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasAp999() {
        assertThat(snapshot.get999thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasValues() {
        assertThat(snapshot.getValues())
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    public void hasASize() {
        assertThat(snapshot.size())
                .isEqualTo(5);
    }

    @Test
    public void worksWithUnderestimatedCollections() {
        final List<WeightedDoubleSample> items = spy(weightedArray(new long[]{5, 1, 2, 3, 4}, new double[]{1, 2, 3, 2, 2}));
        when(items.size()).thenReturn(4, 5);

        final DoubleSnapshot other = new WeightedDoubleSnapshot(items);

        assertThat(other.getValues())
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    public void worksWithOverestimatedCollections() {
        final List<WeightedDoubleSample> items = spy(weightedArray(new long[]{5, 1, 2, 3, 4}, new double[]{1, 2, 3, 2, 2}));
        when(items.size()).thenReturn(6, 5);

        final DoubleSnapshot other = new WeightedDoubleSnapshot(items);

        assertThat(other.getValues())
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    public void dumpsToAStream() {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        snapshot.dump(output);

        assertThat(output.toString())
                .isEqualTo(String.format("1.000000%n2.000000%n3.000000%n4.000000%n5.000000%n"));
    }

    @Test
    public void calculatesTheMinimumValue() {
        assertThat(snapshot.getMin())
                .isEqualTo(1);
    }

    @Test
    public void calculatesTheMaximumValue() {
        assertThat(snapshot.getMax())
                .isEqualTo(5);
    }

    @Test
    public void calculatesTheMeanValue() {
        assertThat(snapshot.getMean())
                .isEqualTo(2.7);
    }

    @Test
    public void calculatesTheStdDev() {
        assertThat(snapshot.getStdDev())
                .isEqualTo(1.2688, offset(0.0001));
    }

    @Test
    public void calculatesAMinOfZeroForAnEmptySnapshot() {
        final DoubleSnapshot emptySnapshot = new WeightedDoubleSnapshot(
                weightedArray(new long[]{}, new double[]{}));

        assertThat(emptySnapshot.getMin())
                .isZero();
    }

    @Test
    public void calculatesAMaxOfZeroForAnEmptySnapshot() {
        final DoubleSnapshot emptySnapshot = new WeightedDoubleSnapshot(
                weightedArray(new long[]{}, new double[]{}));

        assertThat(emptySnapshot.getMax())
                .isZero();
    }

    @Test
    public void calculatesAMeanOfZeroForAnEmptySnapshot() {
        final DoubleSnapshot emptySnapshot = new WeightedDoubleSnapshot(
                weightedArray(new long[]{}, new double[]{}));

        assertThat(emptySnapshot.getMean())
                .isZero();
    }

    @Test
    public void calculatesAStdDevOfZeroForAnEmptySnapshot() {
        final DoubleSnapshot emptySnapshot = new WeightedDoubleSnapshot(
                weightedArray(new long[]{}, new double[]{}));

        assertThat(emptySnapshot.getStdDev())
                .isZero();
    }

    @Test
    public void calculatesAStdDevOfZeroForASingletonSnapshot() {
        final DoubleSnapshot singleItemSnapshot = new WeightedDoubleSnapshot(
                weightedArray(new long[]{1}, new double[]{1.0}));

        assertThat(singleItemSnapshot.getStdDev())
                .isZero();
    }

    @Test
    public void expectNoOverflowForLowWeights() {
        final DoubleSnapshot scatteredSnapshot = new WeightedDoubleSnapshot(
                weightedArray(
                        new long[]{1, 2, 3},
                        new double[]{Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE}
                )
        );

        assertThat(scatteredSnapshot.getMean())
                .isEqualTo(2);
    }

    @Test
    public void doesNotProduceNaNValues() {
        WeightedDoubleSnapshot WeightedDoubleSnapshot = new WeightedDoubleSnapshot(
                weightedArray(new long[]{1, 2, 3}, new double[]{0, 0, 0}));
        assertThat(WeightedDoubleSnapshot.getMean()).isEqualTo(0);
    }

}
