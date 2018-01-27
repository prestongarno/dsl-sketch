package org.kotlinq.delegates

import org.kotlinq.Model
import org.kotlinq.delegates.GraphQlPropertyStub.Companion.create
import org.kotlinq.dsl.ArgBuilder
import org.kotlinq.dsl.ArgumentSpec
import org.kotlinq.dsl.DslBuilder
import org.kotlinq.static.readOnly
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
          DisjointCollectionStub::class -> DisjointCollectionStub<Any>(propertyName, args)
          DisjointCollectionStub1::class -> DisjointCollectionStub1<Any>(propertyName, args)
          DisjointCollectionStub2::class -> DisjointCollectionStub2<Any>(propertyName, args)
          Disjoint::class -> Disjoint<Any>(propertyName, args)
          else -> throw UnsupportedOperationException("Unknown class $clazz")
        } as T
  }

  class Disjoint<in Z>(private val propertyName: String, args: ArgumentSpec = ArgBuilder()) : GraphQlPropertyStub(args) {

    override fun withArguments(arguments: ArgumentSpec): InitializingStub<Z> = TODO()

    operator fun <U : Model<Z>> invoke(init: () -> U): DelegateProvider<U?> = initializingProvider(propertyName, init)
  }
}

class PredicateStub<in A : ArgumentSpec, out T : GraphQlPropertyStub>(
    private val name: String,
    private val clazz: KClass<T>
) {
  fun withArguments(arguments: A): T = create(clazz, name, arguments)
}

class InitializingStub<in Z>(
    private val propertyName: String,
    args: ArgumentSpec = ArgBuilder()
) : GraphQlPropertyStub(args) {

  override fun withArguments(arguments: ArgumentSpec): InitializingStub<Z> = InitializingStub(propertyName, arguments)

  operator fun <U : Model<Z>> invoke(
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
        @Suppress("UNCHECKED_CAST")
        (property.returnType.classifier as? KClass<Z>)
            ?.java?.enumConstants?.find { it.name == str }
      }).provideDelegate(inst, property)

  companion object {

    enum class None

    internal fun create(
        name: String,
        args: ArgumentSpec = ArgBuilder()
    ): EnumStub<None> = EnumStub(name, args)

  }
}

sealed class DisjointCollection(
    internal val name: String,
    args: ArgumentSpec = ArgBuilder()
) : GraphQlPropertyStub(args) {

  companion object {

    @Suppress("UNCHECKED_CAST")
    internal fun <T : DisjointCollection> create(name: String, args: ArgumentSpec, clazz: KClass<T>)
        : ReadOnlyProperty<Any, T> = readOnly(when (clazz) {
      DisjointCollectionStub::class -> DisjointCollectionStub<Any>(name, args)
      DisjointCollectionStub1::class -> DisjointCollectionStub1<Any>(name, args)
      else -> throw IllegalArgumentException()
    } as T)
  }

}

class DisjointCollectionStub<T>(
    name: String,
    args: ArgumentSpec = ArgBuilder()
) : DisjointCollection(name, args) {

  override fun withArguments(arguments: ArgumentSpec)
      : DisjointCollectionStub<T> = DisjointCollectionStub(name, arguments)

  operator fun <Z : Model<T>> invoke(init: () -> Z)
      : DelegateProvider<List<Z>> = collectionProvider(name, init)

  operator fun <Z : Model<T>> invoke(init: () -> Z, block: DslBuilder<Z, ArgumentSpec>.() -> Unit)
      : DelegateProvider<List<Z>> = collectionProvider(name, init, block)
}

class DisjointCollectionStub1<T>(
    name: String,
    args: ArgumentSpec = ArgBuilder()
) : DisjointCollection(name, args) {

  override fun withArguments(arguments: ArgumentSpec)
      : DisjointCollectionStub1<T> = DisjointCollectionStub1(name, arguments)

  operator fun <Z : Model<T>> invoke(init: () -> Z)
      : DelegateProvider<List<List<Z>>> = collectionProvider(name, init)

  operator fun <Z : Model<T>> invoke(init: () -> Z, block: DslBuilder<Z, ArgumentSpec>.() -> Unit)
      : DelegateProvider<List<List<Z>>> = collectionProvider(name, init, block)
}

class DisjointCollectionStub2<T>(
    name: String,
    args: ArgumentSpec = ArgBuilder()
) : DisjointCollection(name, args) {

  override fun withArguments(arguments: ArgumentSpec)
      : DisjointCollectionStub<T> = DisjointCollectionStub(name, arguments)

  operator fun <Z : Model<T>> invoke(init: () -> Z)
      : DelegateProvider<List<List<List<Z>>>> = collectionProvider(name, init)

  operator fun <Z : Model<T>> invoke(init: () -> Z, block: DslBuilder<Z, ArgumentSpec>.() -> Unit)
      : DelegateProvider<List<List<List<Z>>>> = collectionProvider(name, init, block)
}

private fun <U : List<*>> collectionProvider(name: String, init: () -> Model<*>): DelegateProvider<U> =
    collectionProvider(name, init, { /*nothing */ })

@Suppress("UNCHECKED_CAST")
private fun <T : Model<*>, U : List<*>> collectionProvider(
    name: String,
    init: () -> T,
    block: DslBuilder<T, ArgumentSpec>.() -> Unit
): DelegateProvider<U> =
    initializingProvider(name, init, block as DslBuilder<Model<*>, ArgumentSpec>.() -> Unit)