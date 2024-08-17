package ru.scalabook.fp.state

opaque type State[S, +A] = S => (A, S)

object State:
  def apply[S, A](f: S => (A, S)): State[S, A] = f

  def unit[S, A](a: A): State[S, A] =
    s => (a, s)

  def get[S]: State[S, S] = s => (s, s)

  def set[S](s: S): State[S, Unit] = _ => ((), s)

  def modify[S](f: S => S): State[S, Unit] =
    for
      s <- get
      _ <- set(f(s))
    yield ()

  def sequence[S, A](list: List[State[S, A]]): State[S, List[A]] =
    traverse(list)(identity)

  def traverse[S, A, B](as: List[A])(f: A => State[S, B]): State[S, List[B]] =
    as.foldRight(unit[S, List[B]](Nil))((a, acc) => f(a).map2(acc)(_ :: _))

  extension [S, A](underlying: State[S, A])
    def run(s: S): (A, S) = underlying(s)

    def map[B](f: A => B): State[S, B] =
      s =>
        val (a, s1) = run(s)
        (f(a), s1)

    def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
      s0 =>
        val (a, s1) = run(s0)
        val (b, s2) = sb.run(s1)
        (f(a, b), s2)

    def flatMap[B](f: A => State[S, B]): State[S, B] =
      s0 =>
        val (a, s1) = run(s0)
        f(a)(s1)
end State
