package ru.scalabook.typeclass.monoid

trait Ring[A] extends AbGroup[A], Semiring[A]

object Ring:
  def apply[A: Ring]: Ring[A] = summon[Ring[A]]
