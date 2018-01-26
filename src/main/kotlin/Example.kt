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

  val baz by schema<Int>()
      .asList()
      .asNullable()
      .asList()
      .asList()
      .asNullable()
      .asList()
      .requiringArguments<BazArgs>()
      .build()

  class BazArgs : ArgBuilder() {
    var stringArgumentOptional: String? = null
  }
}


class BarImpl : Model<ContextDao>(ContextDao) {
  val implFoo by FooDao.scalar({ 10000 })
  val implContextFoo by ContextDao.foo { FooDao }
  val enumMapped by ContextDao.response

  val bazImpl by ContextDao.baz.withArguments(ContextDao.BazArgs())({ -1 }) {
    default = 9000
    config {
      take("Hello" to "world")
    }
  }
}

fun main(args: Array<String>) {
  val foo = BarImpl()

  foo.properties.forEach {
    println(it.kotlinType)
  }

  foo.apply {
    require(implContextFoo == FooDao)
    require(implFoo == 10000)
    require(bazImpl
        .first()
        .first()
        ?.first()
        ?.first()
        ?.first()
        ?.first()?.let { it - 1 } == 0)
  }
}
