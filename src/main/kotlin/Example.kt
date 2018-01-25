import org.kotlinq.static.stub

object FooDao {
  val scalar by stub<Int>()
}

object ContextDao {
  val foo by stub<FooDao>()
}


class BarImpl {
  val implFoo by FooDao.scalar({ 10000 })
  val implContextFoo by ContextDao.foo { FooDao }
}

fun main(args: Array<String>) {
  BarImpl().apply {
    require(implFoo == 1000)
    require(implContextFoo == FooDao)
  }
}
