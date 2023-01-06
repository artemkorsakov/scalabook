import cats.data.ValidatedNec
import cats.implicits.*
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.cats.syntax.*
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.refineV
import eu.timepit.refined.string.{MatchesRegex, Uuid}

import java.util.UUID

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]
object Name extends RefinedTypeOps[Name, String]

type Age = Int Refined Interval.ClosedOpen[7, 77]
object Age extends RefinedTypeOps[Age, Int]

type Id = String Refined Uuid

final case class Person(name: Name, age: Age, id: Id)

object Person:
  def refine(name: String, age: Int, id: String): ValidatedNec[String, Person] =
    (
      Name.validateNec(name),
      Age.validateNec(age),
      refineV[Uuid](id).toValidatedNec
    ).mapN(Person.apply)

Person.refine("Андрей", 50, UUID.randomUUID().toString)
Person.refine("Andrew", 150, "id")
