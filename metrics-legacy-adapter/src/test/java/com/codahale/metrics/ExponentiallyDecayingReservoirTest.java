package com.codahale.metrics;

import org.assertj.core.data.Offset;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class ExponentiallyDecayingReservoirTest {

    @Test
    public void testCreateReservoir() {
        ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir();
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
        assertThat(snapshot.getStdDev()).isEqualTo(32.62, Offset.offset(0.1));
        assertThat(snapshot.get75thPercentile()).isEqualTo(190);
        assertThat(snapshot.get95thPercentile()).isEqualTo(200);
        assertThat(snapshot.get98thPercentile()).isEqualTo(200);
        assertThat(snapshot.get99thPercentile()).isEqualTo(200);
        assertThat(snapshot.get999thPercentile()).isEqualTo(200);
    }

    @Test
    public void testCreateReservoirWithCustomSizeAndAlpha() {
        ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(512, 0.01);
        reservoir.update(100);
        assertThat(reservoir.size()).isEqualTo(1);
    }


    @Test
    public void testCreateReservoirWithCustomSizeAlphaAndClock() {
        ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(512, 0.01,
                new Clock() {
                    @Override
                    public long getTick() {
                        return 24;
                    }
                });
        reservoir.update(100);
        assertThat(reservoir.size()).isEqualTo(1);
    }
}
