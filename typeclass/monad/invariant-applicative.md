# Invariant Applicative

Инвариантный Applicative поддерживает операции `xunit0`, `xunit1` и т.д., 
которые "оборачивают" тип в инвариантный функтор. 
`InvariantApplicative` расширяет [`InvariantFunctor`](invariant-functor).

Используется для получения класса типов продуктов и типов значений.


## Описание инвариантного Applicative

```scala
trait InvariantFunctor[F[_]]:
  extension [A](fa: F[A]) def xmap[B](f: A => B, g: B => A): F[B]

trait InvariantApplicative[F[_]] extends InvariantFunctor[F]:
  def xunit0[A](a: => A): F[A]

  extension [A](fa: => F[A])
    def xunit1[B](f: A => B, g: B => A): F[B] =
      fa.xmap(f, g)
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FInvariantApplicative.scala&plain=1)

---

## References

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/InvariantApplicative.html)
