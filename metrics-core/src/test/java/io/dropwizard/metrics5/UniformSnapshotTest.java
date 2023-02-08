package io.dropwizard.metrics5;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UniformSnapshotTest {
    private final Snapshot snapshot = new UniformSnapshot(new long[]{5, 1, 2, 3, 4});

    @Test
    void smallQuantilesAreTheFirstValue() {
        assertThat(snapshot.getValue(0.0))
                .isEqualTo(1, offset(0.1));
    }

    @Test
    void bigQuantilesAreTheLastValue() {
        assertThat(snapshot.getValue(1.0))
                .isEqualTo(5, offset(0.1));
    }

    @Test
    void disallowsNotANumberQuantile() {
        assertThrows(IllegalArgumentException.class, () -> {
            snapshot.getValue(Double.NaN);
        });
    }

    @Test
    void disallowsNegativeQuantile() {
        assertThrows(IllegalArgumentException.class, () -> {
            snapshot.getValue(-0.5);
        });
    }

    @Test
    void disallowsQuantileOverOne() {
        assertThrows(IllegalArgumentException.class, () -> {
            snapshot.getValue(1.5);
        });
    }

    @Test
    void hasAMedian() {
        assertThat(snapshot.getMedian()).isEqualTo(3, offset(0.1));
    }

    @Test
    void hasAp75() {
        assertThat(snapshot.get75thPercentile()).isEqualTo(4.5, offset(0.1));
    }

    @Test
    void hasAp95() {
        assertThat(snapshot.get95thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    void hasAp98() {
        assertThat(snapshot.get98thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    void hasAp99() {
        assertThat(snapshot.get99thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    void hasAp999() {
        assertThat(snapshot.get999thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    void hasValues() {
        assertThat(snapshot.getValues())
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    void hasASize() {
        assertThat(snapshot.size())
                .isEqualTo(5);
    }

    @Test
    void canAlsoBeCreatedFromACollectionOfLongs() {
        final Snapshot other = new UniformSnapshot(asList(5L, 1L, 2L, 3L, 4L));

        assertThat(other.getValues())
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    void correctlyCreatedFromCollectionWithWeakIterator() throws Exception {
        final ConcurrentSkipListSet<Long> values = new ConcurrentSkipListSet<>();

        // Create a latch to make sure that the background thread has started and
        // pushed some data to the collection.
        final CountDownLatch latch = new CountDownLatch(10);
        final Thread backgroundThread = new Thread(() -> {
            final Random random = new Random();
            // Update the collection in the loop to trigger a potential `ArrayOutOfBoundException`
            // and verify that the snapshot doesn't make assumptions about the size of the iterator.
            while (!Thread.currentThread().isInterrupted()) {
                values.add(random.nextLong());
                latch.countDown();
            }
        });
        backgroundThread.start();

        try {
            latch.await(5, TimeUnit.SECONDS);
            assertThat(latch.getCount()).isEqualTo(0);

            // Create a snapshot while the  collection is being updated.
            final Snapshot snapshot = new UniformSnapshot(values);
            assertThat(snapshot.getValues().length).isGreaterThanOrEqualTo(10);
        } finally {
            backgroundThread.interrupt();
        }
    }

    @Test
    void dumpsToAStream() {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        snapshot.dump(output);

        assertThat(output.toString())
                .isEqualTo(String.format("1%n2%n3%n4%n5%n"));
    }

    @Test
    void calculatesTheMinimumValue() {
        assertThat(snapshot.getMin())
                .isEqualTo(1);
    }

    @Test
    void calculatesTheMaximumValue() {
        assertThat(snapshot.getMax())
                .isEqualTo(5);
    }

    @Test
    void calculatesTheMeanValue() {
        assertThat(snapshot.getMean())
                .isEqualTo(3.0);
    }

    @Test
    void calculatesTheStdDev() {
        assertThat(snapshot.getStdDev())
                .isEqualTo(1.5811, offset(0.0001));
    }

    @Test
    void calculatesAMinOfZeroForAnEmptySnapshot() {
        final Snapshot emptySnapshot = new UniformSnapshot(new long[]{});

        assertThat(emptySnapshot.getMin())
                .isZero();
    }

    @Test
    void calculatesAMaxOfZeroForAnEmptySnapshot() {
        final Snapshot emptySnapshot = new UniformSnapshot(new long[]{});

        assertThat(emptySnapshot.getMax())
                .isZero();
    }

    @Test
    void calculatesAMeanOfZeroForAnEmptySnapshot() {
        final Snapshot emptySnapshot = new UniformSnapshot(new long[]{});

        assertThat(emptySnapshot.getMean())
                .isZero();
    }

    @Test
    void calculatesAStdDevOfZeroForAnEmptySnapshot() {
        final Snapshot emptySnapshot = new UniformSnapshot(new long[]{});

        assertThat(emptySnapshot.getStdDev())
                .isZero();
    }

    @Test
    void calculatesAStdDevOfZeroForASingletonSnapshot() {
        final Snapshot singleItemSnapshot = new UniformSnapshot(new long[]{1});

        assertThat(singleItemSnapshot.getStdDev())
                .isZero();
    }
}
