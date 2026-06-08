package ru.scalabook.typeclass.monad

trait ContravariantFunctor[F[_]] extends InvariantFunctor[F]:
  self =>

  def cmap[A, B](b: F[B])(f: A => B): F[A]

  def contramap[A, B](b: F[B])(f: A => B): F[A] = cmap(b)(f)

  extension [A](fa: F[A])
    override def xmap[B](f: A => B, g: B => A): F[B] = cmap(fa)(g)

  /** Композиция ковариантного и контравариантного функторов ковариантна */
  def compose[G[_]: ContravariantFunctor]: Functor[[X] =>> F[G[X]]] =
    new Functor[[X] =>> F[G[X]]]:
      extension [A](as: F[G[A]])
        def map[B](f: A => B): F[G[B]] =
          cmap(as)(gb => ContravariantFunctor[G].cmap(gb)(f))

  /** Композиция контравариантного и ковариантного функторов контравариантна */
  def icompose[G[_]: Functor]: ContravariantFunctor[[X] =>> F[G[X]]] =
    new ContravariantFunctor[[X] =>> F[G[X]]]:
      def cmap[A, B](fa: F[G[B]])(f: A => B): F[G[A]] =
        self.cmap(fa)(Functor[G].lift(f))

  /** Произведение двух контравариантных функторов контравариантно */
  def product[G[_]: ContravariantFunctor]
      : ContravariantFunctor[[X] =>> (F[X], G[X])] =
    new ContravariantFunctor[[X] =>> (F[X], G[X])]:
      def cmap[A, B](fa: (F[B], G[B]))(f: A => B): (F[A], G[A]) =
        (self.contramap(fa._1)(f), ContravariantFunctor[G].contramap(fa._2)(f))
end ContravariantFunctor

object ContravariantFunctor:
  inline def apply[F[_]: ContravariantFunctor]: ContravariantFunctor[F] =
    summon[ContravariantFunctor[F]]
