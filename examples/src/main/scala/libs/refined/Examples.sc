import eu.timepit.refined.*
import eu.timepit.refined.api.{RefType, Refined}
import eu.timepit.refined.auto.*
import eu.timepit.refined.boolean.*
import eu.timepit.refined.char.*
import eu.timepit.refined.collection.*
import eu.timepit.refined.generic.*
import eu.timepit.refined.numeric.*
import eu.timepit.refined.string.*
import shapeless.{::, HNil}

val x = 42
refineV[Positive](x)
refineV[Positive](-x)

refineV[NonEmpty]("Hello")
refineV[NonEmpty]("")

type ZeroToOne = Not[Less[0.0]] And Not[Greater[1.0]]
refineV[ZeroToOne](0.8)
refineV[ZeroToOne](1.8)

refineV[AnyOf[Digit :: Letter :: Whitespace :: HNil]]('F')

refineV[MatchesRegex["[0-9]+"]]("123.")

type Age = Int Refined Interval.ClosedOpen[7, 77]
val userInput                       = 55
val ageEither1: Either[String, Age] = refineV(userInput)
val ageEither2                      = RefType.applyRef[Age](userInput)
