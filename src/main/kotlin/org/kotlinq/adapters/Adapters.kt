package org.kotlinq.adapters

import org.kotlinq.adapters.validation.isCollection
import org.kotlinq.adapters.validation.isList
import org.kotlinq.adapters.validation.isNullable
import org.kotlinq.api.Model
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

fun <Z> deserializer(type: KType, init: (java.io.InputStream) -> Z): GraphQlAdapter =
    DeserializingAdapter(type, init)

fun <Z> parser(type: KType, init: (String) -> Z): GraphQlAdapter =
    ParsingAdapter(type, init)

fun <Z : Model<*>> initializer(type: KType, init: () -> Z): GraphQlAdapter =
    ObjectAdapter(type, init)

fun <T: GraphQlAdapter> T.asCollection(): GraphQlAdapter =
    if (type.isCollection()) CollectionAdapterImpl(this) else this

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
    private val adapter: () -> Model<*>
) : Adapter() {

  private var backingField: Model<*>? = null

  override fun accept(input: Any): Boolean {
    (input as? Map<*, *>)?.map {

    }
    return backingField == null
  }

  override fun transform(input: Any): Any? {
    TODO("not implemented")
  }

  override fun getValue(): Any? {
    return backingField ?: if (!isNullable()) {
      // attempt to create the value before returning null
      adapter()
          .let { if (it.isResolved()) it else null }
          ?.also { backingField = it }
    } else null
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
    backingField = (adapter.invoke("$input")) ?: backingField
    return backingField != null || isNullable()
  }

  override fun transform(input: Any): Any? {
    TODO("not implemented")
  }

  override fun getValue() = backingField
}

private
class CollectionAdapterImpl(
    override val delegateAdapter: GraphQlAdapter,
    override val type: KType = delegateAdapter.type
) : CollectionAdapter {

  init {
    require(isList()) { "Illegal property type, must be a List<*> but found '$type'" }
  }

  private var backingField: List<*>? = null

  val dimensions = let {
    var count = 0
    var ktype: KType? = type
    while(ktype?.isCollection() == true)
      ktype = ktype.arguments.firstOrNull()?.type.also { count++ }
    count
  }

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

  override fun getValue() = backingField ?: emptyList<Any?>()

}
