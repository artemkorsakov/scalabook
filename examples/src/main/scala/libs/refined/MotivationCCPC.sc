final case class Name private (value: String) extends AnyVal
object Name:
  import scala.util.matching.Regex
  private val pattern: Regex                = "[А-ЯЁ]{1}[а-яё]+".r
  def fromString(str: String): Option[Name] =
    if pattern.matches(str) then Some(Name(str))
    else None

final case class Person(name: Name)

Name("€‡™µ")

Name.fromString("€‡™µ").map(Person.apply)
Name.fromString("12345").map(Person.apply)
Name.fromString("Alyona").map(Person.apply)
Name.fromString("Алёна18").map(Person.apply)
Name.fromString("алёна").map(Person.apply)
Name.fromString("Алёна").map(Person.apply)

Name.fromString("Алёна").map(_.copy("€‡™µ")).map(Person.apply)
Name.fromString("Алёна").map(_.copy("12345")).map(Person.apply)
Name.fromString("Алёна").map(_.copy("Alyona")).map(Person.apply)
Name.fromString("Алёна").map(_.copy("Алёна18")).map(Person.apply)
Name.fromString("Алёна").map(_.copy( "алёна")).map(Person.apply)
Name.fromString("Алёна").map(_.copy("Алёна")).map(Person.apply)
