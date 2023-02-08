package io.dropwizard.metrics5;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class ClockTest {

    @Test
    void userTimeClock() {
        final Clock.UserTimeClock clock = new Clock.UserTimeClock();

        assertThat((double) clock.getTime())
                .isEqualTo(System.currentTimeMillis(),
                        offset(100.0));

        assertThat((double) clock.getTick())
                .isEqualTo(System.nanoTime(),
                        offset(1000000.0));
    }

    @Test
    void defaultsToUserTime() {
        assertThat(Clock.defaultClock())
                .isInstanceOf(Clock.UserTimeClock.class);
    }
}
