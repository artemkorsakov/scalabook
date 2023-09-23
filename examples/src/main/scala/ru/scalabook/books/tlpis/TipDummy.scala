package ru.scalabook.books.tlpis

sealed class TipDummy[S, HL <: HList](val hl: HL):
  def toIndexed[I <: Indexed](using tip: Tip[S, HL, I]): I = tip(hl)

object TipDummy:
  given fromTipDummy[S, HL <: HList, I <: Indexed](using
      tip: Tip[S, HL, I]
  ): Conversion[TipDummy[S, HL], I] with
    def apply(dummy: TipDummy[S, HL]): I =
      tip(dummy.hl)
