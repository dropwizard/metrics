package com.yammer.metrics.logback

import org.junit.Test
import com.codahale.simplespec.Spec
<<<<<<< HEAD
=======
import org.junit.Test
>>>>>>> hotfix/2.0.0-BETA16-with-2.9.1
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

    @Test def `maintains accurate counts` = {
      InstrumentedAppender.ALL_METER.count.must(be(0))
      InstrumentedAppender.TRACE_METER.count.must(be(0))
      InstrumentedAppender.DEBUG_METER.count.must(be(0))
      InstrumentedAppender.INFO_METER.count.must(be(0))
      InstrumentedAppender.WARN_METER.count.must(be(0))
      InstrumentedAppender.ERROR_METER.count.must(be(0))

      logger.trace("Test")

      InstrumentedAppender.ALL_METER.count.must(be(1))
      InstrumentedAppender.TRACE_METER.count.must(be(1))
      InstrumentedAppender.DEBUG_METER.count.must(be(0))
      InstrumentedAppender.INFO_METER.count.must(be(0))
      InstrumentedAppender.WARN_METER.count.must(be(0))
      InstrumentedAppender.ERROR_METER.count.must(be(0))

      logger.trace("Test")
      logger.debug("Test")

      InstrumentedAppender.ALL_METER.count.must(be(3))
      InstrumentedAppender.TRACE_METER.count.must(be(2))
      InstrumentedAppender.DEBUG_METER.count.must(be(1))
      InstrumentedAppender.INFO_METER.count.must(be(0))
      InstrumentedAppender.WARN_METER.count.must(be(0))
      InstrumentedAppender.ERROR_METER.count.must(be(0))

      logger.info("Test")

      InstrumentedAppender.ALL_METER.count.must(be(4))
      InstrumentedAppender.TRACE_METER.count.must(be(2))
      InstrumentedAppender.DEBUG_METER.count.must(be(1))
      InstrumentedAppender.INFO_METER.count.must(be(1))
      InstrumentedAppender.WARN_METER.count.must(be(0))
      InstrumentedAppender.ERROR_METER.count.must(be(0))

      logger.warn("Test")

      InstrumentedAppender.ALL_METER.count.must(be(5))
      InstrumentedAppender.TRACE_METER.count.must(be(2))
      InstrumentedAppender.DEBUG_METER.count.must(be(1))
      InstrumentedAppender.INFO_METER.count.must(be(1))
      InstrumentedAppender.WARN_METER.count.must(be(1))
      InstrumentedAppender.ERROR_METER.count.must(be(0))

      logger.error("Test")

      InstrumentedAppender.ALL_METER.count.must(be(6))
      InstrumentedAppender.TRACE_METER.count.must(be(2))
      InstrumentedAppender.DEBUG_METER.count.must(be(1))
      InstrumentedAppender.INFO_METER.count.must(be(1))
      InstrumentedAppender.WARN_METER.count.must(be(1))
      InstrumentedAppender.ERROR_METER.count.must(be(1))
    }
  }
}
