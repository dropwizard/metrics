package com.yammer.jmx.tests


import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import com.yammer.jmx.{JmxReadOnlyAttribute, JmxBean}

class JmxBeanTest extends Spec with MustMatchers {
  describe("setting the attributes of a JMX bean") {
    val bean = JmxBean(classOf[JmxBean], "a bean", Map())

    it("doesn't work yet") {
      // smoke test
      bean.setAttributes(null) must have size(0)
      bean.setAttribute(null)
    }
  }

  describe("invoking the operations of a JMX bean") {
    val bean = JmxBean(classOf[JmxBean], "a bean", Map())

    it("doesn't work") {
      // smoke test
      bean.invoke("what", Array("yeah"), Array("ok"))
    }
  }

  describe("getting an attribute of a JMX bean") {
    val bean = JmxBean(
      classOf[JmxBean],
      "a bean",
      Map("stuff" -> JmxReadOnlyAttribute("stuff", null, () => 19918))
    )

    it("returns the attribute value as a string") {
      bean.getAttribute("stuff") must equal("19918")
    }
  }

  describe("getting multiple attributes of a JMX bean") {
    val bean = JmxBean(
      classOf[JmxBean],
      "a bean",
      Map(
        "stuff" -> JmxReadOnlyAttribute("stuff", null, () => 19918),
        "things" -> JmxReadOnlyAttribute("things", null, () => 19919)
      )
    )

    it("returns the attributes as an AttributeList") {
      val attrs = bean.getAttributes(Array("stuff", "things")).asList

      attrs must have size (2)

      attrs.get(0).getName must equal("stuff")
      attrs.get(0).getValue must equal("19918")

      attrs.get(1).getName must equal("things")
      attrs.get(1).getValue must equal("19919")
    }
  }

  describe("getting an JMX bean's information") {
    val bean = JmxBean(
      classOf[JmxBeanTest],
      "a bean",
      Map(
        "stuff" -> JmxReadOnlyAttribute("stuff", "some stuff", () => 19918),
        "things" -> JmxReadOnlyAttribute("things", "some things", () => 19919)
      )
    )

    val info = bean.getMBeanInfo

    it("has a class name") {
      info.getClassName must equal("com.yammer.jmx.tests.JmxBeanTest")
    }

    it("has a description") {
      info.getDescription must equal("a bean")
    }

    it("has attributes") {
      val attrs = info.getAttributes

      attrs must have size (2)

      attrs(0).getName must equal("stuff")
      attrs(0).getType must equal("java.lang.String")
      attrs(0).getDescription must equal("some stuff")
      attrs(0).isIs must be(false)
      attrs(0).isReadable must be(true)
      attrs(0).isWritable must be(false)

      attrs(1).getName must equal("things")
      attrs(1).getType must equal("java.lang.String")
      attrs(1).getDescription must equal("some things")
      attrs(1).isIs must be(false)
      attrs(1).isReadable must be(true)
      attrs(1).isWritable must be(false)
    }

    it("has no constructors") {
      info.getConstructors must have size(0)
    }

    it("has no operations") {
      info.getOperations must have size(0)
    }

    it("has no notifications") {
      info.getNotifications must have size(0)
    }
  }
}
