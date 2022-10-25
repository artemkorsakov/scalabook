# CoBind

Комонада реализует противоположные для монады операции.


## Описание комонады

```scala
trait CoMonad[F[_]]:
  def coUnit[A](fa: F[A]): A
  def coFlatMap[A, B](fa: F[A])(f: F[A] => B): F[B]
```

## Примеры

### "Обертка"

```scala
case class Id[A](value: A)

given CoMonad[Id] with
  override def coUnit[A](fa: Id[A]): A = fa.value
  override def coFlatMap[A, B](fa: Id[A])(f: Id[A] => B): Id[B] = Id(f(fa))
```

### Переменные окружения

```scala
final case class Env[A, R](a: A, r: R)

given envCoMonad[R]: CoMonad[[X] =>> Env[X, R]] with
  override def coUnit[A](fa: Env[A, R]): A = fa.a
  override def coFlatMap[A, B](fa: Env[A, R])(f: Env[A, R] => B): Env[B, R] = Env(f(fa), fa.r)
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FCoMonad.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FCoMonadSuite.scala)


---

## References

- [Scalaz API](https://javadoc.io/static/org.scalaz/scalaz-core_3/7.3.6/scalaz/Contravariant.html)
