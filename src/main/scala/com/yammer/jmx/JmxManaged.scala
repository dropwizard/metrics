package com.yammer.jmx

import javax.management.ObjectName

/**
 * A mixin for exposing metrics via JMX. Extends a class with this, then call
 * enableJMX and specify the attributes you want exposed.
 *
 * @author coda
 */
trait JmxManaged {
  /**
   * Enables JMX, passing a JmxBeanBuilder to the provided function.
   */
  protected def enableJMX(configure: JmxBeanBuilder => Unit) {
    enableJMX("")(configure)
  }

  /**
   * Enables JMX, passing a JmxBeanBuilder to the provided function.
   */
  protected def enableJMX(description : String, name: String = getClass.getSimpleName)
                         (configure: JmxBeanBuilder => Unit) {
    val builder = new JmxBeanBuilder(description, this)
    configure(builder)
    JMX.register(builder.build, objectName(name))
  }

  private def objectName(name : String) = new ObjectName(
    "%s:type=%s".format(getClass.getPackage.getName, name)
  )
}
