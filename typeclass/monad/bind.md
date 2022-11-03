# Bind

`Bind` - это [`Apply`](apply) функтор, в котором функция может вводить новые значения и новый контекст функтора, 
включенный в контекст "подъемника". Эта операция называется `flatMap[B](f: A => F[B]): F[B]` (или её синоним - `bind`)

Для `Bind` должны соблюдаться следующие законы:
- соответствие `apply` и `flatMap`:
  - `apply(fab)(fa) == fab.flatMap(a2b => fa.map(a2b))`
- associativity на `flatMap` (как и в случае с полугруппами, монадические эффекты изменяются только при изменении их порядка,
  а не при изменении порядка их объединения):
  - `fa.flatMap(f).flatMap(g) == fa.flatMap { a => f(a).flatMap(g) }`

Функция `flatMap` позволяет нам определить операцию `join[A](ffa: F[F[A]]): F[A]`, "схлопывающую" слои функтора.
Синоним `join` - это `flatten`.


## Описание

```scala
trait Bind[F[_]] extends Apply[F]:
  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]

    def bind[B](f: A => F[B]): F[B] = flatMap(f)

  def join[A](ffa: F[F[A]]): F[A] = flatMap(ffa)(a => a)

  override def apply[A, B](fab: F[A => B])(fa: F[A]): F[B] =
    flatMap(fab)(x => fa.map(x))

  override def apply2[A, B, C](fa: => F[A], fb: => F[B])(f: (A, B) => C): F[C] =
    flatMap(fa)(a => fb.map(b => f(a, b)))
```

## Примеры

### "Обертка"

```scala
case class Id[A](value: A)

given Bind[Id] with
  extension [A](fa: Id[A])
    override def map[B](f: A => B): Id[B] =
      Id(f(fa.value))

    override def flatMap[B](f: A => Id[B]): Id[B] =
      f(fa.value)
```

### [Option](../../scala/fp/functional-error-handling)

```scala
given Bind[Option] with
  extension [A](fa: Option[A])
    override def map[B](f: A => B): Option[B] =
      fa match
        case Some(a) => Some(f(a))
        case None    => None

    override def flatMap[B](f: A => Option[B]): Option[B] =
      fa match
        case Some(a) => f(a)
        case None    => None
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FBind.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FBindSuite.scala)


## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*

3.some flatMap { x => (x + 1).some }  // Some(4)
3.some >>= { x => (x + 1).some }      // Some(4)
3.some >> 4.some                      // Some(4)
List(List(1, 2), List(3, 4)).join     // List(1, 2, 3, 4)
```


---

## References

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monad.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Bind.html)
