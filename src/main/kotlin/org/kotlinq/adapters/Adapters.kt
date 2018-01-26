package org.kotlinq.adapters

import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
fun <Z> deserializer(type: KType, init: (java.io.InputStream) -> Z): GraphQlAdapter<Z> =
    DeserializingAdapter(type, init) as GraphQlAdapter<Z>

@Suppress("UNCHECKED_CAST")
fun <Z> parser(type: KType, init: (String) -> Z): GraphQlAdapter<Z> =
    ParsingAdapter(type, init) as GraphQlAdapter<Z>

@Suppress("UNCHECKED_CAST")
fun <Z> initializer(type: KType, init: () -> Z): GraphQlAdapter<Z> =
    ObjectAdapter(init) as GraphQlAdapter<Z>

interface GraphQlAdapter<out T> {
  fun accept(input: Any): Boolean
  fun getValue(): T?
}

private
class ObjectAdapter(
    private val adapter: () -> Any?
) : GraphQlAdapter<Any?> {

  var _value: Any? = null

  override fun accept(input: Any): Boolean {
    @Suppress("UNCHECKED_CAST")
    _value = (input as? Map<*, *>)?.let {
      adapter()
    } ?: adapter()
    return _value == null
  }

  override fun getValue(): Any? {
    return _value ?: adapter().also { _value = adapter }
  }
}

private
class DeserializingAdapter(
    val type: KType,
    private val adapter: (java.io.InputStream) -> Any?
) : GraphQlAdapter<Any?> {

  var _value: Any? = null

  override fun accept(input: Any): Boolean {
    @Suppress("UNCHECKED_CAST")
    // todo interfaces for adapters
    _value = (input as? Any?)?.let { adapter(it.toString().byteInputStream()) } ?: _value
    return _value == null
  }

  override fun getValue() = _value
}

private
class ParsingAdapter(
    val type: KType,
    private val adapter: (String) -> Any?
) : GraphQlAdapter<Any?> {

  var _value: Any? = null

  override fun accept(input: Any): Boolean {
    @Suppress("UNCHECKED_CAST")
    // todo interfaces for adapters
    _value = (input as? Any?)?.let { adapter(it.toString()) } ?: _value
    return _value == null
  }

  override fun getValue() = _value
}
