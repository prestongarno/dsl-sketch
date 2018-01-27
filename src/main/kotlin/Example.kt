import org.kotlinq.Model
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

object ContextDao {

  val response by enumMapper<Response>()

  val model2D by schema<BasicModel>()
      .asList()
      .asList()
      .build()

  val model2DWithArgs by schema<BasicModel>()
      .asList()
      .asList()
      .requiringArguments<BazArgs>()
      .build()

  val modelList by schema<BasicModel>()
      .asList()
      .build()

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

  val model2DImpl by model.model2D(::BasicModelQuery)

  val model2DArgsImpl by model.model2DWithArgs
      .withArguments(ContextDao.BazArgs())(::BasicModelQuery)

  val singleList by model.modelList(::BasicModelQuery)
}

fun main(args: Array<String>) {

  val foo = ContextDaoQuery()

  require(foo.properties.values.find {

    !it.adapter.accept(
        when (it.propertyName) {
          "response" -> "NO"
          "model2D" -> listOf(listOf(mapOf("response" to "NO")))
          "model2DWithArgs" -> listOf(listOf(mapOf("response" to "YES")))
          "modelList" -> listOf(mapOf("response" to "YES"))
          else -> null
        }

    )
  } == null)

  foo.apply {
    require(enumMapped == Response.NO)
    require(model2DImpl.firstOrNull()?.firstOrNull()?.parsedResponse == Response.NO)
    require(model2DArgsImpl.firstOrNull()?.firstOrNull()?.parsedResponse == Response.YES)
    require(singleList.firstOrNull()?.parsedResponse == Response.YES)
  }
}
