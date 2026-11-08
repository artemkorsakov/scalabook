package ru.scalabook.site.syntax

import cats.syntax.apply.catsSyntaxTuple2Semigroupal
import laika.api.*
import laika.api.bundle.{TemplateDirectives, *}
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

  private val leftArrow  = "← "
  private val rightArrow = " →"

  private val previousPage: TemplateDirectives.Directive =
    TemplateDirectives.create("previousPage") {
      import TemplateDirectives.dsl.*

      cursor.map { cursor =>
        val previous = cursor.flattenedSiblings.previousDocument

        val title = previous.flatMap(c => c.target.title)
        val path  = previous.map(c => c.path)

        val link =
          (title, path).mapN { (t, p) =>
            SpanLink(Seq(Text(leftArrow), t), InternalTarget(p))
          }

        TemplateElement(link.getOrElse(Text("")))
      }
    }

  private val nextPage =
    TemplateDirectives.create("nextPage") {
      import TemplateDirectives.dsl.*

      cursor.map { cursor =>
        val next = cursor.flattenedSiblings.nextDocument

        val title = next.flatMap(c => c.target.title)
        val path  = next.map(c => c.path)

        val link =
          (title, path).mapN { (t, p) =>
            SpanLink(Seq(t, Text(rightArrow)), InternalTarget(p))
          }

        TemplateElement(link.getOrElse(Text("")))
      }
    }

  val spanDirectives: Seq[SpanDirectives.Directive]   = Seq(noteSpanDirective)
  val blockDirectives: Seq[BlockDirectives.Directive] = Seq(noteBlockDirective)
  val templateDirectives: Seq[TemplateDirectives.Directive] =
    Seq(previousPage, nextPage)
  val linkDirectives: Seq[LinkDirectives.Directive] = Seq()
end MyDirectives
