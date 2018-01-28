package org.kotlinq.api

import dagger.Module
import dagger.Provides
import javax.inject.Singleton


interface Transformer<out T> {
  fun transform(value: String): T
}

interface ObjectTransformer: Transformer<Map<String, String>>

interface ValueTransformer: Transformer<String>



@Module
@Singleton
class AdapterUtilities {

  @get:Provides
  val objectTransformer: ObjectTransformer = ObjectTransformerImpl()

  @get:Provides
  val valueTransformer: ValueTransformer = ValueTransformerImpl

}

internal
class ObjectTransformerImpl : ObjectTransformer {
  override fun transform(value: String): Map<String, String> {
    return mapOf("Hello" to "World").also { println(it) }
  }
}

internal
object ValueTransformerImpl : ValueTransformer {
  override fun transform(value: String): String {
    return null!!
  }

}
