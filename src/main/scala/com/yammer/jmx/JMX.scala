package com.yammer.jmx

import management.ManagementFactory
import javax.management.{ObjectName, MBeanServer}

/**
 * A mockable singleton for interacting with the platform's JMX MBean server.
 *
 * @author coda
 */
object JMX {
  @volatile var server: MBeanServer = null
  reset()

  /**
   * Resets the specified user back to the default platform server.
   */
  def reset() {
    server = ManagementFactory.getPlatformMBeanServer
  }

  /**
   * Registers a JMX MBean with the server. If the name is already registered,
   * the old MBean is unregistered and the new one registered in its place.
   */
  def register(mbean: AnyRef, name: ObjectName) {
    if (server.isRegistered(name)) {
      server.unregisterMBean(name)
    }

    server.registerMBean(mbean, name)
  }
}
