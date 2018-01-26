package org.kotlinq.delegates

import org.kotlinq.adapters.deserializer
import org.kotlinq.adapters.graphQlProperty
import org.kotlinq.adapters.initializer
import org.kotlinq.adapters.parser
import org.kotlinq.api.Model
import org.kotlinq.dsl.ArgBuilder
import org.kotlinq.dsl.DslBuilder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


interface DelegateProvider<out Z> {
  operator fun provideDelegate(inst: Model<*>, property: KProperty<*>)
      : ReadOnlyProperty<Any, Z>
}

internal
fun <Z> deserializingProvider(name: String, init: (java.io.InputStream) -> Z)
    : DslBuilderProvider<Z> = DeserializingDelegateProviderImpl(name, init)

internal
fun <Z> parsingProvider(name: String, init: (String) -> Z)
    : DslBuilderProvider<Z> = ParsingDelegateProvider(name, init)

internal
fun <Z> initializingProvider(name: String, init: () -> Z)
    : DslBuilderProvider<Z> = DelegateProviderImpl(name, init)

interface DslBuilderProvider<Z>
  : DslBuilder<Z, ArgBuilder>,
    DelegateProvider<Z>

private
class DeserializingDelegateProviderImpl<Z>(
    val name: String,
    val init: (java.io.InputStream) -> Z
) : DslBuilderProvider<Z> {

  private val args: ArgBuilder = ArgBuilder()

  override var default: Z? = null

  override fun config(block: ArgBuilder.() -> Unit) = args.block()

  override operator fun provideDelegate(inst: Model<*>, property: KProperty<*>)
      : ReadOnlyProperty<Any, Z> = graphQlProperty(
      inst,
      name,
      deserializer(property.returnType, init),
      default
  )
}

private
class ParsingDelegateProvider<Z>(
    val name: String,
    val init: (String) -> Z,
    override var default: Z? = null
) : DslBuilderProvider<Z> {

  val args: ArgBuilder = ArgBuilder()

  override fun config(block: ArgBuilder.() -> Unit) = args.block()

  override fun provideDelegate(inst: Model<*>, property: KProperty<*>) =
      graphQlProperty<Z>(
          inst,
          name,
          parser(property.returnType, init)
      )
}

private
class DelegateProviderImpl<Z>(
    val name: String,
    val init: () -> Z
) : DslBuilderProvider<Z> {

  private val args: ArgBuilder = ArgBuilder()

  override var default: Z? = null

  override fun config(block: ArgBuilder.() -> Unit) = args.block()

  override operator fun provideDelegate(inst: Model<*>, property: KProperty<*>)
      : ReadOnlyProperty<Any, Z> = graphQlProperty(
      inst,
      name,
      initializer(property.returnType, init),
      default
  )
}