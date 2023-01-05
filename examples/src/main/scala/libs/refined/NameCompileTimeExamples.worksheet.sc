import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.auto.*
import eu.timepit.refined.string.*

import scala.util.matching.Regex

import libs.refined.Name.{toName, pattern}

"€‡™µ".toName
"12345".toName
"Alyona".toName
"Алёна18".toName
"алёна".toName
"Алёна".toName

type Name = String Refined MatchesRegex[pattern.type]

given Conversion[String, Name] = RefinedTypeOps[Name, String].unsafeFrom(_)

val name0: Name = "€‡™µ".toName
val name1: Name = "12345".toName
val name2: Name = "Alyona".toName
val name3: Name = "Алёна18".toName
val name4: Name = "алёна".toName
val name5: Name = "Алёна".toName
