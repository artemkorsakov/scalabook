import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.auto.*
import eu.timepit.refined.boolean.*
import eu.timepit.refined.char.*
import eu.timepit.refined.collection.*
import eu.timepit.refined.generic.*
import eu.timepit.refined.numeric.*
import eu.timepit.refined.string.*
import shapeless.{::, HNil}

final case class Person(name: String, age: Int)

type Age = Int Refined Interval.ClosedOpen[7, 77]

def isTrimmedStringEmpty(value: String): Boolean = Option(value).exists(_.trim.nonEmpty)

given Validate[Person, NonEmpty] with
  override type R = NonEmpty
  override def validate(person: Person): Res    =
    Result.fromBoolean(
      isTrimmedStringEmpty(person.name) &&
        RefType.applyRef[Age](person.age).isRight,
      Not(Empty())
    )
  override def showExpr(person: Person): String = person.toString

refineV[NonEmpty](Person("", 18))
refineV[NonEmpty](Person(" ", 18))
refineV[NonEmpty](Person("   ", 18))
refineV[NonEmpty](Person(null, 18))
refineV[NonEmpty](Person("Ivan", 0))
refineV[NonEmpty](Person("Ivan", 100))

refineV[NonEmpty](Person("Ivan", 18))
