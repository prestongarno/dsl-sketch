package org.kotlinq.api

import org.kotlinq.adapters.GraphQlProperty
import org.kotlinq.adapters.validation.isNullable


open class Model<out T>(val model: T) {

  internal
  val properties = mutableMapOf<String, GraphQlProperty<*>>()

  internal
  fun <T> bind(property: GraphQlProperty<T>): GraphQlProperty<T> {
    properties[property.propertyName] = property
    return property
  }

  internal fun isResolved(): Boolean = properties.values.find {
    !it.isNullable() && it.adapter.getValue() != null
  } == null
}