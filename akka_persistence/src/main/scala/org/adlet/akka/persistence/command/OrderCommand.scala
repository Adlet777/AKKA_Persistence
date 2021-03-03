package org.adlet.akka.persistence.command

import akka.actor.typed.ActorRef
import org.adlet.akka.persistence.model.Summary

trait OrderCommand {
  def product: String
  def creditCard: Int
}

case class Choose(
                   product: String,
                   replyTo: ActorRef[Summary],
                   creditCard: Int
                                    ) extends OrderCommand

case class Enter(
                  product: String,
                  creditCard: Int,

                                    ) extends OrderCommand
