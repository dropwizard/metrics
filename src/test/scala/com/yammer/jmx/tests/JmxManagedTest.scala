package com.yammer.jmx.tests


import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito.{when, verify}
import org.mockito.Matchers.{eq => matchEq}
import javax.management.{ObjectName, MBeanServer}
import org.scalatest.{OneInstancePerTest, BeforeAndAfterEach, Spec}
import org.mockito.ArgumentCaptor
import com.yammer.jmx.{JmxBean, JmxManaged, JMX}

class JmxManagedTest extends Spec
        with MustMatchers
        with BeforeAndAfterEach
        with MockitoSugar
        with OneInstancePerTest {

  class JmxEnabledDummy extends JmxManaged {
    def go() {
      enableJMX("it's a me") { jmx =>
        jmx.addAttribute("yay") { "whee" }
      }
    }
  }

  describe("a JMX-enabled object") {
    val objectName = new ObjectName("com.yammer.jmx.tests:type=JmxEnabledDummy")

    val server = mock[MBeanServer]
    when(server.isRegistered(objectName)).thenReturn(false)
    JMX.server = server

    it("checks to see if the object is already registered") {
      new JmxEnabledDummy().go()

      verify(server).isRegistered(objectName)
    }

    it("registers the created bean") {
      new JmxEnabledDummy().go()

      val captor = ArgumentCaptor.forClass(classOf[JmxBean])
      verify(server).registerMBean(captor.capture, matchEq(objectName))

      val bean = captor.getValue
      bean.klass must equal(classOf[JmxEnabledDummy])
      bean.description must equal("it's a me")
      bean.attributes("yay")() must equal("whee")
    }
  }

  override protected def afterEach() {
    JMX.reset()
  }
}
