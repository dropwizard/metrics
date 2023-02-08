package io.dropwizard.metrics5.influxdb;

import io.dropwizard.metrics5.MetricAttribute;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InfluxDbLineBuilderAssumptionsTest {

    @Test
    void ensureMetricAttributeCodesAreSafeFieldKeys() {
        for (MetricAttribute ma : MetricAttribute.values()) {
            String code = ma.getCode();
            assertThat(code).doesNotContainPattern("[,= ]");
        }
    }
}
