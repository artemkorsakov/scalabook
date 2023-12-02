package ru.scalabook.books.tlpis

sealed trait HApply[-In, +Out]:
  def apply(in: In): Out

object HApply:
  import HList.::

  given HApply[HNil.type, HApply[HNil.type, HNil.type]] with
    def apply(h: HNil.type): HApply[HNil.type, HNil.type] =
      new HApply[HNil.type, HNil.type]:
        def apply(in: HNil.type): HNil.type = HNil

  given happlyCons[InH, OutH, TF <: HList, TIn <: HList, TOut <: HList](using
      applyTail: HApply[TF, HApply[TIn, TOut]]
  ): HApply[(InH => OutH) :: TF, HApply[InH :: TIn, OutH :: TOut]] with
    def apply(h: (InH => OutH) :: TF): HApply[InH :: TIn, OutH :: TOut] =
      new HApply[InH :: TIn, OutH :: TOut]:
        def apply(in: InH :: TIn): OutH :: TOut =
          HCons(h.head(in.head), applyTail(h.tail)(in.tail))

  def happly[H <: HList, In <: HList, Out <: HList](h: H)(in: In)(using
      toApply: HApply[H, HApply[In, Out]]
  ): Out =
    toApply(h)(in)
end HApply
