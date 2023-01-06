# Invariant Functor

Инвариантный функтор поддерживает операцию `xmap` (или `imap`), 
которая преобразует `F[A]` в `F[B]` с учетом двух функций: `A => B` и `B => A`. 

Если [функтор](functor) создает новые экземпляры класса типов, добавляя функцию в конец цепочки преобразований, 
а [контравариантный функтор](contravariant-functor) создает их, добавляя функцию в начало цепочки преобразований, 
то инвариантный функтор создает их с помощью пары двунаправленных преобразований.

Инвариантный функтор должен удовлетворять двум законам: 
- Identity (тождественность): Если определен метод идентификации `identity` такой, что: `identity(a) == a`,
  тогда `xmap(ma)(identity, identity) == ma`.
- Composition (композиция) - `xmap(xmap(ma, f1, g1), f2, g2) == xmap(ma, f2 compose f1, g1, compose g2)`

Также известен как экспоненциальный функтор.


## Описание инвариантного функтора

```scala
trait InvariantFunctor[F[_]]:
  extension [A](fa: F[A]) 
    def xmap[B](f: A => B, g: B => A): F[B]
```

## Примеры

### "Обертка"

```scala
final case class Id[A](value: A)

given InvariantFunctor[Id] with
  extension [A](fa: Id[A]) 
    override def xmap[B](f: A => B, g: B => A): Id[B] = Id(f(fa.value))
```

### Codec

Зачастую инвариантный функтор используется в кодеках, кодирующих и декодирующих строки.

```scala
trait Codec[A]:
  def encode(value: A): String
  def decode(value: String): A

given InvariantFunctor[Codec] with
  extension [A](fa: Codec[A])
    override def xmap[B](f: A => B, g: B => A): Codec[B] =
      new Codec[B]:
        def encode(value: B): String = fa.encode(g(value))

        def decode(value: String): B = f(fa.decode(value))
```


## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FInvariantFunctor.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FInvariantFunctorSuite.scala)


---

## References

- [Cats](https://typelevel.org/cats/typeclasses/invariant.html)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#sec:functors:invariant)
- [Scalaz API](https://javadoc.io/static/org.scalaz/scalaz-core_3/7.3.6/scalaz/InvariantFunctor.html)
