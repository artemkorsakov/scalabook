# Группа Monad

## Схема группы

```mermaid
---
title: Группа монад
---
classDiagram
    direction LR
    class InvariantFunctor~A~{
        +xmap[B](fa: F[A], f: A => B, g: B => A) F[B]
    }
    class Semigroupal~A~{
        +product[A, B](fa: F[A], fb: F[B]) F[Tuple2[A, B]]
    }
    class Functor~A~{
        +map[B](fa: F[A], f: A => B) F[B]
    }
    InvariantFunctor <|-- Functor    
    class InvariantApplicative~A~{
        +xunit0[A](a: => A) F[A]
    }
    InvariantFunctor <|-- InvariantApplicative   
    class Apply~A~{
        +apply[A, B](fab: F[A => B])(fa: F[A]) F[B]
    }
    Functor <|-- Apply
    Semigroupal <|-- Apply
    class Applicative~A~{
        +unit[A](a: => A) F[A]
    }
    Apply <|-- Applicative
    InvariantApplicative <|-- Applicative
    class Bind~A~{
        +flatMap[B](fa: F[A], f: A => F[B]) F[B]
    }
    Apply <|-- Bind
    class Monad~A~
    Applicative <|-- Monad
    Bind <|-- Monad
    class Plus~A~{
        +plus[A](fa1: F[A], fa2: => F[A]) F[A]
    }
    class PlusEmpty~A~{
        +empty[A]: F[A]
    }
    Plus <|-- PlusEmpty
    class ApplicativePlus~A~
    Applicative <|-- ApplicativePlus
    PlusEmpty <|-- ApplicativePlus
    class MonadPlus~A~
    Monad <|-- MonadPlus
    ApplicativePlus <|-- MonadPlus
    class IsEmpty~A~{
        +isEmpty[A](fa: F[A]) Boolean
    }
    PlusEmpty <|-- IsEmpty
    class Foldable~A~{
        +foldRight[B](fa: F[A])(acc: B)(f: (A, B) => B) B 
        +foldLeft[B](fa: F[A])(acc: B)(f: (B, A) => B) B
    }
    class Traverse~A~{
        +traverse[G[_]: Applicative, B](fa: F[A])(f: A => G[B]) G[F[B]]
        +sequence[G[_]: Applicative, A](fga: F[G[A]]) G[F[A]]
    }
    Functor <|-- Traverse
    Foldable <|-- Traverse
    class ContravariantFunctor~A~{
        +contramap[A, B](b: F[B])(f: A => B) F[A]
    }
    InvariantFunctor <|-- ContravariantFunctor
    class Divide~A~{
        +divide[A, B, C](fa: => F[A], fb: => F[B])(f: C => (A, B)) F[C]
    }
    ContravariantFunctor <|-- Divide
    class Divisible~A~{
        +conquer[A]: F[A]
    }
    Divide <|-- Divisible
    InvariantApplicative <|-- Divisible
    class CoBind~A~{
        +cobind[B](fa: F[A], f: F[A] => B) F[B]
    }
    Functor <|-- CoBind
    class CoMonad~A~{
        +coUnit[A](fa: F[A]) A
    }
    CoBind <|-- CoMonad
    class Bimonad~A~
    Monad <|-- Bimonad
    CoMonad <|-- Bimonad
```

**Ссылки:**

- [Исходный код](https://gitverse.ru/artemkorsakov/scalabook/content/master/examples/src/main/scala/ru/scalabook/typeclass/monad)
- [Тесты](https://gitverse.ru/artemkorsakov/scalabook/content/master/examples/src/test/scala/ru/scalabook/typeclass/monad)
- [A Monads Approach for Beginners, in Scala - Rock the JVM](https://www.youtube.com/watch?v=d-dy1x33moA&list=PLmtsMNDRU0Bzj7INIrLugi3a_WClwQuiS)
