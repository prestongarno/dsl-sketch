package org.kotlinq.adapters

import dagger.Module
import dagger.Provides
import org.kotlinq.Model
import org.kotlinq.api.AdapterService
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
internal fun <T> graphQlProperty(
    name: String,
    adapter: GraphQlAdapter,
    default: T? = null
): GraphQlProperty<T> =
 GraphQlPropertyImpl(default, name, adapter) as GraphQlProperty<T>

internal
interface GraphQlProperty<out T> : ReadOnlyProperty<Any, T> {
  val propertyName: String
  val adapter: GraphQlAdapter

  @Module
  companion object : AdapterFactory {

    override val service: AdapterService
      get() = AdapterService.Companion

    @get:Provides val adapterFactory: AdapterFactory = this
  }
}

internal
class GraphQlPropertyImpl<out T>(
    private val default: T? = null,
    override val propertyName: String,
    override val adapter: GraphQlAdapter
) : GraphQlProperty<T?> {

  override fun getValue(thisRef: Any, property: KProperty<*>): T? =
      (adapter.getValue() as? T) ?: default

  override fun toString() = "$propertyName (${adapter.getValue()})"

}
