import org.kotlinq.static.deserialized
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
}


class BarImpl {
  val implFoo by FooDao.scalar({ 10000 })
  val implContextFoo by ContextDao.foo { FooDao }
  val enumMapped by ContextDao.response
}

fun main(args: Array<String>) {
  val foo = BarImpl()
  foo.apply {
    require(implContextFoo == FooDao)
    require(implFoo == 10000)
  }
}
