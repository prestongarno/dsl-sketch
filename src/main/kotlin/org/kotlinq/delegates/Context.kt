package org.kotlinq.delegates

import org.kotlinq.dsl.ArgBuilder
import org.kotlinq.dsl.ArgumentSpec
import org.kotlinq.dsl.DslBuilder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


sealed class GraphQlPropertyStub {

  abstract fun withArguments(arguments: ArgumentSpec): GraphQlPropertyStub

  companion object {

    @Suppress("UNCHECKED_CAST")
    internal fun <T : GraphQlPropertyStub> create(clazz: KClass<T>, propertyName: String): T =
        when (clazz) {
          InitializingStub::class -> InitializingStub<Any>(propertyName)
          DeserializingStub::class -> DeserializingStub(propertyName)
          EnumStub::class -> EnumStub.create(propertyName)
          else -> null!!
        } as T
  }
}

class PredicateStub<in A : ArgumentSpec, out T : GraphQlPropertyStub> {
  fun withArguments(arguments: A): T = TODO()
}

class InitializingStub<in Z>(
    private val propertyName: String
) : GraphQlPropertyStub() {

  override fun withArguments(arguments: ArgumentSpec): InitializingStub<Z> = TODO()


  operator fun <U : Z> invoke(
      init: () -> U
  ): DelegateProvider<U> =
      initializingProvider(propertyName, init)

}

class DeserializingStub(private val propertyName: String) : GraphQlPropertyStub() {

  override fun withArguments(arguments: ArgumentSpec) = this

  operator fun <Z> invoke(
      init: (java.io.InputStream) -> Z,
      block: DslBuilder<Z, ArgBuilder>.() -> Unit = {}
  ): DelegateProvider<Z> =
      deserializingProvider(propertyName, init).also(block)

}

class EnumStub<Z : Enum<Z>>(private val propertyName: String) : GraphQlPropertyStub(), DelegateProvider<Z> {

  override fun withArguments(arguments: ArgumentSpec): EnumStub<Z> = TODO()

  override operator fun provideDelegate(inst: Any, property: KProperty<*>): ReadOnlyProperty<Any, Z> =
      parsingProvider(propertyName, { str ->
        @Suppress("UNCHECKED_CAST") (property.returnType.classifier as KClass<Z>)
            .java.enumConstants.find { it.name == str }!!
      }).provideDelegate(inst, property)

  companion object {

    enum class None

    internal fun create(name: String): EnumStub<None> = EnumStub(name)
  }
}
