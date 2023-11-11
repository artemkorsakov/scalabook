package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import cats.syntax.either.*

trait Laws:
  protected def check(law: Boolean): ValidatedNel[String, Unit] =
    check(law, "")
    
  protected def check(
      law: Boolean,
      message: => String
  ): ValidatedNel[String, Unit] =
    Either
      .cond[String, Unit](law, (), message)
      .toValidatedNel
