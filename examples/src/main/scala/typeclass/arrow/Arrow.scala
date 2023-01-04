package typeclass.arrow

import typeclass.monad.Applicative

trait Arrow[=>:[_, _]] extends Split[=>:], Strong[=>:], Category[=>:]:
  self =>

  /** 'Поднятие' обычной функции до Arrow. */
  def arr[A, B](f: A => B): A =>: B

  /** Псевдоним для `compose`. */
  final def <<<[A, B, C](fbc: B =>: C, fab: A =>: B): A =>: C =
    compose(fbc, fab)

  /** Обратный `<<<`. */
  final def >>>[A, B, C](fab: A =>: B, fbc: B =>: C): A =>: C =
    compose(fbc, fab)

  /** Меняет пару местами */
  def swap[X, Y]: (X, Y) =>: (Y, X) = arr[(X, Y), (Y, X)] { case (x, y) =>
    (y, x)
  }

  /** Пропустить `C` нетронутым. */
  override def second[A, B, C](f: A =>: B): (C, A) =>: (C, B) =
    >>>(<<<(first[A, B, C](f), swap), swap)

  /** Запустить `fab` и `fcd` рядом друг с другом. Иногда обозначается как
    * `***`.
    */
  override def split[A, B, C, D](
      fab: A =>: B,
      fcd: C =>: D
  ): (A, C) =>: (B, D) =
    >>>(first[A, B, C](fab), second[C, D, B](fcd))

  /** Запустить `fab` и `fac` на одном и том же `A`. Иногда обозначается как
    * `&&&`.
    */
  def combine[A, B, C](fab: A =>: B, fac: A =>: C): A =>: (B, C) =
    >>>(arr((a: A) => (a, a)), split(fab, fac))

  /** Contramap on `A`. */
  override def mapfst[A, B, C](fab: A =>: B)(f: C => A): C =>: B =
    >>>[C, A, B](arr(f), fab)

  /** Functor map on `B`. */
  override def mapsnd[A, B, C](fab: A =>: B)(f: B => C): A =>: C =
    <<<[A, B, C](arr(f), fab)

  override def covariantInstance[C]: Applicative[[X] =>> =>:[C, X]] =
    new Applicative[[X] =>> =>:[C, X]]:
      override def unit[A](a: => A): C =>: A = arr(_ => a)

      override def apply[A, B](fab: C =>: (A => B))(fa: C =>: A): C =>: B =
        <<<(arr((y: (A => B, A)) => y._1(y._2)), combine(fab, fa))

  /** Запустить два `fab` рядом друг с другом */
  def product[A, B](fab: A =>: B): (A, A) =>: (B, B) =
    split(fab, fab)

object Arrow:
  def apply[=>:[_, _]: Strong]: Strong[=>:] = summon[Strong[=>:]]

  given Arrow[Function1] with
    override def arr[A, B](f: A => B): A => B                   = f
    override def id[A]: A => A                                  = a => a
    override def compose[A, B, C](f: B => C, g: A => B): A => C = g andThen f
    override def first[A, B, C](fa: A => B): ((A, C)) => (B, C) = (a, c) =>
      (fa(a), c)
