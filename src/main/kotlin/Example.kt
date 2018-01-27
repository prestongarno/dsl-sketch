import org.kotlinq.api.Model
import org.kotlinq.dsl.ArgBuilder
import org.kotlinq.static.ContextBuilder.Companion.schema
import org.kotlinq.static.enumMapper
import org.kotlinq.static.initialized

enum class Response {
  YES, NO, MAYBE
}

object FooDao {
  val scalar by initialized<Int>()
}

object ContextDao {
  val foo by initialized<FooDao>()
  val response by enumMapper<Response>()

  val baz by schema<String>()
      .asList()
      .asList()
      .build()

  class BazArgs : ArgBuilder() {
    var stringArgumentOptional: String? = null
  }
}


class ModelInt : Model<String>("Hello world")

class BarImpl : Model<ContextDao>(ContextDao) {
  //val implFoo by FooDao.scalar({ 10000 })
  //val implContextFoo by ContextDao.foo { FooDao }
  val enumMapped by ContextDao.response

  val bazImpl by ContextDao.baz(::ModelInt) {
    println(this::class)
    config {
      take("Hello" to "world")
    }
  }
}

fun main(args: Array<String>) {
  val foo = BarImpl()

  foo.properties.values.forEach {
    println(it.kotlinType)
    if (it.propertyName == "response") {
      require(it.adapter.accept("NO"))
      println(it.adapter.getValue())
    }
  }

  foo.apply {
    require(enumMapped == Response.NO)
    require(bazImpl.isEmpty())
  }
}
