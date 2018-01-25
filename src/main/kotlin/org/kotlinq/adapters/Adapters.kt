package org.kotlinq.adapters

fun <A, Z> adapter(init: (A) -> Z): GraphQlAdapter<Z> =
    DeserializingAdapter(init)

fun <Z> adapter(init: () -> Z): GraphQlAdapter<Z> =
    ObjectAdapter(init)

interface GraphQlAdapter<out T> {
  fun accept(input: Any): Boolean
  fun getValue(): T?
}

private
class ObjectAdapter<T>(
    private val adapter: () -> T
) : GraphQlAdapter<T> {

  var _value: T? = null

  override fun accept(input: Any): Boolean {
    @Suppress("UNCHECKED_CAST")
    _value = (input as? Map<*, *>)?.let {
      adapter() /* <--TODO type constraint takes map*/
    } ?: _value
    return _value == null
  }

  override fun getValue(): T? {
    return _value
  }
}

private
class DeserializingAdapter<in A, T>(
    private val adapter: (A) -> T
) : GraphQlAdapter<T> {

  var _value: T? = null

  override fun accept(input: Any): Boolean {
    @Suppress("UNCHECKED_CAST")
    _value = (input as? A)?.let { adapter(it) } ?: _value
    return _value == null
  }

  override fun getValue(): T? {
    return _value
  }
}