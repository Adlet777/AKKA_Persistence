package org.adlet.akka.persistence

import akka.NotUsed
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.cassandra.testkit.CassandraLauncher
import com.typesafe.config.{Config, ConfigFactory}
import org.adlet.akka.persistence.entity.OrderEntity
import org.adlet.akka.persistence.route.{HttpRoutes, WebServer}
import org.adlet.akka.persistence.util.EventProcessorSettings

import java.io.File
import java.util.concurrent.CountDownLatch

object Boot {

  implicit val config: Config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    args.headOption match {

      case Some(portString) if portString.matches("""\d+""") =>
        val port = portString.toInt
        val httpPort = ("80" + portString.takeRight(2)).toInt
        startNode(port, httpPort)

      case Some("cassandra") =>
        startCassandraDatabase()
        println("Started Embedded Cassandra")
        new CountDownLatch(1).await()

      case None =>
        throw new IllegalArgumentException("port number, or cassandra required argument")
    }
  }

  def apply(): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      implicit val system = context.system

      val httpPort = context.system.settings.config.getInt("http-server.port")

      val settings = EventProcessorSettings(system)

      implicit val executionContext = system.executionContext

      OrderEntity.init(system, settings)


      val httpRoutes = new HttpRoutes()
      new WebServer(httpRoutes.routes, httpPort).start()


      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
  }


  def config(port: Int, httpPort: Int): Config =
    ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.port = $port
      http-server.port = $httpPort
       """).withFallback(ConfigFactory.load())


  def startNode(port: Int, httpPort: Int): Unit = {
    val system = ActorSystem(Boot(), "Template", config(port, httpPort))
  }


  def startCassandraDatabase(): Unit = {
    val databaseDirectory = new File("target/cassandra-db")
    CassandraLauncher.start(databaseDirectory, CassandraLauncher.DefaultTestConfigResource, clean = false, port = 9042)
  }

}