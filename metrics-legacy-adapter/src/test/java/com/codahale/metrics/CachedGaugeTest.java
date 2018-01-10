package com.codahale.metrics;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class CachedGaugeTest {

    @Test
    public void testCreate() {
        CachedGauge<String> cachedGauge = new CachedGauge<String>(100, TimeUnit.MILLISECONDS) {
            @Override
            protected String loadValue() {
                return "heavyValue";
            }
        };
        assertThat(cachedGauge.getValue()).isEqualTo("heavyValue");
    }

    @Test
    public void testCreateWothClock() {
        CachedGauge<String> cachedGauge = new CachedGauge<String>(new Clock.UserTimeClock(), 100,
                TimeUnit.MILLISECONDS) {
            @Override
            protected String loadValue() {
                return "heavyValue";
            }
        };
        assertThat(cachedGauge.getValue()).isEqualTo("heavyValue");
    }
}
