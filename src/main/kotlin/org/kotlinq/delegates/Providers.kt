package org.kotlinq.delegates

import org.kotlinq.adapters.adapter
import org.kotlinq.adapters.graphQlProperty
import org.kotlinq.dsl.ArgBuilder
import org.kotlinq.dsl.DslBuilder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


interface DelegateProvider<out Z> {
  operator fun provideDelegate(inst: Any, property: KProperty<*>)
      : ReadOnlyProperty<Any, Z>
}

internal
fun <A, Z> deserializingProvider(name: String, init: (A) -> Z)
    : DslBuilderProvider<Z> = DeserializingDelegateProviderImpl(name, init)

internal
fun <Z> delegateProvider(name: String, init: () -> Z)
    : DslBuilderProvider<Z> = DelegateProviderImpl(name, init)

internal
interface DslBuilderProvider<Z>
  : DslBuilder<Z, ArgBuilder>,
    DelegateProvider<Z>

private
class DeserializingDelegateProviderImpl<in A, Z>(
    val name: String,
    val init: (A) -> Z
) : DslBuilderProvider<Z> {

  private val args: ArgBuilder = ArgBuilder()

  override var default: Z? = null

  override fun config(block: ArgBuilder.() -> Unit) = args.block()

  override operator fun provideDelegate(inst: Any, property: KProperty<*>)
      : ReadOnlyProperty<Any, Z> =
      graphQlProperty(name, property.returnType, adapter(init), default)
}

class DelegateProviderImpl<Z>(
    val name: String,
    val init: () -> Z
) : DslBuilderProvider<Z> {

  private val args: ArgBuilder = ArgBuilder()

  override var default: Z? = null

  override fun config(block: ArgBuilder.() -> Unit) = args.block()

  override operator fun provideDelegate(inst: Any, property: KProperty<*>)
      : ReadOnlyProperty<Any, Z> =
      graphQlProperty(name, property.returnType, adapter(init), default)
}