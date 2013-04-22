package com.codahale.metrics;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.offset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SnapshotTest {
    private final Snapshot snapshot = new Snapshot(new long[]{5, 1, 2, 3, 4});

    @Test
    public void smallQuantilesAreTheFirstValue() throws Exception {
        assertThat(snapshot.getValue(0.0))
                .isEqualTo(1, offset(0.1));
    }

    @Test
    public void bigQuantilesAreTheLastValue() throws Exception {
        assertThat(snapshot.getValue(1.0))
                .isEqualTo(5, offset(0.1));
    }

    @Test
    public void hasAMedian() throws Exception {
        assertThat(snapshot.getMedian()).isEqualTo(3, offset(0.1));
    }

    @Test
    public void hasAp75() throws Exception {
        assertThat(snapshot.get75thPercentile()).isEqualTo(4.5, offset(0.1));
    }

    @Test
    public void hasAp95() throws Exception {
        assertThat(snapshot.get95thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasAp98() throws Exception {
        assertThat(snapshot.get98thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasAp99() throws Exception {
        assertThat(snapshot.get99thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasAp999() throws Exception {
        assertThat(snapshot.get999thPercentile()).isEqualTo(5.0, offset(0.1));
    }

    @Test
    public void hasValues() throws Exception {
        assertThat(snapshot.getValues())
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    public void hasASize() throws Exception {
        assertThat(snapshot.size())
                .isEqualTo(5);
    }

    @Test
    public void canAlsoBeCreatedFromACollectionOfLongs() throws Exception {
        final Snapshot other = new Snapshot(asList(5L, 1L, 2L, 3L, 4L));

        assertThat(other.getValues())
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    public void worksWithUnderestimatedCollections() throws Exception {
        final List<Long> longs = spy(new ArrayList<Long>());
        longs.add(5L);
        longs.add(1L);
        longs.add(2L);
        longs.add(3L);
        longs.add(4L);
        when(longs.size()).thenReturn(4, 5);

        final Snapshot other = new Snapshot(longs);

        assertThat(other.getValues())
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    public void worksWithOverestimatedCollections() throws Exception {
        final List<Long> longs = spy(new ArrayList<Long>());
        longs.add(5L);
        longs.add(1L);
        longs.add(2L);
        longs.add(3L);
        longs.add(4L);
        when(longs.size()).thenReturn(6, 5);

        final Snapshot other = new Snapshot(longs);

        assertThat(other.getValues())
                .containsOnly(1, 2, 3, 4, 5);
    }

    @Test
    public void dumpsToAStream() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        snapshot.dump(output);

        assertThat(output.toString())
                .isEqualTo(String.format("1%n2%n3%n4%n5%n"));
    }

    @Test
    public void calculatesTheMinimumValue() throws Exception {
        assertThat(snapshot.getMin())
                .isEqualTo(1);
    }

    @Test
    public void calculatesTheMaximumValue() throws Exception {
        assertThat(snapshot.getMax())
                .isEqualTo(5);
    }

    @Test
    public void calculatesTheMeanValue() throws Exception {
        assertThat(snapshot.getMean())
                .isEqualTo(3.0);
    }

    @Test
    public void calculatesTheStdDev() throws Exception {
        assertThat(snapshot.getStdDev())
                .isEqualTo(1.5811, offset(0.0001));
    }

    @Test
    public void calculatesAMinOfZeroForAnEmptySnapshot() throws Exception {
        final Snapshot snapshot = new Snapshot(new long[]{ });

        assertThat(snapshot.getMin())
                .isZero();
    }

    @Test
    public void calculatesAMaxOfZeroForAnEmptySnapshot() throws Exception {
        final Snapshot snapshot = new Snapshot(new long[]{ });

        assertThat(snapshot.getMax())
                .isZero();
    }

    @Test
    public void calculatesAMeanOfZeroForAnEmptySnapshot() throws Exception {
        final Snapshot snapshot = new Snapshot(new long[]{ });

        assertThat(snapshot.getMean())
                .isZero();
    }

    @Test
    public void calculatesAStdDevOfZeroForAnEmptySnapshot() throws Exception {
        final Snapshot snapshot = new Snapshot(new long[]{ });

        assertThat(snapshot.getStdDev())
                .isZero();
    }

    @Test
    public void calculatesAStdDevOfZeroForASingletonSnapshot() throws Exception {
        final Snapshot snapshot = new Snapshot(new long[]{ 1 });

        assertThat(snapshot.getStdDev())
                .isZero();
    }
}
