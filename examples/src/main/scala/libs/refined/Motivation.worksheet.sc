import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.string.MatchesRegex

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]

object Name extends RefinedTypeOps[Name, String]

// to Either
Name.from("€‡™µ")
Name.from("12345")
Name.from("Alyona")
Name.from("Алёна18")
Name.from("алёна")
Name.from("Алёна")

// Небезопасная конвертация
try Name.unsafeFrom("€‡™µ")
catch case e: IllegalArgumentException => print(e.getMessage())
try Name.unsafeFrom("12345")
catch case e: IllegalArgumentException => print(e.getMessage())
try Name.unsafeFrom("Alyona")
catch case e: IllegalArgumentException => print(e.getMessage())
try Name.unsafeFrom("Алёна18")
catch case e: IllegalArgumentException => print(e.getMessage())
try Name.unsafeFrom("алёна")
catch case e: IllegalArgumentException => print(e.getMessage())
Name.unsafeFrom("Алёна")

// Неявное преобразование типов
given Conversion[String, Option[Name]] = Name.unapply(_)

val name0: Option[Name] = "€‡™µ"
val name1: Option[Name] = "12345"
val name2: Option[Name] = "Alyona"
val name3: Option[Name] = "Алёна18"
val name4: Option[Name] = "алёна"
val name5: Option[Name] = "Алёна"

val name = Name.unsafeFrom("Алёна")
def printT[T >: String](t: T): Unit = println(t)
printT(name)
