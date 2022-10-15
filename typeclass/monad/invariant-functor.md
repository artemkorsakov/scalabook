# Invariant Functor

Инвариантный функтор поддерживает операцию `xmap`, которая преобразует `F[A]` в `F[B]` с учетом двух функций: `A => B` и `B => A`. 

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
case class Id[A](value: A)

given idInvariantFunctor: InvariantFunctor[Id] with
  extension [A](fa: Id[A]) 
    override def xmap[B](f: A => B, g: B => A): Id[B] = Id(f(fa.value))
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FInvariantFunctor.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FInvariantFunctorSuite.scala)


---

## References

- [Scalaz API](https://javadoc.io/static/org.scalaz/scalaz-core_3/7.3.6/scalaz/InvariantFunctor.html)
