package com.yammer.metrics

import core._

object NameBuilder {
  def apply(klass : Class[_]) = new NameBuilder(klass)
  def apply(domain : String, tipe : String) = new NameBuilder(domain,tipe)
}

class NameBuilder(val domain : String, val tipe : String) {
  def this(klass : Class[_]) = 
           this(klass.getPackage.getName, 
                klass.getSimpleName)
  
  def apply(name : String) = new MetricName(domain, tipe, name)
}