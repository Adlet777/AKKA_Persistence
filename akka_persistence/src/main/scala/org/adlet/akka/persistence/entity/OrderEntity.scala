package org.adlet.akka.persistence.entity

import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.Effect.persist
import akka.persistence.typed.{PersistenceId, scaladsl}
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}
import org.adlet.akka.persistence.adapter.OrderEventAdapter
import org.adlet.akka.persistence.command.{Choose, Enter, OrderCommand}
import org.adlet.akka.persistence.event.{Chosen, Entered, OrderEvent}
import org.adlet.akka.persistence.model.Summary
import org.adlet.akka.persistence.util.EventProcessorSettings


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
          product = Some(evt.product),
          creditCard = Some(evt.creditCard)
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

  def init(system: ActorSystem[_], eventProcessorSettings: EventProcessorSettings): Unit = {

    ClusterSharding(system).init(Entity(typeKey) { entityContext =>
      val n = math.abs(entityContext.entityId.hashCode % eventProcessorSettings.parallelism)
      val eventProcessorTag = eventProcessorSettings.tagPrefix + "-" + n
      OrderEntity(entityContext.entityId, Set(eventProcessorTag))
    })

  }

def apply(id: String, eventProcessorTag: Set[String]): Behavior[OrderCommand] = {
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
    .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 10, keepNSnapshots = 2))
    .eventAdapter(new OrderEventAdapter)
}

  def commandHandler(PersistenceId: String, holder: OrderEntity.StateHolder, command: OrderCommand): Effect[OrderEvent, StateHolder] = {

    command match {
      case cmd: Choose => {
        holder.state match {
          case OrderState.PRODUCT => {
            val evt = Chosen (
              product = cmd.product,
              creditCard = cmd.creditCard
            )
            Effect.persist(evt).thenReply(cmd.replyTo)(_=>
            Summary(
              product = cmd.product,
              creditCard = cmd.creditCard
            )
            )
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
