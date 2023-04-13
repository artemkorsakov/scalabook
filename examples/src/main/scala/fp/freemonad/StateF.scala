package fp.freemonad

sealed trait StateF[S, +A]
case class Get[S, A](f: S => A)  extends StateF[S, A]
case class Put[S, A](s: S, a: A) extends StateF[S, A]

object StateF:
  given statefFun[S]: Functor[[X] =>> StateF[S, X]] = new:
    def map[A, B](m: StateF[S, A])(f: A => B): StateF[S, B] =
      m match
        case Get(g)    => Get(s => f(g(s)))
        case Put(s, a) => Put(s, f(a))

  type FreeState[S, A] = Free[[X] =>> StateF[S, X], A]

  def pureState[S, A](a: A): FreeState[S, A] = Done(a)

  def getState[S]: FreeState[S, S] = More(Get(s => Done(s)))

  def setState[S](s: S): FreeState[S, Unit] = More(Put(s, Done(())))

  @annotation.tailrec
  def evalS[S, A](s: S, t: FreeState[S, A]): A =
    t.resume match
      case Left(Get(f))    => evalS(s, f(s))
      case Left(Put(n, a)) => evalS(n, a)
      case Right(a)        => a

  def zipIndex[A](as: List[A]): List[(Int, A)] =
    evalS(
      0,
      as.foldLeft(pureState[Int, List[(Int, A)]](List())) { (acc, a) =>
        for
          xs <- acc
          n  <- getState
          _  <- setState(n + 1)
        yield (n, a) :: xs
      }
    ).reverse
