package org.kotlinq.static

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


// todo


fun <T> readOnly(value: T): ReadOnlyProperty<Any, T> = ReadOnlyImpl(value)

@PublishedApi internal
inline
fun <reified T : Any> stub(): StubProvider<T> = StubProvider(T::class)

interface Provider<out T: GraphQlPropertyStub<*>> {
  operator fun provideDelegate(inst: Any, property: KProperty<*>): ReadOnlyProperty<Any, T>
}

private
class ReadOnlyImpl<out T>(val value: T) : ReadOnlyProperty<Any, T> {
  override operator fun getValue(thisRef: Any, property: KProperty<*>): T = value
}

@PublishedApi
internal
class StubProvider<in U: Any>(clazz: KClass<U>) {

  operator fun provideDelegate(
      inst: Any,
      property: KProperty<*>
  ): ReadOnlyProperty<Any, InitializingStub<U>> =
      readOnly(GraphQlPropertyStub.create(property.name))
}