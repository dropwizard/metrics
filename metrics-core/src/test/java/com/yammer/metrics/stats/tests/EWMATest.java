package com.yammer.metrics.stats.tests;

import com.yammer.metrics.stats.EWMA;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EWMATest {
    @Test
    public void aOneMinuteEWMAWithAValueOfThree() throws Exception {
        final EWMA ewma = EWMA.oneMinuteEWMA();
        ewma.update(3);
        ewma.tick();

        assertThat("the EWMA has a rate of 0.6 events/sec after the first tick",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.6, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.22072766 events/sec after 1 minute",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.22072766, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.08120117 events/sec after 2 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.08120117, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.02987224 events/sec after 3 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.02987224, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.01098938 events/sec after 4 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.01098938, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.00404277 events/sec after 5 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.00404277, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.00148725 events/sec after 6 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.00148725, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.00054713 events/sec after 7 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.00054713, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.00020128 events/sec after 8 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.00020128, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.00007405 events/sec after 9 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.00007405, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.00002724 events/sec after 10 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.00002724, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.00001002 events/sec after 11 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.00001002, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.00000369 events/sec after 12 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.00000369, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.00000136 events/sec after 13 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.00000136, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.00000050 events/sec after 14 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.00000050, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.00000018 events/sec after 15 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.00000018, 0.000001)));
    }

    @Test
    public void aFiveMinuteEWMAWithAValueOfThree() throws Exception {
        final EWMA ewma = EWMA.fiveMinuteEWMA();
        ewma.update(3);
        ewma.tick();

        assertThat("the EWMA has a rate of 0.6 events/sec after the first tick",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.6, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.49123845 events/sec after 1 minute",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.49123845, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.40219203 events/sec after 2 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.40219203, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.32928698 events/sec after 3 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.32928698, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.26959738 events/sec after 4 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.26959738, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.22072766 events/sec after 5 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.22072766, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.18071653 events/sec after 6 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.18071653, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.14795818 events/sec after 7 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.14795818, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.12113791 events/sec after 8 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.12113791, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.09917933 events/sec after 9 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.09917933, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.08120117 events/sec after 10 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.08120117, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.06648190 events/sec after 11 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.06648190, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.05443077 events/sec after 12 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.05443077, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.04456415 events/sec after 13 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.04456415, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.03648604 events/sec after 14 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.03648604, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.02987224 events/sec after 15 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.02987224, 0.000001)));
    }

    @Test
    public void aFifteenMinuteEWMAWithAValueOfThree() throws Exception {
        final EWMA ewma = EWMA.fifteenMinuteEWMA();
        ewma.update(3);
        ewma.tick();

        assertThat("the EWMA has a rate of 0.6 events/sec after the first tick",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.6, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.56130419 events/sec after 1 minute",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.56130419, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.52510399 events/sec after 2 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.52510399, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.49123845 events/sec after 3 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.49123845, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.45955700 events/sec after 4 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.45955700, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.42991879 events/sec after 5 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.42991879, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.40219203 events/sec after 6 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.40219203, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.37625345 events/sec after 7 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.37625345, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.35198773 events/sec after 8 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.35198773, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.32928698 events/sec after 9 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.32928698, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.30805027 events/sec after 10 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.30805027, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.28818318 events/sec after 11 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.28818318, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.26959738 events/sec after 12 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.26959738, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.25221023 events/sec after 13 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.25221023, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.23594443 events/sec after 14 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.23594443, 0.000001)));

        elapseMinute(ewma);

        assertThat("the EWMA has a rate of 0.22072766 events/sec after 15 minutes",
                   ewma.rate(TimeUnit.SECONDS),
                   is(closeTo(0.22072766, 0.000001)));
    }


    private void elapseMinute(EWMA ewma) {
        for (int i = 1; i <= 12; i++) {
            ewma.tick();
        }
    }
}
