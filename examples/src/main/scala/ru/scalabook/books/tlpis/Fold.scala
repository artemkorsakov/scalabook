package ru.scalabook.books.tlpis

trait Fold[-Elem, Value]:
  type Apply[N <: Elem, Acc <: Value] <: Value
  def apply[N <: Elem, Acc <: Value](n: N, acc: Acc): Apply[N, Acc]

trait FoldAny[Value] extends Fold[Any, Value]:
  type Foldr[H <: HList, I <: Value] <: Value =
    H match
      case HNil.type   => I
      case HCons[h, t] => Apply[h, Foldr[t, I]]

  def foldr[H <: HList, I <: Value](hlist: H, i: I): Value =
    hlist match
      case HNil              => i
      case HCons(head, tail) => apply(head, foldr(tail, i))

  type Foldl[H <: HList, I <: Value] <: Value =
    H match
      case HNil.type   => I
      case HCons[h, t] => Foldl[t, Apply[h, I]]

  def foldl[H <: HList, I <: Value](hlist: H, i: I): Value =
    hlist match
      case HNil              => i
      case HCons(head, tail) => foldl(tail, apply(head, i))

object Fold:
  type Inc = FoldAny[Nat] {
    type Apply[N <: Any, Acc <: Nat] = Succ[Acc]
  }
