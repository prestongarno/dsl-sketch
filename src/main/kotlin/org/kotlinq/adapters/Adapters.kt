package org.kotlinq.adapters

import org.kotlinq.adapters.validation.isCollection
import org.kotlinq.adapters.validation.isList
import org.kotlinq.adapters.validation.isNullable
import org.kotlinq.Model
import kotlin.reflect.KType

fun <Z> deserializer(type: KType, init: (java.io.InputStream) -> Z): GraphQlAdapter =
    DeserializingAdapter(type, init)

fun <Z> parser(type: KType, init: (String) -> Z): GraphQlAdapter =
    ParsingAdapter(type, init)

fun <Z : Model<*>> initializer(type: KType, init: () -> Z): GraphQlAdapter =
    ObjectAdapter(type, init)

fun <T : GraphQlAdapter> T.asCollection(): GraphQlAdapter {
  var adapter: GraphQlAdapter = this
  for (i in 1..type.dimensions()) {
    adapter = CollectionAdapterImpl(adapter)
  }
  return adapter
}

interface GraphQlAdapter {
  val type: KType
  /* Takes the value [input] and returns the result of setting the value on this property */
  fun accept(input: Any?): Boolean

  /** Take [input] and return the result of deserializing as an object */
  fun transform(input: Any?): Any?

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

  override fun accept(input: Any?): Boolean {
    backingField = transform(input) as? Model<*>
    return backingField == null
  }

  override fun transform(input: Any?): Any? = (input as? Map<*, *>)?.entries?.let { entries ->
    val init = adapter()
    entries.forEach { (k, v) ->
      init.properties["$k"]?.adapter?.accept(v ?: Unit)
    }
    init
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

  override fun accept(input: Any?): Boolean {
    TODO("todo implement proper deserialization")
  }

  override fun transform(input: Any?): Any? {
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

  override fun accept(input: Any?): Boolean {
    backingField = (adapter.invoke("$input")) ?: backingField
    return backingField != null || isNullable()
  }

  override fun transform(input: Any?): Any? {
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

  override fun accept(input: Any?): Boolean {
    println(type.dimensions())
    backingField = (input as? List<*>)
        ?.filterNotNull()
        ?.map { delegateAdapter.transform(it) }
        ?: if (!isNullable()) emptyList<Any>() else null

    return isNullable() || backingField != null
  }

  override fun transform(input: Any?): List<*>? = (input as? List<*>)
      ?.filterNotNull()
      ?.map(delegateAdapter::transform)
      ?: if (!isNullable()) emptyList<Any>() else null

  override fun getValue() = backingField ?: emptyList<Any?>()

}

private
fun KType.dimensions(): Int {
  var count = 0
  var ktype: KType? = this
  while (ktype?.isCollection() == true)
    ktype = ktype.arguments.firstOrNull()?.type.also { count++ }
  return count
}
