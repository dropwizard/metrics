package com.yammer.jmx.tests

import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import com.yammer.jmx.JmxBeanBuilder
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito.{when, verify}
import java.util.concurrent.TimeUnit
import com.yammer.time.{Rate, Duration}
import com.yammer.metrics.{LoadMeter, Timer, Meter, Counter}

class JmxBeanBuilderTest extends Spec with MustMatchers with MockitoSugar {
  describe("building a JMX bean") {
    it("builds a bean with the class of the provided object") {
      val builder = new JmxBeanBuilder("funky", this)
      builder.build.klass must equal(classOf[JmxBeanBuilderTest])
    }

    it("builds a bean with the provided description") {
      val builder = new JmxBeanBuilder("funky", this)
      builder.build.description must equal("funky")
    }

    describe("with an attribute") {
      val builder = new JmxBeanBuilder("funky", this)
      builder.addAttribute("my name", "my desc") {
        "result"
      }

      it("builds a bean with a read-only attribute") {
        val attr = builder.build.attributes("my name")

        attr.name must equal("my name")
        attr.description must equal("my desc")
        attr() must equal("result")
      }
    }

    describe("with a counter") {
      val counter = mock[Counter]
      when(counter.count).thenReturn(11)

      val builder = new JmxBeanBuilder("funky", this)
      builder.addCounter("ninjas", counter)

      it("builds a bean with a read-only count attribute") {
        val attr = builder.build.attributes("ninjas-count")

        attr.name must equal("ninjas-count")
        attr() must equal("11")
      }
    }

    describe("with a meter") {
      val meter = mock[Meter]
      when(meter.rate).thenReturn(Rate.perSecond(0.32))
      when(meter.count).thenReturn(11)

      val builder = new JmxBeanBuilder("funky", this)
      builder.addMeter("ninjas", meter, TimeUnit.SECONDS)

      it("builds a bean with a read-only count attribute") {
        val attr = builder.build.attributes("ninjas-count")

        attr.name must equal("ninjas-count")
        attr() must equal("11")
      }

      it("builds a bean with a read-only rate attribute") {
        val attr = builder.build.attributes("ninjas-rate")

        attr.name must equal("ninjas-rate")
        attr() must equal("0.32/s")
      }
    }

    describe("with a timer") {
      val timer = mock[Timer]
      when(timer.max).thenReturn(Duration.milliseconds(400))
      when(timer.min).thenReturn(Duration.milliseconds(20))
      when(timer.mean).thenReturn(Duration.milliseconds(123))
      when(timer.standardDeviation).thenReturn(Duration.milliseconds(12))
      when(timer.p999).thenReturn(Duration.milliseconds(320))
      when(timer.count).thenReturn(11)

      val builder = new JmxBeanBuilder("funky", this)
      builder.addTimer("ninjas", timer, TimeUnit.MILLISECONDS)

      it("builds a bean with a read-only count attribute") {
        val attr = builder.build.attributes("ninjas-count")

        attr.name must equal("ninjas-count")
        attr() must equal("11")
      }

      it("builds a bean with a read-only max attribute") {
        val attr = builder.build.attributes("ninjas-max")

        attr.name must equal("ninjas-max")
        attr() must equal("400.00ms")
      }

      it("builds a bean with a read-only min attribute") {
        val attr = builder.build.attributes("ninjas-min")

        attr.name must equal("ninjas-min")
        attr() must equal("20.00ms")
      }

      it("builds a bean with a read-only mean attribute") {
        val attr = builder.build.attributes("ninjas-mean")

        attr.name must equal("ninjas-mean")
        attr() must equal("123.00ms")
      }

      it("builds a bean with a read-only stddev attribute") {
        val attr = builder.build.attributes("ninjas-stddev")

        attr.name must equal("ninjas-stddev")
        attr() must equal("12.00ms")
      }

      it("builds a bean with a read-only 99.9% attribute") {
        val attr = builder.build.attributes("ninjas-99.9%")

        attr.name must equal("ninjas-99.9%")
        attr() must equal("320.00ms")
      }
    }

    describe("with a load meter") {
      val meter = mock[LoadMeter]
      when(meter.rate).thenReturn(Rate.perSecond(0.32))
      when(meter.oneMinuteRate).thenReturn(Rate.perSecond(1.68))
      when(meter.fiveMinuteRate).thenReturn(Rate.perSecond(0.84))
      when(meter.fifteenMinuteRate).thenReturn(Rate.perSecond(0.16))
      when(meter.count).thenReturn(11)

      val builder = new JmxBeanBuilder("funky", this)
      builder.addLoadMeter("ninjas", meter, TimeUnit.SECONDS)

      it("builds a bean with a read-only count attribute") {
        val attr = builder.build.attributes("ninjas-count")

        attr.name must equal("ninjas-count")
        attr() must equal("11")
      }

      it("builds a bean with a read-only mean rate attribute") {
        val attr = builder.build.attributes("ninjas-mean-rate")

        attr.name must equal("ninjas-mean-rate")
        attr() must equal("0.32/s")
      }

      it("builds a bean with a read-only 1 minute rate attribute") {
        val attr = builder.build.attributes("ninjas-01min-rate")

        attr.name must equal("ninjas-01min-rate")
        attr() must equal("1.68/s")
      }

      it("builds a bean with a read-only 5 minute rate attribute") {
        val attr = builder.build.attributes("ninjas-05min-rate")

        attr.name must equal("ninjas-05min-rate")
        attr() must equal("0.84/s")
      }

      it("builds a bean with a read-only 15 minute rate attribute") {
        val attr = builder.build.attributes("ninjas-15min-rate")

        attr.name must equal("ninjas-15min-rate")
        attr() must equal("0.16/s")
      }
    }
  }
}
