package org.kotlinq.adapters.validation

import org.kotlinq.adapters.GraphQlAdapter
import org.kotlinq.adapters.GraphQlProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf


internal
fun KType.isCollection(): Boolean {
  return isSubtypeOf(PrototypeContainer.apexListType)
}

internal
fun GraphQlProperty<*>.isList(): Boolean =
    adapter.type.isSubtypeOf(PrototypeContainer.apexListType)

internal
fun GraphQlProperty<*>.isNullable(): Boolean =
    adapter.type.isMarkedNullable

internal
fun GraphQlAdapter.isList(): Boolean =
    type.isSubtypeOf(PrototypeContainer.apexListType)

internal
fun GraphQlAdapter.isNullable(): Boolean =
    type.isMarkedNullable

private object PrototypeContainer {
  val listProperty: Collection<*>? = emptyList<Any?>()
  val apexListType = PrototypeContainer::listProperty.returnType
}

