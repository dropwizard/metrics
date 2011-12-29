package com.yammer.metrics.util.tests;

import com.yammer.metrics.util.ToggleGauge;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ToggleGaugeTest {
    private final ToggleGauge toggle = new ToggleGauge();

    @Test
    public void returnsOneThenZero() throws Exception {
        assertThat(toggle.value(),
                   is(1));

        assertThat(toggle.value(),
                   is(0));

        assertThat(toggle.value(),
                   is(0));

        assertThat(toggle.value(),
                   is(0));
    }
}
