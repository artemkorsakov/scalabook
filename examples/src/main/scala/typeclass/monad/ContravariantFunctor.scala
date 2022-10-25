package typeclass.monad

import typeclass.common.Id

trait ContravariantFunctor[F[_]] extends InvariantFunctor[F]:
  self =>

  def cmap[A, B](b: F[B])(f: A => B): F[A]

  def contramap[A, B](b: F[B])(f: A => B): F[A] = cmap(b)(f)

  extension [A](fa: F[A]) override def xmap[B](f: A => B, g: B => A): F[B] = cmap(fa)(g)

  /** Композиция двух контравариантных функторов ковариантна */
  def compose[G[_]: ContravariantFunctor]: Functor[[X] =>> F[G[X]]] =
    new Functor[[X] =>> F[G[X]]]:
      private val g = summon[ContravariantFunctor[G]]
      extension [A](as: F[G[A]]) def map[B](f: A => B): F[G[B]] = cmap(as)(gb => g.cmap(gb)(f))

  /** Композиция контравариантного и ковариантного функторов контравариантна */
  def icompose[G[_]: Functor]: ContravariantFunctor[[X] =>> F[G[X]]] =
    new ContravariantFunctor[[X] =>> F[G[X]]]:
      private val g = summon[Functor[G]]
      def cmap[A, B](fa: F[G[B]])(f: A => B): F[G[A]] = self.cmap(fa)(g.lift(f))

  /** Произведение двух контравариантных функторов контравариантно */
  def product[G[_]: ContravariantFunctor]: ContravariantFunctor[[X] =>> (F[X], G[X])] =
    new ContravariantFunctor[[X] =>> (F[X], G[X])]:
      private val g = summon[ContravariantFunctor[G]]
      def cmap[A, B](fa: (F[B], G[B]))(f: A => B): (F[A], G[A]) =
        (self.contramap(fa._1)(f), g.contramap(fa._2)(f))

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
