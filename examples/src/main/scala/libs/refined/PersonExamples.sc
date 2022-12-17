import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.boolean.*
import eu.timepit.refined.collection.*

final case class Packed(value: Any):
  lazy val nonEmpty: Boolean =
    value match
      case str: String => Option(str).exists(_.trim.nonEmpty)
      case num: Int    => num > 0
      case _           => false

given Validate[Packed, NonEmpty] with
  override type R = NonEmpty
  override def validate(packed: Packed): Res    =
    Result.fromBoolean(packed.nonEmpty, Not(Empty()))
  override def showExpr(packed: Packed): String = s"Empty packed value: $packed"

refineV[NonEmpty](Packed(null))
refineV[NonEmpty](Packed(""))
refineV[NonEmpty](Packed(" "))
refineV[NonEmpty](Packed("   "))
refineV[NonEmpty](Packed(0))
refineV[NonEmpty](Packed(-42))
refineV[NonEmpty](Packed(true))

refineV[NonEmpty](Packed("value"))
refineV[NonEmpty](Packed(42))
