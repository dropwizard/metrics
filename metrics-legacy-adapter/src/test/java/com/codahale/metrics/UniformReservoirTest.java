package com.codahale.metrics;

import org.assertj.core.data.Offset;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class UniformReservoirTest {

    @Test
    public void testCreateReservoir() {
        UniformReservoir reservoir = new UniformReservoir();
        reservoir.update(120);
        reservoir.update(190);
        reservoir.update(200);
        reservoir.update(130);
        reservoir.update(140);

        Snapshot snapshot = reservoir.getSnapshot();
        assertThat(snapshot.size()).isEqualTo(5);
        assertThat(snapshot.getValues()).contains(120, 130, 140, 190, 200);
        assertThat(snapshot.getMin()).isEqualTo(120);
        assertThat(snapshot.getMax()).isEqualTo(200);
        assertThat(snapshot.getStdDev()).isEqualTo(36.47, Offset.offset(0.1));
        assertThat(snapshot.get75thPercentile()).isEqualTo(195);
        assertThat(snapshot.get95thPercentile()).isEqualTo(200);
        assertThat(snapshot.get98thPercentile()).isEqualTo(200);
        assertThat(snapshot.get99thPercentile()).isEqualTo(200);
        assertThat(snapshot.get999thPercentile()).isEqualTo(200);
    }

    @Test
    public void testCreateReservoirWithCustomSize() {
        UniformReservoir reservoir = new UniformReservoir(128);
        reservoir.update(440);
        reservoir.update(250);
        reservoir.update(380);

        assertThat(reservoir.size()).isEqualTo(3);
    }
}
