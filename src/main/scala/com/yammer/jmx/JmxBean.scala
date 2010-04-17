package com.yammer.jmx

import scala.collection.mutable.ListBuffer
import javax.management._

/**
 * A dynamic JMX MBean with a set of read-only attributes.
 */
case class JmxBean(klass: Class[_],
                   description: String,
                   attributes: Map[String, JmxReadOnlyAttribute])
        extends DynamicMBean {

  def setAttributes(attributes: AttributeList) = new AttributeList
  def setAttribute(attribute: Attribute) {}
  def invoke(actionName: String, params: Array[Object], signature: Array[String]) = null

  def getMBeanInfo: MBeanInfo = {
    val attributeInfos = ListBuffer[MBeanAttributeInfo]()
    for ((name, attr) <- attributes.iterator) {
      attributeInfos += new MBeanAttributeInfo(
        name,
        "java.lang.String",
        attr.description,
        true, false, false
      )
    }
    new MBeanInfo(
      clean(klass.getCanonicalName),
      description,
      attributeInfos.toArray,
      Array(), Array(), Array()
    )
  }

  def getAttributes(names: Array[String]): AttributeList = {
    val list = new AttributeList
    for (name <- names) {
      list.add(new Attribute(name, attributes(name)()))
    }
    list
  }

  def getAttribute(name: String) = attributes(name)()

  private def clean(s: String) = if (s.endsWith("$")) {
    s.substring(0, s.length-1)
  } else {
    s
  }.replaceAll("\\$", ".")
}
