import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.numeric.{Divisible, Even, Positive}

type PositiveInt = Int Refined Positive
object PositiveInt extends RefinedTypeOps[PositiveInt, Int]

PositiveInt.from(-6)
PositiveInt.from(3)
PositiveInt.from(4)
PositiveInt.from(6)

type PositiveEvenInt = Int Refined (Positive And Even)
object PositiveEvenInt extends RefinedTypeOps[PositiveEvenInt, Int]

PositiveEvenInt.from(-6)
PositiveEvenInt.from(3)
PositiveEvenInt.from(4)
PositiveEvenInt.from(6)

type PositiveDivisibleBySixInt = Int Refined (Positive And Even And Divisible[3])
object PositiveDivisibleBySixInt extends RefinedTypeOps[PositiveDivisibleBySixInt, Int]

PositiveDivisibleBySixInt.from(-6)
PositiveDivisibleBySixInt.from(3)
PositiveDivisibleBySixInt.from(4)
PositiveDivisibleBySixInt.from(6)
