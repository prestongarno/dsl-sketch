package org.kotlinq.delegates

import org.kotlinq.api.Model
import org.kotlinq.delegates.GraphQlPropertyStub.Companion.create
import org.kotlinq.dsl.ArgBuilder
import org.kotlinq.dsl.ArgumentSpec
import org.kotlinq.dsl.DslBuilder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


sealed class GraphQlPropertyStub(val arguments: ArgumentSpec) {

  abstract fun withArguments(arguments: ArgumentSpec): GraphQlPropertyStub

  companion object {

    @Suppress("UNCHECKED_CAST")
    internal fun <T : GraphQlPropertyStub> create(clazz: KClass<T>, propertyName: String, args: ArgumentSpec = ArgBuilder()): T =
        when (clazz) {
          InitializingStub::class -> InitializingStub<Any>(propertyName, args)
          DeserializingStub::class -> DeserializingStub(propertyName, args)
          EnumStub::class -> EnumStub.create(propertyName, args)
          else -> null!!
        } as T
  }

  class Disjoint<in Z>(private val propertyName: String, args: ArgumentSpec = ArgBuilder()) : GraphQlPropertyStub(args) {

    override fun withArguments(arguments: ArgumentSpec): InitializingStub<Z> = TODO()

    operator fun <U : Z> invoke(init: () -> U): DelegateProvider<U?> = initializingProvider(propertyName, init)
  }
}

class PredicateStub<in A : ArgumentSpec, out T : GraphQlPropertyStub>(
    private val name: String,
    private val clazz: KClass<T>
) {
  fun withArguments(arguments: A): T = create(clazz, name, arguments)
}

class DisjointCollectionStub<T, out U : List<*>>(args: ArgumentSpec = ArgBuilder()) : GraphQlPropertyStub(args) {

  override fun withArguments(arguments: ArgumentSpec): DisjointCollectionStub<T, U> = DisjointCollectionStub(arguments)

  operator fun invoke(init: () -> T): DelegateProvider<U> = TODO()

  operator fun invoke(init: () -> T, block: DslBuilder<T, ArgumentSpec>.() -> Unit): DelegateProvider<U> = TODO()
}

class InitializingStub<in Z>(
    private val propertyName: String,
    args: ArgumentSpec = ArgBuilder()
) : GraphQlPropertyStub(args) {

  override fun withArguments(arguments: ArgumentSpec): InitializingStub<Z> = InitializingStub(propertyName, arguments)


  operator fun <U : Z> invoke(
      init: () -> U
  ): DelegateProvider<U> =
      initializingProvider(propertyName, init)

}

class DeserializingStub(
    private val propertyName: String,
    args: ArgumentSpec = ArgBuilder()
) : GraphQlPropertyStub(args) {

  override fun withArguments(arguments: ArgumentSpec) = DeserializingStub(propertyName, arguments)

  operator fun <Z> invoke(
      init: (java.io.InputStream) -> Z,
      block: DslBuilder<Z, ArgBuilder>.() -> Unit = {}
  ): DelegateProvider<Z> =
      deserializingProvider(propertyName, init).also(block)

}

class EnumStub<Z : Enum<Z>>(
    private val propertyName: String,
    args: ArgumentSpec = ArgBuilder()
) : GraphQlPropertyStub(args), DelegateProvider<Z> {

  override fun withArguments(arguments: ArgumentSpec): EnumStub<Z> = EnumStub(propertyName, arguments)

  override operator fun provideDelegate(inst: Model<*>, property: KProperty<*>): ReadOnlyProperty<Any, Z> =
      parsingProvider(propertyName, { str ->
        @Suppress("UNCHECKED_CAST") (property.returnType.classifier as KClass<Z>)
            .java.enumConstants.find { it.name == str }!!
      }).provideDelegate(inst, property)

  companion object {

    enum class None

    internal fun create(name: String, args: ArgumentSpec): EnumStub<None> = EnumStub(name)
  }
}
