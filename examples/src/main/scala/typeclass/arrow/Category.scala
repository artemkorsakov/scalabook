package typeclass.arrow

import typeclass.monad.PlusEmpty
import typeclass.monoid.Monoid

trait Category[=>:[_, _]] extends Compose[=>:]:
  def id[A]: A =>: A

  def empty: PlusEmpty[[A] =>> A =>: A] = new PlusEmpty[[A] =>> A =>: A] with ComposePlus:
    def empty[A]: A =>: A = id

  def monoid[A]: Monoid[A =>: A] = new Monoid[A =>: A] with ComposeSemigroup[A]:
    def empty: A =>: A = id

object Category:
  given Category[Function1] with
    override def compose[A, B, C](f: B => C, g: A => B): A => C = g andThen f

    override def id[A]: A => A = a => a
