package io.dropwizard.metrics5.collectd;

import io.dropwizard.metrics5.MetricRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CollectdReporterSecurityTest {

    private final MetricRegistry registry = new MetricRegistry();

    @Test
    void testUnableSetSecurityLevelToSignWithoutUsername() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                CollectdReporter.forRegistry(registry)
                        .withHostName("eddie")
                        .withSecurityLevel(SecurityLevel.SIGN)
                        .withPassword("t1_g3r")
                        .build(new Sender("localhost", 25826)))
                .withMessage("username is required for securityLevel: SIGN");
    }

    @Test
    void testUnableSetSecurityLevelToSignWithoutPassword() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                CollectdReporter.forRegistry(registry)
                        .withHostName("eddie")
                        .withSecurityLevel(SecurityLevel.SIGN)
                        .withUsername("scott")
                        .build(new Sender("localhost", 25826)))
                .withMessage("password is required for securityLevel: SIGN");
    }
}
