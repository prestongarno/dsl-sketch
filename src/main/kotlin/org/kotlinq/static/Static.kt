package org.kotlinq.static

import org.kotlinq.delegates.DeserializingStub
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

interface ConfiguredInitializedProvider<in A : ArgumentSpec, in T> : Provider<PredicateStub<A, InitializingStub<T>>> {
  override operator fun provideDelegate(inst: Any, property: KProperty<*>)
      : ReadOnlyProperty<Any, PredicateStub<A, InitializingStub<T>>>
}

interface PredicateProvider<in A : ArgumentSpec, out T : GraphQlPropertyStub> {
  operator fun provideDelegate(inst: Any, property: KProperty<*>): ReadOnlyProperty<Any, PredicateStub<A, T>>
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