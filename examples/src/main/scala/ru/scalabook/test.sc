import cats.data.ValidatedNel
import cats.syntax.all.*

trait Semigroup[A]:
  def combine(x: A, y: A): A

trait CSemigroup[A] extends Semigroup[A]

object CSemigroup:
  given CSemigroup[Int] = (x: Int, y: Int) => x + y

def checkAssociativity[A](x: A, y: A, z: A)(using
    s: Semigroup[A]
): ValidatedNel[String, Unit] =
  Either.cond[String, Unit](
    s.combine(s.combine(x, y), z) == s.combine(x, s.combine(y, z)),
    (),
    "Не соблюдается ассоциативность: (x + y) + z должно быть равно x + (y + z)"
  ).toValidatedNel

summon[CSemigroup[Int]].combine(1, 2)
summon[CSemigroup[Int]].combine(2, 1)

Tuple2
