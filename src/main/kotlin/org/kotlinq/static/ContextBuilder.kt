package org.kotlinq.static

import org.kotlinq.delegates.DisjointCollectionStub
import org.kotlinq.delegates.GraphQlPropertyStub
import org.kotlinq.delegates.InitializingStub
import org.kotlinq.dsl.ArgumentSpec
import org.kotlinq.static.PredicateProvider.Companion.using
import kotlin.reflect.KClass


class ContextBuilder<T : Any, out A : ArgumentSpec, S : GraphQlPropertyStub> {

  @Suppress("UNCHECKED_CAST") fun <B : ArgumentSpec> requiringArguments()
      : ConfiguredContextBuilder<InitializingStub<T>, B> =
      ConfiguredContextBuilder<InitializingStub<T>, A>(InitializingStub::class as KClass<InitializingStub<T>>)
          as ConfiguredContextBuilder<InitializingStub<T>, B>

  fun asList(): DisjointDelegateBuilder<T, List<T>> = DisjointDelegateBuilder()

  fun asNullable(): NullableTypeProvider<T> = NullableTypeProvider()

  companion object {
    fun <T : Any> schema(): ContextBuilder<T, ArgumentSpec, GraphQlPropertyStub> = ContextBuilder()
  }
}

class ConfiguredContextBuilder<T : GraphQlPropertyStub, in A : ArgumentSpec>(val clazz: KClass<T>) {
  fun build(): PredicateProvider<A, T> = using<A>().build(clazz)
}

class DisjointDelegateBuilder<T, U : List<*>> {

  @Suppress("UNCHECKED_CAST")
  fun <A : ArgumentSpec> requiringArguments(): ConfiguredContextBuilder<DisjointCollectionStub<T, U>, A> =
      ConfiguredContextBuilder(DisjointCollectionStub::class as KClass<DisjointCollectionStub<T, U>>)

  fun asList(): DisjointDelegateBuilder<T, List<U>> = DisjointDelegateBuilder()
  fun asNullable(): DisjointDelegateBuilder<T, List<U?>> = DisjointDelegateBuilder()
  fun build(): CollectionProvider<T, U> = CollectionProvider.new(this)
}

class NullableTypeProvider<T : Any> {
  fun <A : ArgumentSpec> requiringArguments(): ConfiguredContextBuilder<GraphQlPropertyStub.Disjoint<T>, A> = TODO()
  fun build(): GraphQlPropertyStub.Disjoint<T> = TODO()
}
