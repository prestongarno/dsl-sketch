package org.kotlinq.static

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


// todo


fun <T> readOnly(value: T): ReadOnlyProperty<Any, T> = ReadOnlyImpl(value)

interface Provider<out T> {
  operator fun provideDelegate(inst: Any, property: KProperty<*>): ReadOnlyProperty<Any, T>
}

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal
fun <T : Any> deserialized(): Provider<DeserializingStub> =
    StubProvider(DeserializingStub::class)

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal
fun <T : Any> initialized(): Provider<InitializingStub<T>> =
    StubProvider(InitializingStub::class) as StubProvider<InitializingStub<T>>

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal
fun <T : Any> enumMapper(): Provider<InitializingStub<T>> =
    StubProvider(InitializingStub::class) as StubProvider<InitializingStub<T>>


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