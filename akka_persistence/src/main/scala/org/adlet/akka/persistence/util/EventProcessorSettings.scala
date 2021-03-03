package org.adlet.akka.persistence.util

import akka.actor.typed.ActorSystem
import com.typesafe.config.Config

import scala.concurrent.duration._

object EventProcessorSettings {

  def apply(system: ActorSystem[_]): EventProcessorSettings =
    apply(system.settings.config.getConfig("event-processor"))

  def apply(config: Config): EventProcessorSettings = {
    val id: String                        = config.getString("id")
    val keepAliveInterval: FiniteDuration = config.getDuration("keep-alive-interval").toMillis.millis
    val tagPrefix: String                 = config.getString("tag-prefix")
    val parallelism: Int                  = config.getInt("parallelism")
    EventProcessorSettings(id, keepAliveInterval, tagPrefix, parallelism)
  }
}

final case class EventProcessorSettings(id: String,
                                        keepAliveInterval: FiniteDuration,
                                        tagPrefix: String,
                                        parallelism: Int)