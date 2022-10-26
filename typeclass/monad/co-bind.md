# CoBind

`CoBind` реализует противоположные для [`Bind`](bind) операции.


## Описание

```scala
trait CoBind[F[_]] extends Functor[F]:
  extension [A](fa: F[A])
    /** Также известно как `extend` */
    def cobind[B](f: F[A] => B): F[B]

    final def extend[B](f: F[A] => B): F[B] = fa.cobind(f)

    final def coFlatMap[B](f: F[A] => B): F[B] = fa.cobind(f)

    /** Также известно как `duplicate` */
    final def cojoin: F[F[A]] = fa.cobind(fa => fa)
```

## Примеры

### "Обертка"

```scala
case class Id[A](value: A)

given CoBind[Id] with
  extension [A](as: Id[A])
    override def map[B](f: A => B): Id[B] = Id(f(as.value))

    override def cobind[B](f: Id[A] => B): Id[B] = Id(f(as))
```

### Переменные окружения

```scala
final case class Env[A, R](a: A, r: R)

given envCoBind[R]: CoBind[[X] =>> Env[X, R]] with
  extension [A](as: Env[A, R])
    override def map[B](f: A => B): Env[B, R] =
      val Env(a, r) = as
      Env(f(a), r)

    override def cobind[B](f: Env[A, R] => B): Env[B, R] =
      val Env(_, r) = as
      Env(f(as), r)
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FCoBind.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FCoBindSuite.scala)


---

## References

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Cobind.html)
