package org.adlet.akka.persistence.serializable.proto

import akka.serialization.SerializerWithStringManifest
import org.adlet.akka.persistence.event.proto.{ChosenV1, EnteredV1}

class OrderEventSerializer extends SerializerWithStringManifest{

final val OrderCreateEventManifestV1: String = classOf[ChosenV1].getName

  override def identifier: Int = 1000

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match{
    case evt: ChosenV1 => evt.toByteArray
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case OrderCreateEventManifestV1 => ChosenV1.parseFrom(bytes)
  }
}