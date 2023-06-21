# Invariant Functor

Инвариантный функтор поддерживает операцию `xmap` (или `imap`), 
которая преобразует `F[A]` в `F[B]` с учетом двух функций: `A => B` и `B => A`. 

Если [функтор](functor.md) 
создает новые экземпляры класса типов, добавляя функцию в конец цепочки преобразований, 
а [контравариантный функтор](contravariant-functor.md) 
создает их, добавляя функцию в начало цепочки преобразований, 
то инвариантный функтор создает их с помощью пары двунаправленных преобразований.

Инвариантный функтор должен удовлетворять двум законам:

- Identity (тождественность): Если определен метод идентификации `identity` такой, что: `identity(a) == a`,
  тогда `xmap(ma)(identity, identity) == ma`.
- Composition (композиция) - `xmap(xmap(ma, f1, g1), f2, g2) == xmap(ma, f2 compose f1, g1 compose g2)`

Также известен как экспоненциальный функтор.


## Описание инвариантного функтора

```dotty
trait InvariantFunctor[F[_]]:
  extension [A](fa: F[A]) 
    def xmap[B](f: A => B, g: B => A): F[B]
```

## Примеры

### "Обертка"

```dotty
final case class Id[A](value: A)

given InvariantFunctor[Id] with
  extension [A](fa: Id[A]) 
    override def xmap[B](f: A => B, g: B => A): Id[B] = Id(f(fa.value))
```

Докажем, что `Id` удовлетворяет законам инвариантного функтора.

- тождественность: `xmap(ma)(identity, identity) == ma`
    - по определению метода `xmap` получим: `Id(a).xmap(identity, identity) == Id(identity(a)) == Id(a)`
- композиция: `xmap(xmap(ma, f1, g1), f2, g2) == xmap(ma, f2 compose f1, g1 compose g2)`
    - по определению метода `xmap` получим:
      `Id(a).xmap(f1, g1).xmap(f2, g2) == Id(f1(a)).xmap(f2, g2) == Id(f2(f1(a)))` 
      и `Id(a).xmap(f2 compose f1, g1 compose g2) == Id(f2(f1(a)))`

### Codec

Зачастую инвариантный функтор используется в кодеках, кодирующих и декодирующих строки.

```dotty
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

Докажем, что `Codec` удовлетворяет законам инвариантного функтора.

- тождественность: `xmap(ma)(identity, identity) == ma`
    - по определению метода `xmap` получим: `fa.xmap(identity, identity) ==`
      ```dotty
      def encode(value: A): String = fa.encode(identity(value)) = fa.encode(value)
      def decode(value: String): A = identity(fa.decode(value)) = = fa.decode(value)
      ```
      Это эквивалентно исходному `fa` (в случае использования чистых функций) - проксируются его методы.
- композиция: `xmap(xmap(ma, f1, g1), f2, g2) == xmap(ma, f2 compose f1, g1 compose g2)`
    - по определению метода `xmap` получим `xmap(xmap(ma, f1, g1), f2, g2) ==`:
        ```dotty
        def encode(value: C): String = fb.encode(g2(value)) = fa.encode(g1(g2(value)))
        def decode(value: String): C = f2(fb.decode(value)) = f2(f1(fa.decode(value)))
        ```
        `xmap(ma, f2 compose f1, g1 compose g2) ==`:
        ```dotty
        def encode(value: C): String = fa.encode(g1 compose g2) = fa.encode(g1(g2(value)))
        def decode(value: String): C = (f2 compose f1)(fa.decode(value)) = f2(f1(fa.decode(value)))
        ```
  
---

**Ссылки:**

- [Cats](https://typelevel.org/cats/typeclasses/invariant.html)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#sec:functors:invariant)
- [Scalaz API](https://javadoc.io/static/org.scalaz/scalaz-core_3/7.3.6/scalaz/InvariantFunctor.html)
