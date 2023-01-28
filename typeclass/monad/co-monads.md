# Co-Monad

Комонада реализует противоположные для [монады](monad) операции и расширяет [`CoBind`](co-bind).


## Описание

```scala
trait CoMonad[F[_]] extends CoBind[F]:
  /** Также известно как `coPoint` и `coPure` */
  def coUnit[A](fa: F[A]): A

  final def coPoint[A](p: F[A]): A = coUnit(p)

  final def coPure[A](p: F[A]): A = coUnit(p)
```

## Примеры

### "Обертка"

```scala
case class Id[A](value: A)

given CoMonad[Id] with
  override def coUnit[A](fa: Id[A]): A = fa.value

  extension [A](as: Id[A])
    override def map[B](f: A => B): Id[B] = Id(f(as.value))

    override def cobind[B](f: Id[A] => B): Id[B] = Id(f(as))
```

### Переменные окружения

```scala
final case class Env[A, R](a: A, r: R)

given envCoMonad[R]: CoMonad[[X] =>> Env[X, R]] with
  override def coUnit[A](fa: Env[A, R]): A = fa.a

  extension [A](as: Env[A, R])
    override def map[B](f: A => B): Env[B, R] =
      val Env(a, r) = as
      Env(f(a), r)

    override def cobind[B](f: Env[A, R] => B): Env[B, R] =
      val Env(_, r) = as
      Env(f(as), r)
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FCoMonad.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FCoMonadSuite.scala)


---

**Ссылки:**

- [Scalaz API](https://javadoc.io/static/org.scalaz/scalaz-core_3/7.3.6/scalaz/Comonad.html)
