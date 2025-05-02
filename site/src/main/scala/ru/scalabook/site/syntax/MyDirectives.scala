package ru.scalabook.site.syntax

import cats.syntax.apply.catsSyntaxTuple2Semigroupal
import laika.api.*
import laika.api.bundle.*
import laika.ast.*

object MyDirectives extends DirectiveRegistry:
  private val noteSpanDirective: SpanDirectives.Directive =
    import SpanDirectives.dsl.*
    SpanDirectives.create("note") {
      (attribute(0).as[String], parsedBody).mapN { (note, body) =>
        val tooltip = Text(note)
        val content = SpanSequence(body, Styles("tooltip-text"))
        SpanSequence(Seq(tooltip, content), Styles("tooltip"))
      }
    }

  private val noteBlockDirective: BlockDirectives.Directive =
    import BlockDirectives.dsl.*
    BlockDirectives.create("note") {
      (attribute(0).as[String], parsedBody).mapN { (note, body) =>
        val tooltip = Paragraph(note)
        val content = BlockSequence(body, Styles("tooltip-text"))
        BlockSequence(Seq(tooltip, content), Styles("tooltip"))
      }
    }

  val spanDirectives: Seq[SpanDirectives.Directive]   = Seq(noteSpanDirective)
  val blockDirectives: Seq[BlockDirectives.Directive] = Seq(noteBlockDirective)
  val templateDirectives: Seq[TemplateDirectives.Directive] = Seq()
  val linkDirectives: Seq[LinkDirectives.Directive]         = Seq()
end MyDirectives
