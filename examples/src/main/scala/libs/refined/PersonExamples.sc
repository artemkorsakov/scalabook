type Packed = Any

val nonEmpty: Packed => Boolean =
  case str: String => Option(str).exists(_.trim.nonEmpty)
  case num: Int    => num > 0
  case _           => false

import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.boolean.*
import eu.timepit.refined.collection.*

given Validate[Packed, NonEmpty] with
  override type R = NonEmpty
  override def validate(packed: Packed): Res    =
    Result.fromBoolean(nonEmpty(packed), Not(Empty()))
  override def showExpr(packed: Packed): String = s"Empty packed value: $packed"

refineV[NonEmpty](null: Packed)
refineV[NonEmpty]("": Packed)
refineV[NonEmpty](" ": Packed)
refineV[NonEmpty]("   ": Packed)
refineV[NonEmpty](0: Packed)
refineV[NonEmpty](-42: Packed)
refineV[NonEmpty](true: Packed)

refineV[NonEmpty]("value": Packed)
refineV[NonEmpty](42: Packed)
