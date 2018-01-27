package org.kotlinq.adapters

import org.kotlinq.api.Model
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
internal
fun <T> graphQlProperty(
    context: Model<*>,
    name: String,
    adapter: GraphQlAdapter,
    default: T? = null
): GraphQlProperty<T> = context.bind(
    object : GraphQlProperty<T?> {

      private val privateAdapter = adapter.asCollection()

      override fun getValue(thisRef: Any, property: KProperty<*>): T? =
          privateAdapter.getValue() as? T? ?: default

      override val kotlinType get() = adapter.type
      override val propertyName get() = name
      override val adapter get() = privateAdapter

      override fun toString() = "GraphQlProperty::$name ($adapter)"

    } as GraphQlProperty<T>)

internal
interface GraphQlProperty<out T> : ReadOnlyProperty<Any, T> {
  val kotlinType: KType
  val propertyName: String
  val adapter: GraphQlAdapter
}
