package typeclass.common

trait Runner1[F[_]]:
  def run[A]: F[A] => A

object Runner1:
  def stateRunner[S](s: S): Runner1[[X] =>> State[S, X]] = new Runner1[[X] =>> State[S, X]]:
    def run[A]: State[S, A] => A = _.run(s)._2

  def run[F[_], A](using r: Runner1[F]): F[A] => A = r.run
