package org.kotlinq.static

import org.kotlinq.delegates.DisjointCollection
import org.kotlinq.delegates.DisjointCollectionStub
import org.kotlinq.delegates.DisjointCollectionStub1
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

  @Suppress("UNCHECKED_CAST")
  fun asList(): DisjointDelegateBuilder<DisjointCollectionStub<T>, T> =
      DisjointDelegateBuilder(DisjointCollectionStub::class as KClass<DisjointCollectionStub<T>>)

  fun asNullable(): NullableTypeProvider<T> = NullableTypeProvider()

  companion object {
    fun <T : Any> schema(): ContextBuilder<T, ArgumentSpec, GraphQlPropertyStub> = ContextBuilder()
  }
}

class ConfiguredContextBuilder<T : GraphQlPropertyStub, in A : ArgumentSpec>(val clazz: KClass<T>) {
  fun build(): PredicateProvider<A, T> = using<A>().build(clazz)
}

class DisjointDelegateBuilder<T : DisjointCollection, Z>(val clazz: KClass<T>) {

  @Suppress("UNCHECKED_CAST")
  fun <A : ArgumentSpec> requiringArguments(): ConfiguredContextBuilder<T, A> =
      ConfiguredContextBuilder(clazz)

  @Suppress("UNCHECKED_CAST")
  fun asList(): DisjointDelegateBuilder<DisjointCollectionStub1<Z>, Z> =
      DisjointDelegateBuilder(DisjointCollectionStub1::class as KClass<DisjointCollectionStub1<Z>>)

  //fun asNullable(): DisjointDelegateBuilder<T, Z> = DisjointDelegateBuilder()

  fun build(): Provider<T> = Provider.disjoint(this)
}

class NullableTypeProvider<T : Any> {
  fun <A : ArgumentSpec> requiringArguments(): ConfiguredContextBuilder<GraphQlPropertyStub.Disjoint<T>, A> = TODO()
  fun build(): GraphQlPropertyStub.Disjoint<T> = TODO()
}
