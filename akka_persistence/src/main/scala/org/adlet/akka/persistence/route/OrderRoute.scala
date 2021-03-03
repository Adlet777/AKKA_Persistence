package org.adlet.akka.persistence.route

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.adlet.akka.persistence.entity.OrderEntity
import org.adlet.akka.persistence.model.{OrderDTO, Summary}
import io.circe.parser.parse
import io.circe.{Json, parser}
import io.circe.generic.auto._
import io.circe.syntax._
import org.adlet.akka.persistence.command.{Choose, Enter}
import org.adlet.akka.persistence.event.Chosen
import org.adlet.akka.persistence.util.Codec

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}

class PostRoute(implicit system: ActorSystem[_], implicit val executionContext: ExecutionContext) extends Codec with FailFastCirceSupport {

  implicit private val timeout: Timeout = Timeout.create(system.settings.config.getDuration("askTimeout"))

  private val sharding = ClusterSharding(system)

  val routes: Route = {
    createOrderRoute
  }


  def createOrderRoute: Route = {
    pathPrefix("order" / "init") {
      post {
        entity(as[OrderDTO]) { entity =>

          val entityRef = sharding.entityRefFor(OrderEntity.typeKey, entity.product)

          val reply: Future[Summary] = entityRef.ask(
            Choose(
              product = entity.product,
              _,
              creditCard = entity.creditCard
            )
          )

          onSuccess(reply) { summary =>

            val message = HttpRequest(
              method = HttpMethods.POST,
              uri = "http://localhost:8080/api/v1/post/create",
              entity = HttpEntity(ContentTypes.`application/json`, summary.asJson.noSpaces)
            )

            val responseFuture: Future[HttpResponse] = Http().singleRequest(message)

            responseFuture
              .onComplete {
                case Success(res) => println(res)
                case Failure(_)   => sys.error("something wrong")
              }


            complete("OK")
          }
        }
      }
    }
  }


}