package io.dropwizard.metrics5.jvm;

import com.sun.management.UnixOperatingSystemMXBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("UnusedDeclaration")
class FileDescriptorRatioGaugeTest {
    private final UnixOperatingSystemMXBean os = mock(UnixOperatingSystemMXBean.class);

    private final FileDescriptorRatioGauge gauge = new FileDescriptorRatioGauge(os);

    @BeforeEach
    void setUp() throws Exception {
        when(os.getOpenFileDescriptorCount()).thenReturn(10L);
        when(os.getMaxFileDescriptorCount()).thenReturn(100L);
    }

    @Test
    void calculatesTheRatioOfUsedToTotalFileDescriptors() {
        Assertions.assertThat(gauge.getValue())
                .isEqualTo(0.1);
    }

    @Test
    void validateFileDescriptorRatioPresenceOnNixPlatforms() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        assumeTrue(osBean instanceof com.sun.management.UnixOperatingSystemMXBean);

        Assertions.assertThat(new FileDescriptorRatioGauge().getValue())
                .isGreaterThanOrEqualTo(0.0)
                .isLessThanOrEqualTo(1.0);
    }

    @Test
    void returnsNaNWhenTheInformationIsUnavailable() {
        Assertions.assertThat(new FileDescriptorRatioGauge(mock(OperatingSystemMXBean.class)).getValue())
                .isNaN();
    }
}
