package org.adlet.akka.persistence.route

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

class HttpRoutes()(implicit system: ActorSystem[_], implicit val executionContext: ExecutionContext, config: Config) {

  val routes: Route = pathPrefix("api") {
    pathPrefix("v1") {
      concat(
        pathEndOrSingleSlash {
          get {
            complete("Welcome to akka persistence template!")
          }
        },

        new PostRoute().routes
      )
    }
  }

}