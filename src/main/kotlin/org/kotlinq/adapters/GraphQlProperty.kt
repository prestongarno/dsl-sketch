package org.kotlinq.adapters

import org.kotlinq.api.Model
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType

internal
fun <T> graphQlProperty(
    context: Model<*>,
    name: String,
    type: KType,
    adapter: GraphQlAdapter<T>,
    default: T? = null
): GraphQlProperty<T> = context.bind(
    object : GraphQlProperty<T> {

      override fun getValue(thisRef: Any, property: KProperty<*>): T =
          adapter.getValue() ?: default!!

      override val kotlinType get() = adapter.type
      override val propertyName get() = name
      override val adapter get() = adapter

      override fun toString() = "GraphQlProperty::$name ($adapter)"

    })

internal
interface GraphQlProperty<out T> : ReadOnlyProperty<Any, T> {
  val kotlinType: KType
  val propertyName: String
  val adapter: GraphQlAdapter<T>
}
