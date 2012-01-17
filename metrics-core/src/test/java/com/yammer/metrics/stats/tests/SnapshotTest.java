package com.yammer.metrics.stats.tests;

import com.yammer.metrics.stats.Snapshot;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SnapshotTest {
    private final Snapshot snapshot = new Snapshot(new double[]{5, 1, 2, 3, 4});

    @Test
    public void smallQuantilesAreTheFirstValue() throws Exception {
        assertThat(snapshot.getValue(0.0),
                   is(closeTo(1, 0.1)));
    }

    @Test
    public void bigQuantilesAreTheLastValue() throws Exception {
        assertThat(snapshot.getValue(1.0),
                   is(closeTo(5, 0.1)));
    }

    @Test
    public void hasAMedian() throws Exception {
        assertThat(snapshot.getMedian(),
                   is(closeTo(3, 0.1)));
    }

    @Test
    public void hasAp75() throws Exception {
        assertThat(snapshot.get75thPercentile(),
                   is(closeTo(4.5, 0.1)));
    }

    @Test
    public void hasAp95() throws Exception {
        assertThat(snapshot.get95thPercentile(),
                   is(closeTo(5.0, 0.1)));
    }

    @Test
    public void hasAp98() throws Exception {
        assertThat(snapshot.get98thPercentile(),
                   is(closeTo(5.0, 0.1)));
    }

    @Test
    public void hasAp99() throws Exception {
        assertThat(snapshot.get99thPercentile(),
                   is(closeTo(5.0, 0.1)));
    }

    @Test
    public void hasAp999() throws Exception {
        assertThat(snapshot.get999thPercentile(),
                   is(closeTo(5.0, 0.1)));
    }

    @Test
    public void hasValues() throws Exception {
        assertThat(snapshot.getValues(),
                   is(new double[]{1, 2, 3, 4, 5}));
    }

    @Test
    public void hasASize() throws Exception {
        assertThat(snapshot.size(),
                   is(5));
    }

    @Test
    public void canAlsoBeCreatedFromACollectionOfLongs() throws Exception {
        final Snapshot other = new Snapshot(asList(5L, 1L, 2L, 3L, 4L));

        assertThat(other.getValues(),
                   is(new double[]{1.0, 2.0, 3.0, 4.0, 5.0}));
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

        assertThat(other.getValues(),
                   is(new double[]{ 1.0, 2.0, 3.0, 4.0, 5.0 }));
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

        assertThat(other.getValues(),
                   is(new double[]{ 1.0, 2.0, 3.0, 4.0, 5.0 }));
    }
}
