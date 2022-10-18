package typeclass.arrow

import typeclass.monad.Plus
import typeclass.monoid.Semigroup

trait Compose[=>:[_, _]]:
  /** Ассоциативный `=>:` бинарный оператор. */
  def compose[A, B, C](f: B =>: C, g: A =>: B): A =>: C

  def plus: Plus[[A] =>> A =>: A] = new Plus[[A] =>> A =>: A]:
    def plus[A](f1: A =>: A, f2: => A =>: A): A =>: A = compose(f1, f2)

  def semigroup[A]: Semigroup[A =>: A] = (f1: A =>: A, f2: A =>: A) => compose(f1, f2)

object Compose:
  given Compose[Function1] with
    override def compose[A, B, C](f: B => C, g: A => B): A => C = g andThen f
