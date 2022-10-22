package typeclass.common

trait Runner2[=>:[_, _]]:
  self =>

  def run[X, R](x: X): X =>: R => R

object Runner2:
  given Runner2[Function1] with
    override def run[X, R](x: X): (X => R) => R = f => f(x)

  def run[=>:[_, _], A, R](a: A)(using r: Runner2[=>:]): A =>: R => R = r.run(a)
