import kotlin.reflect.KClass

class Stub<A : Any>(val propertyName: String, val clazz: KClass<A>) {
  operator fun <Z> invoke(
      init: (A) -> Z,
      block: DslBuilder<Z, ArgBuilder>.() -> Unit = {}
  ): DelegateProvider<A, Z> = TODO()
}