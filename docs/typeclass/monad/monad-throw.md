{%
showPrev = true
prevPage = free-monads.html
showNext = true
nextPage = ../bifunctor.html
%}

# MonadThrow


## Формальное определение

???

## Код

```dotty
trait MonadThrow[F[_]] extends Monad[F]:
  extension [A](fa: F[A])
    def attempt: F[Try[A]]
    def handleErrorWith(h: Throwable => F[A]): F[A] =
      attempt.flatMap:
        case Failure(t) => h(t)
        case Success(a) => unit(a)

  def raiseError[A](t: Throwable): F[A]
```



## Схема

```mermaid
classDiagram
    class Plus~A~{
        +plus[A](fa1: F[A], fa2: => F[A]) F[A]
    }
    class PlusEmpty~A~{
        +empty[A]: F[A]
    }
    Plus <|-- PlusEmpty
    class IsEmpty~A~{
        +isEmpty[A](fa: F[A]) Boolean
    }
    PlusEmpty <|-- IsEmpty
```

## Реализация в библиотеках

### Реализация в Cats

```dotty
???
```

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]

[sample]: https://gitverse.ru/scalabook/typeclasses
