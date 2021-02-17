package org.adlet.akka.persistence.command

trait OrderCommand {
  def product: String
}

case class Choose(
                   product: String
                                    ) extends OrderCommand

case class Enter(
                  product: String,
                  creditCard: Int
                                    ) extends OrderCommand
