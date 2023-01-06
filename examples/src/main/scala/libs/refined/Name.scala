package libs.refined

import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.string.MatchesRegex
import scala.quoted.{Expr, Quotes}

type Name = String Refined MatchesRegex[Name.pattern.type]

object Name:
  given Conversion[String, Name] = RefinedTypeOps[Name, String].unsafeFrom(_)

  val pattern: "[А-ЯЁ][а-яё]+" = "[А-ЯЁ][а-яё]+"

  extension (inline str: String)
    inline def toName: String = ${ inspectNameCode('str) }

  private def inspectNameCode(x: Expr[String])(using Quotes): Expr[String] =
    import scala.quoted.quotes.reflect.report
    if !pattern.r.matches(x.valueOrAbort) then
      report.errorAndAbort(s"'${x.valueOrAbort}' does not match pattern '$pattern'")
    x
