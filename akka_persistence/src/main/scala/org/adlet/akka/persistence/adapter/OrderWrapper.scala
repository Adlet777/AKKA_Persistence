package org.adlet.akka.persistence.adapter

import org.adlet.akka.persistence.model.protobuf.OrderProtoEvent

case class OrderWrapper(event: OrderProtoEvent)
