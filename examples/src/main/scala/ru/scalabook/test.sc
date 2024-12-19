import cats.data.ValidatedNel
import cats.syntax.all.*

trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Band[A] extends Semigroup[A]

object Band:
  given [A]: Band[Set[A]] =
    (x: Set[A], y: Set[A]) => x ++ y

def checkAssociativity[A](x: A, y: A, z: A)(using
    s: Semigroup[A]
): ValidatedNel[String, Unit] =
  Either.cond[String, Unit](
    s.combine(s.combine(x, y), z) == s.combine(x, s.combine(y, z)),
    (),
    "Не соблюдается ассоциативность: (x + y) + z должно быть равно x + (y + z)"
  ).toValidatedNel

def checkIdempotency[A](x: A)(using s: Band[A]): ValidatedNel[String, Unit] =
  Either.cond[String, Unit](
    s.combine(x, x) == x,
    (),
    "Не соблюдается идемпотентность: x + x должно быть равно x"
  ).toValidatedNel

summon[Band[Set[Int]]].combine(Set(1, 2), Set(1, 2))
