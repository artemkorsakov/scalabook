package ru.scalabook.books.tlpis

sealed class TipDummy[S, HL <: HList](val hl: HL):
  def tipToInd[I <: Indexed](using tip: Tip[S, HL, I]): I = tip(hl)

//object TipDummy:
//  given tipToInd[S, HL <: HList, I <: Indexed](dummy: TipDummy[S, HL])(implicit tip: Tip[S, HL, I]): I = tip(dummy.hl)
