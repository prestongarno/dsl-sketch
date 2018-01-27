import org.kotlinq.Model
import org.kotlinq.delegates.CollectionStubN
import org.kotlinq.dsl.ArgBuilder
import org.kotlinq.static.ContextBuilder.Companion.schema
import org.kotlinq.static.enumMapper

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

  val modelNDimensions: CollectionStubN<Model<BasicModel>, List<List<List<List<Model<BasicModel>>>>>> by schema<BasicModel>()
      .asList()
      .asList()
      .asList()
      .asList()
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

  // erased actual type after >2 nested lists
  val modelNDimenstion by model.modelNDimensions { BasicModelQuery() }

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
          "modelNDimensions" -> {
            listOf(mapOf("response" to "NO"))
                .nested()
                .nested()
                .nested()
          }
          else -> null
        }

    )
  } == null)

  foo.apply {
    require(enumMapped == Response.NO)
    require(model2DImpl.firstOrNull()?.firstOrNull()?.parsedResponse == Response.NO)
    require(model2DArgsImpl.firstOrNull()?.firstOrNull()?.parsedResponse == Response.YES)
    require(singleList.firstOrNull()?.parsedResponse == Response.YES)

    val passesNDimensionTest = modelNDimenstion.firstOrNull()
        ?.firstOrNull()
        ?.firstOrNull()
        ?.firstOrNull()
        ?.let { it as? BasicModelQuery }
        ?.let {
          it.parsedResponse == Response.NO
        } ?: false

    require(passesNDimensionTest) // !!!
  }
}

internal
fun <T> List<T>.nested() = listOf(this)
