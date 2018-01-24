import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class PropertyAdapter<in A, T>(
    val adapter: (A) -> T,
    val default: T? = null
) : ReadOnlyProperty<Any, T> {

  var value: T? = null

  fun deserialize(input: Any) {
    value = (input as? A)?.let { adapter(it) } ?: value
  }

  override operator fun getValue(thisRef: Any, property: KProperty<*>): T = value ?: default!!
}