package libs.refined

import scala.quoted.{Expr, Quotes}

object Name:
  val pattern: "[А-ЯЁ][а-яё]+" = "[А-ЯЁ][а-яё]+"

  extension (inline str: String)
    inline def toName: String = ${ inspectNameCode('str) }

  private def inspectNameCode(x: Expr[String])(using Quotes): Expr[String] =
    import scala.quoted.quotes.reflect.report
    if !pattern.r.matches(x.valueOrAbort) then
      report.errorAndAbort(s"Name does not match pattern '$pattern'")
    x
