package ru.scalabook.books.tlpis

sealed trait HList

case object HNil extends HList:
  self =>

  def ::[A](v: A): HCons[A, HNil.type] = HCons(v, self)

final case class HCons[H, T <: HList](head: H, tail: T) extends HList:
  self =>

  def ::[A](v: A): HCons[A, HCons[H, T]] = HCons(v, self)

// алиасы для построения типов HList и для сопоставления с шаблоном
object HList:
  type ::[H, T <: HList] = HCons[H, T]
  val :: = HCons

  type Inc = FoldAny[Nat] {
    type Apply[N <: Any, Acc <: Nat] = Succ[Acc]
  }

  type Length[H <: HList]            = Inc#Foldr[H, _0]
  type LengthViaFoldLeft[H <: HList] = Inc#Foldl[H, _0]

  object AppHCons extends FoldAny[HList]:
    type Apply[N <: Any, H <: HList] = N :: H

    // будет использоваться позднее для реализации на уровне значений
    def apply[A, B <: HList](a: A, b: B) = HCons(a, b)

  type :::[A <: HList, B <: HList] =
    AppHCons.Foldr[A, B]

  type Reverse_:::[A <: HList, B <: HList] =
    AppHCons.Foldl[A, B]

  type Reverse[A <: HList] =
    AppHCons.Foldl[A, HNil.type]

  object Length extends FoldAny[Int]:
    type Apply[N <: Any, Acc <: Int] = Int
    def apply[A, B <: Int](a: A, b: B): Apply[Any, Int] = b + 1

  type toI[H <: HList, N <: Nat] <: Indexed =
    H match
      case HNil.type   => Nothing
      case HCons[h, t] =>
        // совпадение по N:
        //   Если он равен _0, тип Indexed должен указывать на эту ячейку (то есть это Indexed0)
        //   в противном случае индекс будет справа, поэтому вернитесь к N-1 и верните IndexedN
        N match
          case _0      => Indexed0[h, t]
          case Succ[n] => IndexedN[h, toI[t, n]]

  extension [A <: HList](a: A)
    def length: Int = Length.foldr(a, 0)

    def :::[B <: HList](b: B): HList = AppHCons.foldr(a, b)

    def reverse_:::[B <: HList](b: B): HList = AppHCons.foldl(a, b)

    def reverse: HList = AppHCons.foldl(a, HNil)

    def i[N <: Nat](using in: A => toI[A, N]): toI[A, N] = in(a)
