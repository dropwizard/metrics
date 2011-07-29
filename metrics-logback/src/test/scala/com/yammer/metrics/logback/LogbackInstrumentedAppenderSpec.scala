package com.yammer.metrics.logback

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import ch.qos.logback.classic.{Level, LoggerContext}

class LogbackInstrumentedAppenderSpec extends Spec {
  class `A Logback InstrumentedAppender` {
    val lc = new LoggerContext()
    val logger = lc.getLogger("abc.def")

    val appender = new InstrumentedAppender()
    appender.setContext(lc)
    appender.start()
    logger.addAppender(appender)
    logger.setLevel(Level.TRACE)

    @test def `maintains accurate counts` = {
      InstrumentedAppender.ALL_METER.count must beEqualTo(0)
      InstrumentedAppender.TRACE_METER.count must beEqualTo(0)
      InstrumentedAppender.DEBUG_METER.count must beEqualTo(0)
      InstrumentedAppender.INFO_METER.count must beEqualTo(0)
      InstrumentedAppender.WARN_METER.count must beEqualTo(0)
      InstrumentedAppender.ERROR_METER.count must beEqualTo(0)

      logger.trace("Test")

      InstrumentedAppender.ALL_METER.count must beEqualTo(1)
      InstrumentedAppender.TRACE_METER.count must beEqualTo(1)
      InstrumentedAppender.DEBUG_METER.count must beEqualTo(0)
      InstrumentedAppender.INFO_METER.count must beEqualTo(0)
      InstrumentedAppender.WARN_METER.count must beEqualTo(0)
      InstrumentedAppender.ERROR_METER.count must beEqualTo(0)

      logger.trace("Test")
      logger.debug("Test")

      InstrumentedAppender.ALL_METER.count must beEqualTo(3)
      InstrumentedAppender.TRACE_METER.count must beEqualTo(2)
      InstrumentedAppender.DEBUG_METER.count must beEqualTo(1)
      InstrumentedAppender.INFO_METER.count must beEqualTo(0)
      InstrumentedAppender.WARN_METER.count must beEqualTo(0)
      InstrumentedAppender.ERROR_METER.count must beEqualTo(0)

      logger.info("Test")

      InstrumentedAppender.ALL_METER.count must beEqualTo(4)
      InstrumentedAppender.TRACE_METER.count must beEqualTo(2)
      InstrumentedAppender.DEBUG_METER.count must beEqualTo(1)
      InstrumentedAppender.INFO_METER.count must beEqualTo(1)
      InstrumentedAppender.WARN_METER.count must beEqualTo(0)
      InstrumentedAppender.ERROR_METER.count must beEqualTo(0)

      logger.warn("Test")

      InstrumentedAppender.ALL_METER.count must beEqualTo(5)
      InstrumentedAppender.TRACE_METER.count must beEqualTo(2)
      InstrumentedAppender.DEBUG_METER.count must beEqualTo(1)
      InstrumentedAppender.INFO_METER.count must beEqualTo(1)
      InstrumentedAppender.WARN_METER.count must beEqualTo(1)
      InstrumentedAppender.ERROR_METER.count must beEqualTo(0)

      logger.error("Test")

      InstrumentedAppender.ALL_METER.count must beEqualTo(6)
      InstrumentedAppender.TRACE_METER.count must beEqualTo(2)
      InstrumentedAppender.DEBUG_METER.count must beEqualTo(1)
      InstrumentedAppender.INFO_METER.count must beEqualTo(1)
      InstrumentedAppender.WARN_METER.count must beEqualTo(1)
      InstrumentedAppender.ERROR_METER.count must beEqualTo(1)
    }
  }
}
