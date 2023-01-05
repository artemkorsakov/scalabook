import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.string.MatchesRegex

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]
object Name extends RefinedTypeOps[Name, String]

final case class Person(name: Name)

object Person:
  def fromString(str: String): Option[Person] =
    str match
      case Name(name) => Some(Person(name))
      case _          => None

Person.fromString("€‡™µ")
Person.fromString("12345")
Person.fromString("Alyona")
Person.fromString("Алёна18")
Person.fromString("алёна")
Person.fromString("Алёна")

Name.from("€‡™µ")
Name.from("12345")
Name.from("Alyona")
Name.from("Алёна18")
Name.from("алёна")
Name.from("Алёна")

// Name.unsafeFrom("€‡™µ")
// Name.unsafeFrom("12345")
// Name.unsafeFrom("Alyona")
// Name.unsafeFrom("Алёна18")
// Name.unsafeFrom("алёна")
Name.unsafeFrom("Алёна")
