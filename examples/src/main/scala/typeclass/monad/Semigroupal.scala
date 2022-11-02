package typeclass.monad

import typeclass.common.Id

trait Semigroupal[F[_]]:
  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]

object Semigroupal:
  given Semigroupal[Id] with
    override def product[A, B](fa: Id[A], fb: Id[B]): Id[(A, B)] = Id((fa.value, fb.value))

  given Semigroupal[Option] with
    override def product[A, B](fa: Option[A], fb: Option[B]): Option[(A, B)] =
      (fa, fb) match
        case (Some(a), Some(b)) => Some((a, b))
        case _                  => None

  def product[F[_], A, B](fa: F[A], fb: F[B])(using s: Semigroupal[F]): F[(A, B)] =
    s.product(fa, fb)
