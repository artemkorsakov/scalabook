package typeclass.monad

trait ContravariantFunctor[F[_]]:
  def cmap[A, B](b: F[B])(f: A => B): F[A]

object ContravariantFunctor:
  opaque type Predicate = [X] =>> X => Boolean
  opaque type Show = [X] =>> X => String

  given predicateContravariantFunctor: ContravariantFunctor[Predicate] with
    def cmap[A, B](predicate: Predicate[B])(f: A => B): Predicate[A] =
      a => predicate(f(a))

  given showContravariantFunctor: ContravariantFunctor[Show] with
    def cmap[A, B](show: Show[B])(f: A => B): Show[A] =
      a => show(f(a))

  given functionContravariantFunctor[R]: ContravariantFunctor[[X] =>> Function1[X, R]] with
    def cmap[A, B](function: B => R)(f: A => B): A => R =
      a => function(f(a))

  def cmap[F[_], A, B](b: F[B])(f: A => B)(using cf: ContravariantFunctor[F]): F[A] =
    cf.cmap(b)(f)
