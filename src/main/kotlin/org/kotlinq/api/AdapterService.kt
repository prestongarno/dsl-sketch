package org.kotlinq.api

import dagger.Module
import dagger.Provides
import org.kotlinq.Model
import javax.inject.Inject
import javax.inject.Singleton

interface AdapterService {

  val typeAdapter: TypeAdapter

  val listAdapter: ListAdapter

  @Module
  @Singleton
  companion object : AdapterService {

    @Inject fun inject(utilities: AdapterUtilities) { lazyUtil = lazy { utilities } }

    private var lazyUtil = lazy(::AdapterUtilities)

    internal val utilities get() = lazyUtil.value

    @get:Provides
    override val typeAdapter: TypeAdapter by lazy {
      TypeAdapterImpl(utilities.objectTransformer)
    }

    @get:Provides
    override val listAdapter: ListAdapter by lazy {
      ListAdapterImpl(this@Companion)
    }
  }

}

interface TypeAdapter {
  val transformer: ObjectTransformer
  fun <T : Model<*>> createFromString(value: String, init: () -> T?): T?
}

interface ListAdapter {
  val transformer: TypeAdapter
  fun <T : Model<*>> createFromList(values: List<String>, init: () -> T?): List<T>
}

class TypeAdapterImpl(override val transformer: ObjectTransformer) : TypeAdapter {

  override fun <T : Model<*>> createFromString(value: String, init: () -> T?): T? {
    val result = transformer.transform(value)
    val model = init()

    model?.properties?.forEach { (key, value) ->
      value.adapter.accept(result[key] ?: "")
    }
    return if (model?.isResolved() == true) model else null
  }
}

class ListAdapterImpl @Inject constructor(service: AdapterService) : ListAdapter {

  val service = service

  override val transformer: TypeAdapter get() = service.typeAdapter

  override fun <T : Model<*>> createFromList(values: List<String>, init: () -> T?) =
      values.mapNotNull { transformer.createFromString(it, init) }
}