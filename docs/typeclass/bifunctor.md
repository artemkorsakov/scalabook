# Группа Bifunctor

## Схема

```mermaid
classDiagram
        Bifunctor <|-- Bitraverse
        Bifoldable <|-- Bitraverse
        class Bifunctor~F[_, _]~{
            +bimap[A, B, C, D](fab: F[A, B])(f: A => C, g: B => D) F[C, D]
            +leftMap[A, B, C](fab: F[A, B])(f: A => C) F[C, B]
            +rightMap[A, B, D](fab: F[A, B])(g: B => D) F[A, D] 
        }
        class Bifoldable~F[_, _]~{
            +bifoldMap[M](fa: F[A, B])(f: A => M)(g: B => M)(using ma: Monoid[M]) M
            +bifoldRight[C](fa: F[A, B])(z: => C)(f: (A, C) => C)(g: (B, C) => C) C
            +bifoldLeft[C](fa: F[A, B])(z: C)(f: (C, A) => C)(g: (C, B) => C) C
        }
        class Bitraverse~F[_, _]~{
            +bitraverse[G[_]: Applicative, C, D](fab: F[A, B])(f: A => G[C], g: B => G[D]) G[F[C, D]]
            +bisequence[G[_]: Applicative, A, B](fab: F[A, B])(x: F[G[A], G[B]]) G[F[A, B]]
        }
```

**Ссылки:**

- [Исходный код](https://gitverse.ru/artemkorsakov/scalabook/content/master/examples/src/main/scala/ru/scalabook/typeclass/bifunctor)
- [Тесты](https://gitverse.ru/artemkorsakov/scalabook/content/master/examples/src/test/scala/ru/scalabook/typeclass/bifunctor)
