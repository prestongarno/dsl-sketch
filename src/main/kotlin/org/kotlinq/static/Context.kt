package org.kotlinq.static

import org.kotlinq.delegates.DelegateProvider
import org.kotlinq.delegates.initializingProvider
import org.kotlinq.delegates.deserializingProvider
import org.kotlinq.delegates.parsingProvider
import org.kotlinq.dsl.ArgBuilder
import org.kotlinq.dsl.DslBuilder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


sealed class GraphQlPropertyStub {

  companion object {
    @Suppress("UNCHECKED_CAST")
    fun <T : GraphQlPropertyStub> create(clazz: KClass<T>, propertyName: String): T = when (clazz) {
      InitializingStub::class -> InitializingStub<Any>(propertyName)
      DeserializingStub::class -> DeserializingStub(propertyName)
      EnumStub::class -> EnumStub.create(propertyName)
      else -> null!!
    } as T
  }
}

class InitializingStub<in Z : Any>(
    private val propertyName: String
) : GraphQlPropertyStub() {


  operator fun <U : Z> invoke(
      init: () -> U
  ): DelegateProvider<U> =
      initializingProvider(propertyName, init)

}

// TODO this is where it got complicated
class DeserializingStub(private val propertyName: String) : GraphQlPropertyStub() {

  operator fun <Z> invoke(
      init: (java.io.InputStream) -> Z,
      block: DslBuilder<Z, ArgBuilder>.() -> Unit = {}
  ): DelegateProvider<Z> =
      deserializingProvider(propertyName, init).also(block)

}

class EnumStub<Z : Enum<Z>>(private val propertyName: String) : GraphQlPropertyStub(), DelegateProvider<Z> {

  override operator fun provideDelegate(inst: Any, property: KProperty<*>): ReadOnlyProperty<Any, Z> = TODO()
/*      parsingProvider(propertyName, { str ->
        @Suppress("UNCHECKED_CAST") (property.returnType.classifier as KClass<Z>)
            .java.enumConstants.find { it.name == str }!!
      })*/

  companion object {

    enum class None

    internal fun create(name: String): EnumStub<None> = EnumStub(name)
  }
}
