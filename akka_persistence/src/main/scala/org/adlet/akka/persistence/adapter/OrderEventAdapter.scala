package org.adlet.akka.persistence.adapter

import akka.persistence.typed.{EventAdapter, EventSeq}
import org.adlet.akka.persistence.event.{Chosen, Entered, OrderEvent}
import org.adlet.akka.persistence.event.proto.{ChosenV1, EnteredV1}


class OrderEventAdapter extends EventAdapter[OrderEvent, OrderWrapper]{


  override def toJournal(e: OrderEvent): OrderWrapper = {
    val protoEvent = e match {
      case evt: Chosen =>{
        ChosenV1(
          product = evt.product,
          creditCard = evt.creditCard.toString
        )
      }
        case evt: Entered =>{
          EnteredV1(
          product = evt.product,
          creditCard = evt.creditCard.toString
          )
        }
    }
    OrderWrapper(protoEvent)
  }

  override def manifest(event: OrderEvent): String = ""

  override def fromJournal(p: OrderWrapper, manifest: String): EventSeq[OrderEvent] = {
    p.event match {
      case evt: ChosenV1 => {
        EventSeq.single(
            Chosen(
              product = evt.product,
              creditCard = evt.creditCard.toInt
            )
        )
      }

      case evt: EnteredV1 => {
        EventSeq.single(
          Entered(
            product = evt.product,
            creditCard = evt.creditCard.toInt
          )
        )
      }

    }
  }


}
