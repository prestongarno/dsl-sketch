package org.kotlinq.static

import org.kotlinq.delegates.DelegateProvider
import org.kotlinq.delegates.initializingProvider
import org.kotlinq.delegates.parsingProvider
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
fun <T : Enum<T>> enumMapper(): Provider<T> =
    EnumProvider<T>()


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
class EnumProvider<T : Enum<T>> : Provider<T> {
  @Suppress("UNCHECKED_CAST")
  override fun provideDelegate(inst: Any, property: KProperty<*>): ReadOnlyProperty<Any, T> =
      parsingProvider(property.name, { str ->
        @Suppress("UNCHECKED_CAST") (property.returnType.classifier as KClass<T>)
            .java.enumConstants.find { it.name == str }!!
      }).provideDelegate(inst, property)
}