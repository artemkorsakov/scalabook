import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.auto.*
import eu.timepit.refined.string.*

import scala.language.implicitConversions
import scala.quoted.{Expr, Quotes}
import scala.util.matching.Regex

def inspectNameCode(x: Expr[String])(using Quotes): Expr[String] =
  import scala.quoted.quotes.reflect.report
  val pattern: Regex = "[А-ЯЁ][а-яё]+".r
  if !pattern.matches(x.valueOrAbort) then report.errorAndAbort("Invalid name")
  x

inline def inspectName(inline str: String): String =
  ${ inspectNameCode('str) }

inspectName("€‡™µ")
inspectName("12345")
inspectName("Alyona")
inspectName("Алёна18")
inspectName("алёна")
inspectName("Алёна")

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]

given Conversion[String, Name] = RefinedTypeOps[Name, String].unsafeFrom(_)

val name0: Name = inspectName("€‡™µ")
val name1: Name = inspectName("12345")
val name2: Name = inspectName("Alyona")
val name3: Name = inspectName("Алёна18")
val name4: Name = inspectName("алёна")
val name5: Name = inspectName("Алёна")
