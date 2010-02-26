package com.yammer.jmx.tests


import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import com.yammer.jmx.JmxReadOnlyAttribute

class JmxReadOnlyAttributeTest extends Spec with MustMatchers {
  describe("a read-only JMX attribute") {
    val attr = JmxReadOnlyAttribute("name", "desc", () => 1 + 3)

    it("has a name") {
      attr.name must equal("name")
    }

    it("has a description") {
      attr.description must equal("desc")
    }

    it("has a getter") {
      attr.getter() must equal(4)
    }

    it("converts the call to a string on apply") {
      attr() must equal("4")
    }
  }
}
