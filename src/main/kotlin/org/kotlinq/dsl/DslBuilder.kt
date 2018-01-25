package org.kotlinq.dsl

interface DslBuilder<T, out A> {
  var default: T?
  fun config(block: A.() -> Unit): Unit
}