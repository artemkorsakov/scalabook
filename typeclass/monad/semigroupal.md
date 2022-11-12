# Semigroupal

`Semigroupal` - это класс типов, который позволяет комбинировать контексты. 
Если есть два объекта типа `F[A]` и `F[B]`, `Semigroupal[F]` позволяет объединить их для формирования объекта `F[(A, B)]`:
`product: (F[A], F[B]) => F[(A, B)]` (также встречается название операции `tuple2`).

`Semigroupal` должен удовлетворять следующим законам:
- ассоциативность: `product(a, product(b, c)) == product(product(a, b), c)`

## Описание

```scala
trait Semigroupal[F[_]]:
  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
```

## Примеры

### "Обертка"

```scala
final case class Id[A](value: A)

given Semigroupal[Id] with
  override def product[A, B](fa: Id[A], fb: Id[B]): Id[(A, B)] = Id((fa.value, fb.value))
```

### [Option](../../scala/fp/functional-error-handling)

```scala
given Semigroupal[Option] with
  override def product[A, B](fa: Option[A], fb: Option[B]): Option[(A, B)] =
    (fa, fb) match
      case (Some(a), Some(b)) => Some((a, b))
      case _                  => None
```


## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FSemigroupal.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FSemigroupalSuite.scala)

## Реализация в Cats

```scala
import cats.Semigroupal
import cats.instances.option.*

Semigroupal[Option].product(Some(123), Some("abc")) // Some((123,abc))
Semigroupal.tuple3(Option(1), Option(2), Option(3)) // Some((1,2,3))
```


---

## References

- [Cats API](https://typelevel.org/cats/api/cats/Semigroupal.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Semigroupal.html)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#semigroupal)
