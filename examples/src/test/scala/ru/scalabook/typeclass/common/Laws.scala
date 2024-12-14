package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.monoid.Semigroup

object Laws:
  // Общие законы
  def checkAssociativity[A](x: A, y: A, z: A)(using
      s: Semigroup[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      s.combine(s.combine(x, y), z) == s.combine(x, s.combine(y, z)),
      (),
      "Не соблюдается ассоциативность: (x + y) + z должно быть равно x + (y + z)"
    ).toValidatedNel

  // Законы типовых классов
  def checkSemigroupLaw[A: Semigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkAssociativity[A](x, y, z)
