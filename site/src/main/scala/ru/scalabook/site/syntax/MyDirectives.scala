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

    private def otherLanguageHref(cursorPath: Path): String =
      val outputPath                    = cursorPath.withSuffix("html").toString
      val (currentServed, targetServed) = language match
        case Language.Russian => (outputPath, s"/en$outputPath")
        case Language.English => (s"/en$outputPath", outputPath)
      Path
        .parse(targetServed)
        .relativeTo(Path.parse(currentServed))
        .toString

    private val otherLanguagePage: TemplateDirectives.Directive =
      TemplateDirectives.create("otherLanguagePage") {
        import TemplateDirectives.dsl.*

        cursor.map { cursor =>
          TemplateElement(
            SpanLink(
              Seq(Text(language.switchLabel)),
              ExternalTarget(otherLanguageHref(cursor.path))
            )
          )
        }
      }

    private val languageSwitch: TemplateDirectives.Directive =
      TemplateDirectives.create("languageSwitchPage") {
        import TemplateDirectives.dsl.*

        cursor.map { cursor =>
          val link = SpanLink(
            Seq(Text(language.switchLabel)),
            ExternalTarget(otherLanguageHref(cursor.path)),
            options = Styles("button-link", "language-switch")
          )
          TemplateElement(link)
        }
      }

    val spanDirectives: Seq[SpanDirectives.Directive]   = Seq(noteSpanDirective)
    val blockDirectives: Seq[BlockDirectives.Directive] =
      Seq(noteBlockDirective)
    val templateDirectives: Seq[TemplateDirectives.Directive] =
      Seq(previousPage, nextPage, otherLanguagePage, languageSwitch)
    val linkDirectives: Seq[LinkDirectives.Directive] = Seq()
  end Registry
end MyDirectives
