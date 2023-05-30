package io.dropwizard.metrics5;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassMetadataTest {
    @Test
    public void testParameterMetadataIsAvailable() throws NoSuchMethodException {
        assertThat(DefaultSettableGauge.class.getConstructor(Object.class).getParameters())
                .allSatisfy(parameter -> assertThat(parameter.isNamePresent()).isTrue());
    }
}