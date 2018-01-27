import org.kotlinq.api.Model
import org.kotlinq.dsl.ArgBuilder
import org.kotlinq.static.ContextBuilder.Companion.schema
import org.kotlinq.static.enumMapper
import org.kotlinq.static.initialized

/*******************
 * Generated schema
 */

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

/*******************
 * Graph query implementations
 */
class BasicModelQuery : Model<BasicModel>(model = BasicModel) {
  val parsedResponse by model.response
}

class ContextDaoQuery : Model<ContextDao>(model = ContextDao) {

  val enumMapped by model.response

  val bazImpl by model.baz(::BasicModelQuery)

  val bazArgsImpl by model.bazWithArgs
      .withArguments(ContextDao.BazArgs())(::BasicModelQuery)

  val singleList by model.singleNestedListOfMode(::BasicModelQuery)
}

fun main(args: Array<String>) {

  val foo = ContextDaoQuery()

  require(foo.properties.values.find {
    !it.adapter.accept(

        when (it.propertyName) {
          "response" -> "NO"
          "baz" -> listOf(listOf(mapOf("response" to "NO")))
          "bazWithArgs" -> listOf(listOf(mapOf("response" to "YES")))
          "singleNestedListOfMode " -> listOf(mapOf("response" to "YES"))
          else -> null
        }

    )
  } == null)

  foo.apply {
    require(enumMapped == Response.NO)
    require(bazImpl.firstOrNull()?.firstOrNull()?.parsedResponse == Response.NO)
    require(bazArgsImpl.firstOrNull()?.firstOrNull()?.parsedResponse == Response.YES)
    require(singleList.firstOrNull()?.parsedResponse == Response.YES)
  }
}
