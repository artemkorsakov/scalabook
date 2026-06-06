package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import cats.syntax.either.*

trait BasicLaws:
  def check(condition: Boolean, message: String): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](condition, (), message).toValidatedNel

end BasicLaws
