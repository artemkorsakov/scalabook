package typeclass.arrow

import typeclass.monad.Plus
import typeclass.monoid.Semigroup

trait Compose[=>:[_, _]]:
  self =>

  /** Ассоциативный `=>:` бинарный оператор. */
  def compose[A, B, C](f: B =>: C, g: A =>: B): A =>: C

  protected[this] trait ComposePlus extends Plus[[A] =>> A =>: A]:
    def plus[A](f1: A =>: A, f2: => A =>: A): A =>: A = self.compose(f1, f2)

  protected[this] trait ComposeSemigroup[A] extends Semigroup[A =>: A]:
    def combine(f1: A =>: A, f2: A =>: A): A =>: A = self.compose(f1, f2)

  def plus: Plus[[A] =>> A =>: A] = new ComposePlus {}

  def semigroup[A]: Semigroup[A =>: A] = new ComposeSemigroup[A] {}

object Compose:
  def apply[=>:[_, _]: Compose]: Compose[=>:] = summon[Compose[=>:]]

  given Compose[Function1] with
    override def compose[A, B, C](f: B => C, g: A => B): A => C = g andThen f
