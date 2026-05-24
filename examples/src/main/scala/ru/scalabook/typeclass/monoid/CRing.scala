package ru.scalabook.typeclass.monoid

trait CRing[A] extends Ring[A], CSemiring[A]

object CRing:
  def apply[A: CRing]: CRing[A] = summon[CRing[A]]
