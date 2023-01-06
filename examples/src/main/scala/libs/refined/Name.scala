package libs.refined

import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.string.MatchesRegex
import scala.quoted.{Expr, Quotes, ToExpr, quotes}

type Name = String Refined MatchesRegex[Name.pattern.type]

object Name:
  val pattern: "[А-ЯЁ][а-яё]+" = "[А-ЯЁ][а-яё]+"

  extension (inline str: String)
    inline def toName: Name = ${ inspectNameCode('str) }

  private given NameToExpr: ToExpr[Name] with
    def apply(x: Name)(using Quotes) =
      import quotes.reflect.*
      Literal(StringConstant(x.toString)).asExpr.asInstanceOf[Expr[Name]]

  private def inspectNameCode(x: Expr[String])(using Quotes): Expr[Name] =
    val str        = x.valueOrAbort
    if !pattern.r.matches(str) then
      import quotes.reflect.*
      report.errorAndAbort(s"'$str' does not match pattern '$pattern'")
    val name: Name = RefinedTypeOps[Name, String].unsafeFrom(str)
    Expr(name)
