package ru.scalabook.books.tlpis

sealed trait HZip[A <: HList, B <: HList, Result <: HList]:
  def apply(a: A, b: B): Result

object HZip:
  import HList.::

  given HZip[HNil.type, HNil.type, HNil.type] with
    def apply(a: HNil.type, b: HNil.type): HNil.type = HNil

  given hzipCons[HA, HB, TA <: HList, TB <: HList, TR <: HList](using
      hzipTail: HZip[TA, TB, TR]
  ): HZip[HA :: TA, HB :: TB, (HA, HB) :: TR] with
    def apply(a: HA :: TA, b: HB :: TB): (HA, HB) :: TR =
      HCons((a.head, b.head), hzipTail(a.tail, b.tail))

  given hzipNil1[H, T <: HList]: HZip[H :: T, HNil.type, HNil.type] with
    def apply(a: H :: T, b: HNil.type): HNil.type = HNil

  given hzipNil2[H, T <: HList]: HZip[HNil.type, H :: T, HNil.type] with
    def apply(a: HNil.type, b: H :: T): HNil.type = HNil
