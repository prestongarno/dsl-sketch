import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class DelegateProvider<in A, Z>(val name: String, val init: (A) -> Z) : DslBuilder<Z, ArgBuilder> {

  private val args: ArgBuilder = ArgBuilder()

  override var default: Z? = null

  override fun config(block: ArgBuilder.() -> Unit) {
    args.block()
  }

  operator fun provideDelegate(inst: Any, property: KProperty<*>)
      : ReadOnlyProperty<Any, Z> =
      graphQlProperty(name, property.returnType, adapter(init), default)
}