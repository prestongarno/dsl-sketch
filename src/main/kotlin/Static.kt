import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ReadOnlyImpl<out T>(val value: T) : ReadOnlyProperty<Any, T> {
  override operator fun getValue(thisRef: Any, property: KProperty<*>): T = value
}

fun <T> readOnly(value: T): ReadOnlyProperty<Any, T> = TODO()

@PublishedApi internal inline fun <reified T : Any> stub(): StubProvider<T> = TODO()


@PublishedApi
internal
class StubProvider<U : Any>(val clazz: KClass<U>) {

  operator fun provideDelegate(
      inst: Any,
      property: KProperty<*>
  ): ReadOnlyProperty<Any, Stub<U>> =
      readOnly(Stub(property.name, clazz))
}