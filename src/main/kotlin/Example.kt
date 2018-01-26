import org.kotlinq.static.deserialized
import org.kotlinq.static.initialized

object FooDao {
  val scalar by initialized<Int>()
}

object ContextDao {
  val foo by initialized<FooDao>()
}


class BarImpl {
  val implFoo by FooDao.scalar({ 10000 })
  val implContextFoo by ContextDao.foo { FooDao }
}

fun main(args: Array<String>) {
  BarImpl().apply {
    require(implContextFoo == FooDao)
    require(implFoo == 1000)
  }
}
