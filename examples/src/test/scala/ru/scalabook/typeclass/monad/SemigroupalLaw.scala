package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel
import ru.scalabook.typeclass.common.{Laws, Runner1}

trait SemigroupalLaw extends Laws:
  def checkSemigroupalLaw[F[_]: Semigroupal: Runner1, A, B, C](
      fa: F[A],
      fb: F[B],
      fc: F[C]
  ): ValidatedNel[String, Unit] =
    val product1: (A, (B, C)) =
      Runner1[F].run(
        Semigroupal[F]
          .product[A, (B, C)](fa, Semigroupal[F].product[B, C](fb, fc))
      )
    val product2: ((A, B), C) =
      Runner1[F].run(
        Semigroupal[F]
          .product[(A, B), C](Semigroupal[F].product[A, B](fa, fb), fc)
      )
    val value1: (A, B, C) = (product1._1, product1._2._1, product1._2._2)
    val value2: (A, B, C) = (product2._1._1, product2._1._2, product2._2)
    check(value1 == value2, "associativity")
end SemigroupalLaw
