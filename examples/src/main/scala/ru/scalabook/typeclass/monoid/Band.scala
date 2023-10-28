package ru.scalabook.typeclass.monoid

trait Band[A] extends Semigroup[A]:
  override def combineN(x: A, n: Int): Option[A] =
    if n <= 0 then None
    else Some(x)

object Band:
  def apply[A: Band]: Band[A] = summon[Band[A]]

  given setBandInstance[A]: Band[Set[A]] =
    (x: Set[A], y: Set[A]) => x ++ y
