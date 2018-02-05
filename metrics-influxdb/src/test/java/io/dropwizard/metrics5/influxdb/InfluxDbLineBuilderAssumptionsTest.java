package io.dropwizard.metrics5.influxdb;

import io.dropwizard.metrics5.MetricAttribute;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InfluxDbLineBuilderAssumptionsTest {

    @Test
    public void ensureMetricAttributeCodesAreSafeFieldKeys() {
        for (MetricAttribute ma : MetricAttribute.values()) {
            String code = ma.getCode();
            assertThat(code).doesNotContainPattern("[,= ]");
        }
    }
}
