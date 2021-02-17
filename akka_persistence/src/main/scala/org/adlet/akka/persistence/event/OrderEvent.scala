package org.adlet.akka.persistence.event

trait OrderEvent{
}

case class Chosen(
                   product: String
                                    ) extends OrderEvent

case class Entered(
                    product: String,
                    creditCard: Int
                                    ) extends OrderEvent