package com.yammer.jmx

/**
 * A read-only attribute, exposed via JMX.
 */
case class JmxReadOnlyAttribute(name: String,
                                description: String,
                                getter: () => Any) {
  def apply() = getter().toString
}
