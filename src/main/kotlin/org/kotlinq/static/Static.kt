package org.kotlinq.static

import org.kotlinq.delegates.DeserializingStub
import org.kotlinq.delegates.DisjointCollectionStub
import org.kotlinq.delegates.EnumStub
import org.kotlinq.delegates.GraphQlPropertyStub
import org.kotlinq.delegates.InitializingStub
import org.kotlinq.delegates.PredicateStub
import org.kotlinq.dsl.ArgumentSpec
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


fun <T> readOnly(value: T): ReadOnlyProperty<Any, T> = ReadOnlyImpl(value)

interface Provider<out T> {
  operator fun provideDelegate(inst: Any, property: KProperty<*>): ReadOnlyProperty<Any, T>
}

interface ConfiguredProvider<in A : ArgumentSpec, out T : GraphQlPropertyStub> : Provider<PredicateStub<A, T>> {
  override operator fun provideDelegate(inst: Any, property: KProperty<*>)
      : ReadOnlyProperty<Any, PredicateStub<A, T>>
}

interface PredicateProvider<in A : ArgumentSpec, out T : GraphQlPropertyStub> {

  operator fun provideDelegate(inst: Any, property: KProperty<*>): ReadOnlyProperty<Any, PredicateStub<A, T>>

  companion object {

    internal fun <A : ArgumentSpec> using() = Builder<A>()

    internal class Builder<in A : ArgumentSpec> {
      fun <T : GraphQlPropertyStub> build(clazz: KClass<T>): PredicateProvider<A, T> =
          createPredicateProvider(clazz)
    }

    private
    fun <A : ArgumentSpec, T : GraphQlPropertyStub> createPredicateProvider(clazz: KClass<T>) = object : PredicateProvider<A, T> {
      override fun provideDelegate(inst: Any, property: KProperty<*>) =
          readOnly(PredicateStub<A, T>(property.name, clazz))
    }

  }
}

interface CollectionProvider<T, out U : List<*>> : Provider<DisjointCollectionStub<T, U>> {
  override operator fun provideDelegate(
      inst: Any,
      property: KProperty<*>
  ): ReadOnlyProperty<Any, DisjointCollectionStub<T, U>>

  companion object {
    fun <T, U : List<*>> new(builder: DisjointDelegateBuilder<T, U>) = object : CollectionProvider<T, U> {
      override fun provideDelegate(inst: Any, property: KProperty<*>) = readOnly(DisjointCollectionStub<T, U>())
    }
  }
}

internal
fun <T : Any> deserialized(): Provider<DeserializingStub> =
    StubProvider(DeserializingStub::class)

@Suppress("UNCHECKED_CAST")
internal
fun <T : Any> initialized(): Provider<InitializingStub<T>> =
    StubProvider(InitializingStub::class) as StubProvider<InitializingStub<T>>

internal
fun <T : Enum<T>> enumMapper(): Provider<EnumStub<T>> =
    EnumProvider()


/***********************************************
 * Private implementations
 ***********************************************/

private
class ReadOnlyImpl<out T>(val value: T) : ReadOnlyProperty<Any, T> {
  override operator fun getValue(thisRef: Any, property: KProperty<*>): T = value
}

private
class StubProvider<U : GraphQlPropertyStub>(val dslClass: KClass<U>) : Provider<U> {

  override operator fun provideDelegate(
      inst: Any,
      property: KProperty<*>
  ): ReadOnlyProperty<Any, U> =
      readOnly(GraphQlPropertyStub.create(dslClass, property.name))
}

private
class EnumProvider<T : Enum<T>> : Provider<EnumStub<T>> {
  @Suppress("UNCHECKED_CAST")
  override fun provideDelegate(inst: Any, property: KProperty<*>): ReadOnlyProperty<Any, EnumStub<T>> =
      readOnly(GraphQlPropertyStub.create(EnumStub::class as KClass<EnumStub<T>>, property.name))
}