package com.codahale.metrics;

import org.assertj.core.data.Offset;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class HistogramTest {

    @Test
    public void testCreate() {
        Histogram histogram = new Histogram(new ExponentiallyDecayingReservoir());
        histogram.update(120);
        histogram.update(190);
        histogram.update(200L);
        histogram.update(130);
        histogram.update(140);

        assertThat(histogram.getCount()).isEqualTo(5);
        Snapshot snapshot = histogram.getSnapshot();
        assertThat(snapshot.size()).isEqualTo(5);
        assertThat(snapshot.getValues()).contains(120, 130, 140, 190, 200);
        assertThat(snapshot.getMin()).isEqualTo(120);
        assertThat(snapshot.getMax()).isEqualTo(200);
        assertThat(snapshot.getStdDev()).isEqualTo(32.62, Offset.offset(0.1));
        assertThat(snapshot.get75thPercentile()).isEqualTo(190);
        assertThat(snapshot.get95thPercentile()).isEqualTo(200);
        assertThat(snapshot.get98thPercentile()).isEqualTo(200);
        assertThat(snapshot.get99thPercentile()).isEqualTo(200);
        assertThat(snapshot.get999thPercentile()).isEqualTo(200);
    }
}
