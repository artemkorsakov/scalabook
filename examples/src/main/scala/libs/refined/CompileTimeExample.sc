import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.auto.*
import eu.timepit.refined.string.*

import scala.language.implicitConversions

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]

given Conversion[String, Name] with
  def apply(s: String): Name =
    RefinedTypeOps[Name, String].unsafeFrom(s)

val name0: Name = "€‡™µ"
val name1: Name = "12345"
val name2: Name = "Alyona"
val name3: Name = "Алёна18"
val name4: Name = "алёна"
val name5: Name = "Алёна"
