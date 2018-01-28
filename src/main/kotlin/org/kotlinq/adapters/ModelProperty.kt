package org.kotlinq.adapters

import org.kotlinq.Model
import org.kotlinq.api.ObjectTransformer
import org.kotlinq.api.TypeAdapter
import javax.inject.Inject
import kotlin.reflect.KType

internal
class ModelPropertyImpl(
    override val type: KType,
    private val init: () -> Model<*>
) : GraphQlAdapter {

  @set:Inject lateinit var objectDeserializer: TypeAdapter

  private var value: Model<*>? = null

  override fun accept(input: String): Boolean {
    value = transform(input)
    return value?.isResolved() ?: type.isMarkedNullable
  }

  override fun getValue(): Model<*>? = value

  override fun transform(input: String): Model<*>? =
      objectDeserializer.createFromString(input, init)

}
