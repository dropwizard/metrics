package com.yammer.jmx.tests


import org.scalatest.matchers.MustMatchers
import com.yammer.jmx.JMX
import management.ManagementFactory
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Spec}
import org.mockito.Mockito.{when, verify, inOrder}
import javax.management.{ObjectName, MBeanServer}

class JMXTest extends Spec with MustMatchers with MockitoSugar with BeforeAndAfterEach {
  describe("the JMX server") {
    it("defaults to the platform server") {
      JMX.server must equal(ManagementFactory.getPlatformMBeanServer)
    }

    it("can be set to a mock server") {
      val mockServer = mock[MBeanServer]
      JMX.server = mockServer
      JMX.server must equal(mockServer)
    }

    it("can be reset to the default server") {
      val mockServer = mock[MBeanServer]
      JMX.server = mockServer
      JMX.reset()
      JMX.server must equal(ManagementFactory.getPlatformMBeanServer)
    }
  }

  describe("registering a new MBean with JMX") {
    val server = mock[MBeanServer]
    val name = mock[ObjectName]
    val bean = mock[AnyRef]
    when(server.isRegistered(name)).thenReturn(false)

    it("simply registers the MBean") {
      JMX.server = server
      JMX.register(bean, name)

      verify(server).registerMBean(bean, name)
    }
  }

  describe("re-registering a MBean with JMX") {
    val server = mock[MBeanServer]
    val name = mock[ObjectName]
    val bean = mock[AnyRef]
    when(server.isRegistered(name)).thenReturn(true)

    it("unregisters the old bean before registering the new one") {
      JMX.server = server
      JMX.register(bean, name)

      val ordered = inOrder(server)
      ordered.verify(server).unregisterMBean(name)
      ordered.verify(server).registerMBean(bean, name)
    }
  }

  override protected def afterEach() {
    JMX.reset()
  }
}
