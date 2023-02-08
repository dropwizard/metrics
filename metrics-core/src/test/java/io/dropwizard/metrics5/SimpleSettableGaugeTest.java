package io.dropwizard.metrics5;

import io.dropwizard.metrics5.DefaultSettableGauge;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleSettableGaugeTest {

    @Test
    void defaultValue() {
        DefaultSettableGauge<Integer> settable = new DefaultSettableGauge<>(1);

        assertThat(settable.getValue()).isEqualTo(1);
    }

    @Test
    void setValueAndThenGetValue() {
        DefaultSettableGauge<String> settable = new DefaultSettableGauge<>("default");

        settable.setValue("first");
        assertThat(settable.getValue())
                .isEqualTo("first");

        settable.setValue("second");
        assertThat(settable.getValue())
                .isEqualTo("second");
    }
}
