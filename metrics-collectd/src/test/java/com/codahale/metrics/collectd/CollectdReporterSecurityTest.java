package com.codahale.metrics.collectd;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class CollectdReporterSecurityTest {

    private final MetricRegistry registry = new MetricRegistry();

    @Test
    public void testUnableSetSecurityLevelToSignWithoutUsername() {
        assertThatIllegalArgumentException().isThrownBy(()->
                CollectdReporter.forRegistry(registry)
                        .withHostName("eddie")
                        .withSecurityLevel(SecurityLevel.SIGN)
                        .withPassword("t1_g3r")
                        .build(new Sender("localhost", 25826)))
                .withMessage("username is required for securityLevel: SIGN");
    }

    @Test
    public void testUnableSetSecurityLevelToSignWithoutPassword() {
        assertThatIllegalArgumentException().isThrownBy(()->
                CollectdReporter.forRegistry(registry)
                        .withHostName("eddie")
                        .withSecurityLevel(SecurityLevel.SIGN)
                        .withUsername("scott")
                        .build(new Sender("localhost", 25826)))
                .withMessage("password is required for securityLevel: SIGN");
    }
}
