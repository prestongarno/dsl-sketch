package org.kotlinq.api

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import org.kotlinq.Model
import org.kotlinq.adapters.AdapterFactory
import org.kotlinq.adapters.GraphQlAdapter
import org.kotlinq.adapters.GraphQlProperty
import org.kotlinq.adapters.ModelPropertyImpl
import org.kotlinq.adapters.graphQlProperty
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KType



@Module
@Reusable
internal
class PropertyProvider @Inject constructor(val adapterFactory: AdapterFactory) {
  fun <T : Model<*>> newTypeProperty(name: String, type: KType, init: () -> T): GraphQlProperty<T> =
      graphQlProperty(name, adapterFactory.initializer(type, init))
}
