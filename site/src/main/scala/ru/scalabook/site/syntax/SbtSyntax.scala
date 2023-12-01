package ru.scalabook.site.syntax

import cats.data.NonEmptyList
import laika.bundle.SyntaxHighlighter
import laika.parse.code.CodeSpanParser
import laika.parse.code.languages.DottySyntax

object SbtSyntax extends SyntaxHighlighter:
  val language: NonEmptyList[String] = NonEmptyList.of("sbt")

  val softKeywords: CodeSpanParser = DottySyntax.softKeywords

  val spanParsers: Seq[CodeSpanParser] = DottySyntax.spanParsers
