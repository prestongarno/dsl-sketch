package org.kotlinq.adapters.validation

import org.kotlinq.adapters.GraphQlAdapter
import org.kotlinq.adapters.GraphQlProperty
import kotlin.reflect.full.isSubtypeOf


internal
fun GraphQlProperty<*>.isList(): Boolean =
    kotlinType.isSubtypeOf(PrototypeContainer.apexListType)

internal
fun GraphQlProperty<*>.isNullable(): Boolean =
    kotlinType.isMarkedNullable

internal
fun GraphQlAdapter.isList(): Boolean =
    type.isSubtypeOf(PrototypeContainer.apexListType)

internal
fun GraphQlAdapter.isNullable(): Boolean =
    type.isMarkedNullable

private object PrototypeContainer {
  val listProperty: List<*>? = emptyList<Any?>()
  val apexListType = PrototypeContainer::listProperty.returnType
}

