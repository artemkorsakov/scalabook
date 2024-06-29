package ru.scalabook.site.syntax

import cats.data.NonEmptyList
import laika.api.bundle.SyntaxHighlighter
import laika.parse.code.CodeSpanParser
import laika.parse.code.languages.ScalaSyntax

object SbtSyntax extends SyntaxHighlighter:
  val language: NonEmptyList[String] = NonEmptyList.of("sbt")

  val spanParsers: Seq[CodeSpanParser] = ScalaSyntax.spanParsers
