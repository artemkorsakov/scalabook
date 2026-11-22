package ru.scalabook.site.syntax

import cats.syntax.apply.catsSyntaxTuple2Semigroupal
import laika.api.*
import laika.api.bundle.{TemplateDirectives, *}
import laika.ast.*
import ru.scalabook.site.syntax.HeliumThemes.Language

object MyDirectives:

  def apply(language: Language): DirectiveRegistry = new Registry(language)

  private class Registry(language: Language) extends DirectiveRegistry:

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

    private val otherLanguagePage: TemplateDirectives.Directive =
      TemplateDirectives.create("otherLanguagePage") {
        import TemplateDirectives.dsl.*

        cursor.map { cursor =>
          val outputPath = cursor.path.withSuffix("html").toString
          val (currentServed, targetServed) = language match
            case Language.Russian => (outputPath, s"/en$outputPath")
            case Language.English => (s"/en$outputPath", outputPath)
          val href = Path
            .parse(targetServed)
            .relativeTo(Path.parse(currentServed))
            .toString
          TemplateElement(
            SpanLink(Seq(Text(language.switchLabel)), ExternalTarget(href))
          )
        }
      }

    val spanDirectives: Seq[SpanDirectives.Directive]   = Seq(noteSpanDirective)
    val blockDirectives: Seq[BlockDirectives.Directive] =
      Seq(noteBlockDirective)
    val templateDirectives: Seq[TemplateDirectives.Directive] =
      Seq(previousPage, nextPage, otherLanguagePage)
    val linkDirectives: Seq[LinkDirectives.Directive] = Seq()
  end Registry
end MyDirectives
