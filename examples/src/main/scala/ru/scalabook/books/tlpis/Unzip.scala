package ru.scalabook.books.tlpis

trait Unzip[H <: HList, R1 <: HList, R2 <: HList]:
  def unzip(h: H): (R1, R2)

object Unzip:
  import HList.::

  given Unzip[HNil.type, HNil.type, HNil.type] with
    def unzip(h: HNil.type): (HNil.type, HNil.type) = (HNil, HNil)

  given unzipCons[H1, H2, T <: HList, TR1 <: HList, TR2 <: HList](using
      unzipTail: Unzip[T, TR1, TR2]
  ): Unzip[(H1, H2) :: T, H1 :: TR1, H2 :: TR2] with
    def unzip(h: (H1, H2) :: T): (H1 :: TR1, H2 :: TR2) =
      val (t1, t2) = unzipTail.unzip(h.tail)
      (HCons(h.head._1, t1), HCons(h.head._2, t2))
