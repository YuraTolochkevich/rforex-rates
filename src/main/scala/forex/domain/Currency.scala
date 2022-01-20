package forex.domain

import cats.Show

import enumeratum.EnumEntry
import enumeratum._

import scala.collection.immutable

sealed abstract class Currency(val value: String) extends EnumEntry

object Currency extends Enum[Currency] {
  val values: immutable.IndexedSeq[Currency] = findValues
  def all = ???
  case object AUD extends Currency("AUD")
  case object CAD extends Currency("CAD")
  case object CHF extends Currency("CHF")
  case object EUR extends Currency("EUR")
  case object GBP extends Currency("GBP")
  case object NZD extends Currency("NZD")
  case object JPY extends Currency("JPY")
  case object SGD extends Currency("SGD")
  case object USD extends Currency("USD")

  implicit val show: Show[Currency] = Show.show {
    case AUD => "AUD"
    case CAD => "CAD"
    case CHF => "CHF"
    case EUR => "EUR"
    case GBP => "GBP"
    case NZD => "NZD"
    case JPY => "JPY"
    case SGD => "SGD"
    case USD => "USD"
  }

  def fromString(s: String): Option[Currency] = s.toUpperCase match {
    case "AUD" => Some(AUD)
    case "CAD" => Some(CAD)
    case "CHF" => Some(CHF)
    case "EUR" => Some(EUR)
    case "GBP" => Some(GBP)
    case "NZD" => Some(NZD)
    case "JPY" => Some(JPY)
    case "SGD" => Some(SGD)
    case "USD" => Some(USD)
    case _     => None
  }

}
