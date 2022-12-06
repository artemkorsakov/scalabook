package typeclass.monad

trait ApplicativePlus[F[_]] extends Applicative[F] with PlusEmpty[F]

object ApplicativePlus:
  def apply[F[_]: ApplicativePlus]: ApplicativePlus[F] = summon[ApplicativePlus[F]]

  given ApplicativePlus[List] with
    override def unit[A](a: => A): List[A] = List(a)

    override def apply[A, B](fab: List[A => B])(fa: List[A]): List[B] =
      fab.flatMap { aToB => fa.map(aToB) }

    override def plus[A](fa1: List[A], fa2: => List[A]): List[A] = fa1 ++ fa2

    override def empty[A]: List[A] = List.empty[A]
