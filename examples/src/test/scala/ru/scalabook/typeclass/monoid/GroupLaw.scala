package ru.scalabook.typeclass.monoid

import cats.data.*

trait GroupLaw extends MonoidLaw:
  def checkGroupLaw[A: Group](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkMonoidLaw(x, y, z) combine
      check(
        Group[A].combine(x, Group[A].inverse(x)) == Group[A].empty,
        "inverse law 1: x + (-x) = 0"
      ) combine
      check(
        Group[A].combine(Group[A].inverse(x), x) == Group[A].empty,
        "inverse law 2: (-x) + x = 0"
      )
