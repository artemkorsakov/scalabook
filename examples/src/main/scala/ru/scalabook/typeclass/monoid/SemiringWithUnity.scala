package ru.scalabook.typeclass.monoid

trait SemiringWithUnity[A] extends Semiring[A]:
  def one: A

object SemiringWithUnity:
  def apply[A: SemiringWithUnity]: SemiringWithUnity[A] =
    summon[SemiringWithUnity[A]]
