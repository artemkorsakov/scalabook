package typeclass.common

trait Runner2[=>:[_, _]]:
  self =>

  def run[X, R](x: X): X =>: R => R

object Runner2:
  given Runner2[Function1] with
    override def run[X, R](x: X): (X => R) => R = f => f(x)
