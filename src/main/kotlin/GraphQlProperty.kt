import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType

interface GraphQlProperty<out T> : ReadOnlyProperty<Any, T> {
  val graphqlType: String
  val propertyName: String
  val adapter: GraphQlAdapter<T>
}

fun <T> graphQlProperty(
    name: String,
    type: KType,
    adapter: GraphQlAdapter<T>,
    default: T? = null
): GraphQlProperty<T> = object : GraphQlProperty<T> {

  override fun getValue(thisRef: Any, property: KProperty<*>): T =
      adapter.getValue() ?: default ?: adapter.getValue()!!
  override val graphqlType: String get() = type.toString()
  override val propertyName: String get() = name
  override val adapter: GraphQlAdapter<T> get() = adapter

}