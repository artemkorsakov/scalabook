package typeclass.monad

import munit.Assertions
import typeclass.common.Runner1
import typeclass.common.Runner1.run
import typeclass.monad.Semigroupal.product

trait SemigroupalLaw extends Assertions:
  def checkSemigroupalLawWithRunner[F[_]: Semigroupal: Runner1, A, B, C](fa: F[A], fb: F[B], fc: F[C]): Unit =
    val product1: (A, (B, C)) = run(product[F, A, (B, C)](fa, product[F, B, C](fb, fc)))
    val product2: ((A, B), C) = run(product[F, (A, B), C](product[F, A, B](fa, fb), fc))
    val value1: (A, B, C)     = (product1._1, product1._2._1, product1._2._2)
    val value2: (A, B, C)     = (product2._1._1, product2._1._2, product2._2)
    assertEquals(value1, value2, "associativity")
