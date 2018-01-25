package org.kotlinq.static

import org.kotlinq.delegates.DelegateProvider
import org.kotlinq.delegates.delegateProvider
import org.kotlinq.delegates.deserializingProvider
import org.kotlinq.dsl.ArgBuilder
import org.kotlinq.dsl.DslBuilder


sealed class GraphQlPropertyStub<in Z> {

  companion object {
    inline fun <reified T : GraphQlPropertyStub<*>> create(propertyName: String): T = when (T::class) {
      InitializingStub::class -> InitializingStub<Any>(propertyName)
      DeserializingStub::class -> DeserializingStub<Any>(propertyName)
      else -> null!!
    } as T
  }
}


class InitializingStub<in Z : Any>(
    private val propertyName: String
) : GraphQlPropertyStub<Z>() {

  operator fun <U : Z> invoke(
      init: () -> U
  ): DelegateProvider<U> =
      delegateProvider(propertyName, init)

}

// TODO this is where it got complicated
class DeserializingStub<out A : Any>(private val propertyName: String) : GraphQlPropertyStub<Any>() {

  operator fun <Z> invoke(
      init: (A) -> Z,
      block: DslBuilder<Z, ArgBuilder>.() -> Unit = {}
  ): DelegateProvider<Z> =
      deserializingProvider(propertyName, init).also(block)
}
