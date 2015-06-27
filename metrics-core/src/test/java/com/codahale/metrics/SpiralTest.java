package com.codahale.metrics;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class SpiralTest {
    private long val = 0;
    private final Clock clock = new Clock() {
		    // just return the given Time val
        @Override
        public long getTime() {
            return val;
        }
        @Override
        public long getTick() { return 0; }
    };
    private final Spiral spiral = new Spiral(60*1000L, clock);

    @Test
    public void test() throws Exception {
        assertThat(spiral.getValue()).isEqualTo(0);
        for (int i=0;i<30;i++) {
            val = (i*1000);
            spiral.mark();
        }
        val = (60 * 1000L);
        assertThat(spiral.getValue()).isEqualTo(30);
        val =(65 * 1000L);
        assertThat(spiral.getValue()).isEqualTo(25);
        for (int i=30;i<60;i++) {
            val = (i*1010);
            spiral.mark();
        }
        val = (65 * 1000L);
        assertThat(spiral.getValue()).isEqualTo(55);
        val = (100 * 1000L);
        assertThat(spiral.getValue()).isEqualTo(20);
        val = (120 * 1000L);
        assertThat(spiral.getValue()).isEqualTo(0);
    }
}
