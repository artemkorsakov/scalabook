package ru.scalabook.books.tlpis

sealed trait Tip[S, HL <: HList, Ind <: Indexed]:
  def apply(hl: HL): Ind

object Tip:
  import HList.::

  given tindexed0[S, H, T <: HList](using
      ev: S =:= H
  ): Tip[S, H :: T, Indexed0[H, T]] with
    def apply(hc: H :: T): Indexed0[H, T] = Indexed0[H, T](hc)

  given tindexedN[H, T <: HList, I <: Indexed, S](using
      iTail: Tip[S, T, I]
  ): Tip[S, H :: T, IndexedN[H, I]] with
    def apply(hc: H :: T): IndexedN[H, I] =
      IndexedN[H, I](hc.head, iTail(hc.tail))
