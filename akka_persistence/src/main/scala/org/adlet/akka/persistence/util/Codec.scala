package org.adlet.akka.persistence.util

import org.adlet.akka.persistence.model.{OrderDTO, Summary}

trait Codec {

  implicit val employeeCreateEncodeDecode: EncoderDecoder[OrderDTO] = DerivedEncoderDecoder[OrderDTO]

  implicit val summaryEncodeDecode: EncoderDecoder[Summary] = DerivedEncoderDecoder[Summary]

}
