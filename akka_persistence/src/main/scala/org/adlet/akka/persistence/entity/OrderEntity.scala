package org.adlet.akka.persistence.entity

import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.Effect.persist
import akka.persistence.typed.{PersistenceId, scaladsl}
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}
import org.adlet.akka.persistence.command.{Choose, Enter, OrderCommand}
import org.adlet.akka.persistence.event.{Chosen, Entered, OrderEvent}

object OrderEntity {

case class Order(product: Option[String] = None,
                 creditCard: Option[Int] = None)

object Order{
  def empty = new Order()
}

trait OrderState

  object OrderState {
    case object PRODUCT extends OrderState  //choose product
    case object ASSIGN extends OrderState   //enter credit card number
  }

case class StateHolder(content: Order, state: OrderState) {

  def update(event: OrderEvent): StateHolder = event match {
    case evt: Chosen => {
      copy(
        content = content.copy(
          product = Some(evt.product)
        ),
        state = OrderState.ASSIGN
      )
    }
    case evt: Entered => {
      copy(
        content = content.copy(
          product = Some(evt.product),
          creditCard = Some(evt.creditCard)
        )
      )
    }
  }

}

  //initial state
object StateHolder {
  def empty: StateHolder = StateHolder(content = Order.empty, state = OrderState.PRODUCT)
}

  //type key
  val typeKey: EntityTypeKey[OrderCommand] = EntityTypeKey[OrderCommand]("Order")

def apply(id: String): Behavior[OrderCommand] = {
  EventSourcedBehavior[OrderCommand, OrderEvent, StateHolder](
    persistenceId = PersistenceId(typeKey.name, id),
    StateHolder.empty,
    (state, command) => commandHandler(id, state, command),
    (state, event) => EventHandler(state, event)
  )
    .withTagger{
      case evt: Chosen => Set("order", "product chosen")
      case evt: Entered => Set("order", "credit card number entered")
    }
    .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 1, keepNSnapshots = 2))
}

  def commandHandler(str: String, holder: OrderEntity.StateHolder, command: OrderCommand): Effect[OrderEvent, StateHolder] = {

    command match {
      case cmd: Choose => {
        holder.state match {
          case OrderState.PRODUCT => {
            val evt = Chosen (
              product = cmd.product
            )
            Effect.persist(evt)
          }
          case _ => throw new RuntimeException("Error")
        }
      }
      case cmd: Enter => {
        holder.state match {
          case OrderState.ASSIGN => {
            val evt = Entered (
              product = cmd.product,
              creditCard = cmd.creditCard
            )
            Effect.persist(evt)
          }
        }
      }
    }

  }

  def EventHandler(holder: OrderEntity.StateHolder, event: OrderEvent): StateHolder = {
    holder.update(event)
  }

}
