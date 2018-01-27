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

  val response by enumMapper<Response>()

  val baz by schema<BasicModel>()
      .asList()
      .asList()
      .build()

  val bazWithArgs by schema<BasicModel>()
      .asList()
      .asList()
      .requiringArguments<BazArgs>()
      .build()

  val singleNestedListOfMode by schema<BasicModel>().asList().build()

  class BazArgs : ArgBuilder() {
    var stringArgumentOptional: String? = null
  }
}

object BasicModel {
  val response by enumMapper<Response>()
}

class ModelInt : Model<BasicModel>(model = BasicModel) {
  val parsedResponse by model.response
}

class BarImpl : Model<ContextDao>(model = ContextDao) {

  val enumMapped by model.response

  val bazImpl by model.baz(::ModelInt)

  val bazArgsImpl by model.bazWithArgs
      .withArguments(ContextDao.BazArgs())(::ModelInt)

  val singleList by model.singleNestedListOfMode(::ModelInt)
}

fun main(args: Array<String>) {
  val foo = BarImpl()

  foo.properties.values.forEach {
    println(it.propertyName + ": " + it.kotlinType)
    when {
      it.propertyName == "response" -> require(it.adapter.accept("NO"))
      it.propertyName == "baz" -> require(it.adapter.accept(listOf(listOf(mapOf("response" to "NO")))))
      it.propertyName == "bazWithArgs" -> require(it.adapter.accept(listOf(listOf(mapOf("response" to "YES")))))
      it.propertyName == "singleNestedListOfMode " -> require(it.adapter.accept(listOf(mapOf("response" to "YES"))))
    }
  }

  foo.apply {
    require(enumMapped == Response.NO)
    require(bazImpl.firstOrNull()?.firstOrNull()?.parsedResponse == Response.NO)
    require(bazArgsImpl.firstOrNull()?.firstOrNull()?.parsedResponse == Response.YES)
    require(singleList.firstOrNull()?.parsedResponse == Response.YES)
  }
}
