package ru.scalabook.site.syntax

import cats.syntax.apply.catsSyntaxTuple2Semigroupal
import laika.api.*
import laika.api.bundle.BlockDirectives.dsl.*
import laika.api.bundle.*
import laika.ast.*

object MyDirectives extends DirectiveRegistry:
  private val noteDirective: BlockDirectives.Directive =
    BlockDirectives.create("note") {
      (attribute(0).as[String], parsedBody).mapN { (note, body) =>
        val tooltip = Text(note)
        val content = BlockSequence(body, Styles("tooltip-text"))
        val title   = TitledBlock(Seq(tooltip), Seq(content), Styles("tooltip"))
        BlockSequence(title)
      }
    }

  /*
<span class="tooltip">Наведи на меня
  <span class="tooltip-text">Поясняющий текст</span>
</span>

<div class="tooltip-text">
  <p>Текст пояснения...</p>
  <p>Ещё текст</p>
</div>
   */
  val spanDirectives: Seq[SpanDirectives.Directive]         = Seq()
  val blockDirectives: Seq[BlockDirectives.Directive]       = Seq(noteDirective)
  val templateDirectives: Seq[TemplateDirectives.Directive] = Seq()
  val linkDirectives: Seq[LinkDirectives.Directive]         = Seq()
end MyDirectives
