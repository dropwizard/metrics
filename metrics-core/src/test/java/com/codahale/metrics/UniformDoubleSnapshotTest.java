package com.codahale.metrics;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class UniformDoubleSnapshotTest {
    private final DoubleSnapshot DoubleSnapshot = new UniformDoubleSnapshot(new double[]{5, 1, 2, 3, 4});

    @Test
    public void smallQuantilesAreTheFirstValue() {
        assertThat(DoubleSnapshot.getValue(0.0))
                .isEqualTo(1, offset(0.1));
    }

    @Test
    public void bigQuantilesAreTheLastValue() {
        assertThat(DoubleSnapshot.getValue(1.0))
                .isEqualTo(5, offset(0.1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowsNotANumberQuantile() {
        DoubleSnapshot.getValue(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowsNegativeQuantile() {
        DoubleSnapshot.getValue(-0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowsQuantileOverOne() {
        DoubleSnapshot.getValue(1.5);
    }

    @Test
    public void hasAMedian() {
        assertThat(DoubleSnapshot.getMedian()).isEqualTo(3, offset(0.1));
    }

    @Test
    public void hasAp75() {
        assertThat(DoubleSnapshot.get75thPercentile()).isEqualTo(4.5, offset(0.1));
    }

    @Test
    public void hasAp95() {
        assertThat(DoubleSnapshot.get95thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasAp98() {
        assertThat(DoubleSnapshot.get98thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasAp99() {
        assertThat(DoubleSnapshot.get99thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasAp999() {
        assertThat(DoubleSnapshot.get999thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasValues() {
        assertThat(DoubleSnapshot.getValues())
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    public void hasASize() {
        assertThat(DoubleSnapshot.size())
                .isEqualTo(5);
    }

    @Test
    public void canAlsoBeCreatedFromACollectionOfLongs() {
        final DoubleSnapshot other = new UniformDoubleSnapshot(asList(5D, 1D, 2D, 3D, 4D));

        assertThat(other.getValues())
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    public void correctlyCreatedFromCollectionWithWeakIterator() throws Exception {
        final ConcurrentSkipListSet<Double> values = new ConcurrentSkipListSet<>();

        // Create a latch to make sure that the background thread has started and
        // pushed some data to the collection.
        final CountDownLatch latch = new CountDownLatch(10);
        final Thread backgroundThread = new Thread(() -> {
            final Random random = new Random();
            // Update the collection in the loop to trigger a potential `ArrayOutOfBoundException`
            // and verify that the DoubleSnapshot doesn't make assumptions about the size of the iterator.
            while (!Thread.currentThread().isInterrupted()) {
                values.add(random.nextDouble());
                latch.countDown();
            }
        });
        backgroundThread.start();

        try {
            latch.await(5, TimeUnit.SECONDS);
            assertThat(latch.getCount()).isEqualTo(0);

            // Create a DoubleSnapshot while the  collection is being updated.
            final DoubleSnapshot DoubleSnapshot = new UniformDoubleSnapshot(values);
            assertThat(DoubleSnapshot.getValues().length).isGreaterThanOrEqualTo(10);
        } finally {
            backgroundThread.interrupt();
        }
    }

    @Test
    public void dumpsToAStream() {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        DoubleSnapshot.dump(output);

        assertThat(output.toString())
                .isEqualTo(String.format("1.000000%n2.000000%n3.000000%n4.000000%n5.000000%n"));
    }

    @Test
    public void calculatesTheMinimumValue() {
        assertThat(DoubleSnapshot.getMin())
                .isEqualTo(1);
    }

    @Test
    public void calculatesTheMaximumValue() {
        assertThat(DoubleSnapshot.getMax())
                .isEqualTo(5);
    }

    @Test
    public void calculatesTheMeanValue() {
        assertThat(DoubleSnapshot.getMean())
                .isEqualTo(3.0);
    }

    @Test
    public void calculatesTheStdDev() {
        assertThat(DoubleSnapshot.getStdDev())
                .isEqualTo(1.5811, offset(0.0001));
    }

    @Test
    public void calculatesAMinOfZeroForAnEmptyDoubleSnapshot() {
        final DoubleSnapshot emptyDoubleSnapshot = new UniformDoubleSnapshot(new double[0]);

        assertThat(emptyDoubleSnapshot.getMin())
                .isZero();
    }

    @Test
    public void calculatesAMaxOfZeroForAnEmptyDoubleSnapshot() {
        final DoubleSnapshot emptyDoubleSnapshot = new UniformDoubleSnapshot(new double[0]);

        assertThat(emptyDoubleSnapshot.getMax())
                .isZero();
    }

    @Test
    public void calculatesAMeanOfZeroForAnEmptyDoubleSnapshot() {
        final DoubleSnapshot emptyDoubleSnapshot = new UniformDoubleSnapshot(new double[0]);

        assertThat(emptyDoubleSnapshot.getMean())
                .isZero();
    }

    @Test
    public void calculatesAStdDevOfZeroForAnEmptyDoubleSnapshot() {
        final DoubleSnapshot emptyDoubleSnapshot = new UniformDoubleSnapshot(new double[0]);

        assertThat(emptyDoubleSnapshot.getStdDev())
                .isZero();
    }

    @Test
    public void calculatesAStdDevOfZeroForASingletonDoubleSnapshot() {
        final DoubleSnapshot singleItemDoubleSnapshot = new UniformDoubleSnapshot(new double[0]);

        assertThat(singleItemDoubleSnapshot.getStdDev())
                .isZero();
    }
}
