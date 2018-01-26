package org.kotlinq.api

import org.kotlinq.adapters.GraphQlProperty


open class Model<out T>(val type: T) {

  internal
  val properties = mutableSetOf<GraphQlProperty<*>>()

  internal
  fun <T> bind(property: GraphQlProperty<T>): GraphQlProperty<T> {
    properties += property
    return property
  }
}