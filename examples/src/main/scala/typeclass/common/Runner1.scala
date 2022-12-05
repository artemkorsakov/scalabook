package typeclass.common

trait Runner1[F[_]]:
  def run[A]: F[A] => A

object Runner1:
  def apply[F[_]: Runner1]: Runner1[F] = summon[Runner1[F]]

  given Runner1[Id] with
    override def run[A]: Id[A] => A = _.value

  def stateRunner[S](s: S): Runner1[[X] =>> State[S, X]] = new Runner1[[X] =>> State[S, X]]:
    def run[A]: State[S, A] => A = _.run(s)._2
