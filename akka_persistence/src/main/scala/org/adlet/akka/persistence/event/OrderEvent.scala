package org.adlet.akka.persistence.event



trait OrderEvent{
}

case class Chosen(
                   product: String,
                   creditCard: Int
                                    ) extends OrderEvent

case class Entered(
                    product: String,
                    creditCard: Int
                                    ) extends OrderEvent