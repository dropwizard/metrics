package com.yammer.metrics.stats.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.stats.EWMA
import java.util.concurrent.TimeUnit

class EWMASpec extends Spec {
  protected def markMinutes(minutes: Int)(implicit ewma: EWMA) {
    for (i <- 1 to TimeUnit.MINUTES.toSeconds(minutes).asInstanceOf[Int] / 5) {
      ewma.tick()
    }
  }

  class `A 1min EWMA with a value of 3` {
    implicit val ewma = EWMA.oneMinuteEWMA
    ewma.update(3)
    ewma.tick()

    @test def `has a rate of 0.6 events/sec after the first tick` = {
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.6, 0.000001)
    }

    @test def `has a rate of 0.22072766 events/sec after 1 minute` = {
      markMinutes(1)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.22072766, 0.00000001)
    }

    @test def `has a rate of 0.08120117 events/sec after 2 minutes` = {
      markMinutes(2)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.08120117, 0.00000001)
    }

    @test def `has a rate of 0.02987224 events/sec after 3 minutes` = {
      markMinutes(3)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.02987224, 0.00000001)
    }

    @test def `has a rate of 0.01098938 events/sec after 4 minutes` = {
      markMinutes(4)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.01098938, 0.00000001)
    }

    @test def `has a rate of 0.00404277 events/sec after 5 minutes` = {
      markMinutes(5)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.00404277, 0.00000001)
    }

    @test def `has a rate of 0.00148725 events/sec after 6 minutes` = {
      markMinutes(6)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.00148725, 0.00000001)
    }

    @test def `has a rate of 0.00054713 events/sec after 7 minutes` = {
      markMinutes(7)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.00054713, 0.00000001)
    }

    @test def `has a rate of 0.00020128 events/sec after 8 minutes` = {
      markMinutes(8)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.00020128, 0.00000001)
    }

    @test def `has a rate of 0.00007405 events/sec after 9 minutes` = {
      markMinutes(9)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.00007405, 0.00000001)
    }

    @test def `has a rate of 0.00002724 events/sec after 10 minutes` = {
      markMinutes(10)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.00002724, 0.00000001)
    }

    @test def `has a rate of 0.00001002 events/sec after 11 minutes` = {
      markMinutes(11)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.00001002, 0.00000001)
    }

    @test def `has a rate of 0.00000369 events/sec after 12 minutes` = {
      markMinutes(12)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.00000369, 0.00000001)
    }

    @test def `has a rate of 0.00000136 events/sec after 13 minutes` = {
      markMinutes(13)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.00000136, 0.00000001)
    }

    @test def `has a rate of 0.00000050 events/sec after 14 minutes` = {
      markMinutes(14)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.00000050, 0.00000001)
    }

    @test def `has a rate of 0.00000018 events/sec after 15 minutes` = {
      markMinutes(15)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.00000018, 0.00000001)
    }
  }

  class `A 5min EWMA with a value of 3` {
    implicit val ewma = EWMA.fiveMinuteEWMA
    ewma.update(3)
    ewma.tick()

    @test def `has a rate of 0.6 events/sec after the first tick` = {
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.6, 0.000001)
    }

    @test def `has a rate of 0.49123845 events/sec after 1 minute` = {
      markMinutes(1)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.49123845, 0.00000001)
    }

    @test def `has a rate of 0.40219203 events/sec after 2 minutes` = {
      markMinutes(2)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.40219203, 0.00000001)
    }

    @test def `has a rate of 0.32928698 events/sec after 3 minutes` = {
      markMinutes(3)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.32928698, 0.00000001)
    }

    @test def `has a rate of 0.26959738 events/sec after 4 minutes` = {
      markMinutes(4)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.26959738, 0.00000001)
    }

    @test def `has a rate of 0.22072766 events/sec after 5 minutes` = {
      markMinutes(5)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.22072766, 0.00000001)
    }

    @test def `has a rate of 0.18071653 events/sec after 6 minutes` = {
      markMinutes(6)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.18071653, 0.00000001)
    }

    @test def `has a rate of 0.14795818 events/sec after 7 minutes` = {
      markMinutes(7)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.14795818, 0.00000001)
    }

    @test def `has a rate of 0.12113791 events/sec after 8 minutes` = {
      markMinutes(8)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.12113791, 0.00000001)
    }

    @test def `has a rate of 0.09917933 events/sec after 9 minutes` = {
      markMinutes(9)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.09917933, 0.00000001)
    }

    @test def `has a rate of 0.08120117 events/sec after 10 minutes` = {
      markMinutes(10)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.08120117, 0.00000001)
    }

    @test def `has a rate of 0.06648190 events/sec after 11 minutes` = {
      markMinutes(11)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.06648190, 0.00000001)
    }

    @test def `has a rate of 0.05443077 events/sec after 12 minutes` = {
      markMinutes(12)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.05443077, 0.00000001)
    }

    @test def `has a rate of 0.04456415 events/sec after 13 minutes` = {
      markMinutes(13)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.04456415, 0.00000001)
    }

    @test def `has a rate of 0.03648604 events/sec after 14 minutes` = {
      markMinutes(14)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.03648604, 0.00000001)
    }

    @test def `has a rate of 0.02987224 events/sec after 15 minutes` = {
      markMinutes(15)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.02987224, 0.00000001)
    }
  }

  class `A 15min EWMA with a value of 3` {
    implicit val ewma = EWMA.fifteenMinuteEWMA
    ewma.update(3)
    ewma.tick()

    @test def `has a rate of 0.6 events/sec after the first tick` = {
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.6, 0.000001)
    }

    @test def `has a rate of 0.56130419 events/sec after 1 minute` = {
      markMinutes(1)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.56130419, 0.00000001)
    }

    @test def `has a rate of 0.52510399 events/sec after 2 minutes` = {
      markMinutes(2)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.52510399, 0.00000001)
    }

    @test def `has a rate of 0.49123845 events/sec after 3 minutes` = {
      markMinutes(3)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.49123845, 0.00000001)
    }

    @test def `has a rate of 0.45955700 events/sec after 4 minutes` = {
      markMinutes(4)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.45955700, 0.00000001)
    }

    @test def `has a rate of 0.42991879 events/sec after 5 minutes` = {
      markMinutes(5)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.42991879, 0.00000001)
    }

    @test def `has a rate of 0.40219203 events/sec after 6 minutes` = {
      markMinutes(6)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.40219203, 0.00000001)
    }

    @test def `has a rate of 0.37625345 events/sec after 7 minutes` = {
      markMinutes(7)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.37625345, 0.00000001)
    }

    @test def `has a rate of 0.35198773 events/sec after 8 minutes` = {
      markMinutes(8)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.35198773, 0.00000001)
    }

    @test def `has a rate of 0.32928698 events/sec after 9 minutes` = {
      markMinutes(9)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.32928698, 0.00000001)
    }

    @test def `has a rate of 0.30805027 events/sec after 10 minutes` = {
      markMinutes(10)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.30805027, 0.00000001)
    }

    @test def `has a rate of 0.28818318 events/sec after 11 minutes` = {
      markMinutes(11)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.28818318, 0.00000001)
    }

    @test def `has a rate of 0.26959738 events/sec after 12 minutes` = {
      markMinutes(12)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.26959738, 0.00000001)
    }

    @test def `has a rate of 0.25221023 events/sec after 13 minutes` = {
      markMinutes(13)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.25221023, 0.00000001)
    }

    @test def `has a rate of 0.23594443 events/sec after 14 minutes` = {
      markMinutes(14)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.23594443, 0.00000001)
    }

    @test def `has a rate of 0.22072766 events/sec after 15 minutes` = {
      markMinutes(15)
      ewma.rate(TimeUnit.SECONDS).mustBeApproximately(0.22072766, 0.00000001)
    }
  }
}
