import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.auto.*
import eu.timepit.refined.string.*

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]

given Conversion[String, Option[Name]] = RefinedTypeOps[Name, String].unapply(_)

val name0: Option[Name] = "€‡™µ"
val name1: Option[Name] = "12345"
val name2: Option[Name] = "Alyona"
val name3: Option[Name] = "Алёна18"
val name4: Option[Name] = "алёна"
val name5: Option[Name] = "Алёна"
