package org.kotlinq.adapters

import org.kotlinq.adapters.validation.isList
import org.kotlinq.adapters.validation.isNullable
import kotlin.reflect.KType

fun <Z> deserializer(type: KType, init: (java.io.InputStream) -> Z): GraphQlAdapter =
    DeserializingAdapter(type, init)

fun <Z> parser(type: KType, init: (String) -> Z): GraphQlAdapter =
    ParsingAdapter(type, init)

fun <Z> initializer(type: KType, init: () -> Z): GraphQlAdapter =
    ObjectAdapter(type, init)

interface GraphQlAdapter {
  val type: KType
  /* Takes the value [input] and returns the result of setting the value on this property */
  fun accept(input: Any): Boolean
  /** Take [input] and return the result of deserializing as an object */
  fun transform(input: Any): Any?
  fun getValue(): Any?
}


interface CollectionAdapter : GraphQlAdapter {
  val delegateAdapter: GraphQlAdapter
}

private
sealed class Adapter : GraphQlAdapter {
  override fun toString() = "::$type"
}

private
class ObjectAdapter(
    override val type: KType,
    private val adapter: () -> Any?
) : Adapter() {

  private var backingField: Any? = null

  override fun accept(input: Any): Boolean {
    @Suppress("UNCHECKED_CAST")
    backingField = (input as? Map<*, *>)?.let {
      adapter()
    } ?: adapter()
    return backingField == null
  }

  override fun transform(input: Any): Any? {
    TODO("not implemented")
  }

  override fun getValue(): Any? {
    return backingField ?: adapter().also { backingField = adapter }
  }
}

private
class DeserializingAdapter(
    override val type: KType,
    private val adapter: (java.io.InputStream) -> Any?
) : Adapter() {
  private var backingField: Any? = null

  override fun accept(input: Any): Boolean {
    @Suppress("UNCHECKED_CAST")
    // todo interfaces for adapters
    backingField = (input as? Any?)?.let { adapter(it.toString().byteInputStream()) } ?: backingField
    return backingField == null
  }

  override fun transform(input: Any): Any? {
    TODO("not implemented")
  }

  override fun getValue() = backingField
}

private
class ParsingAdapter(
    override val type: KType,
    private val adapter: (String) -> Any?
) : Adapter() {
  private var backingField: Any? = null

  override fun accept(input: Any): Boolean {
    @Suppress("UNCHECKED_CAST")
    // todo interfaces for adapters
    backingField = (input as? Any?)?.let { adapter(it.toString()) } ?: backingField
    return backingField == null
  }

  override fun transform(input: Any): Any? {
    TODO("not implemented")
  }

  override fun getValue() = backingField
}

private
class CollectionAdapterImpl(
    override val type: KType,
    override val delegateAdapter: GraphQlAdapter
) : CollectionAdapter {

  init {
    require(isList()) { "Illegal property type, must be a List<*> but found '$type'"}
  }

  private var backingField: List<*>? = null

  override fun accept(input: Any): Boolean {

    backingField = (input as? Collection<*>)
        ?.filterNotNull()
        ?.map(delegateAdapter::transform)
        ?: transform(listOf(input))
        ?: if (!isNullable()) emptyList<Any>() else null

    return isNullable() || backingField != null
  }

  override fun transform(input: Any): List<*>? = (input as? Collection<*>)
      ?.filterNotNull()
      ?.map(delegateAdapter::transform)
      ?: if (!isNullable()) emptyList<Any>() else null

  override fun getValue() = backingField

}
