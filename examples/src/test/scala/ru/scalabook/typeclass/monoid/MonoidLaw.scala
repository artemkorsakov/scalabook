package ru.scalabook.typeclass.monoid

import cats.data.*

trait MonoidLaw extends SemigroupLaw:
  def checkMonoidLaw[A: Monoid](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z) combine
      check(
        Monoid[A].combine(x, Monoid[A].empty) == x,
        "right identity: x + e = x"
      ) combine
      check(
        Monoid[A].combine(Monoid[A].empty, x) == x,
        "left identity: e + x = x"
      )
