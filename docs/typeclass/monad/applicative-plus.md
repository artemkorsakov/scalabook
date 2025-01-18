# ApplicativePlus

## Формальное определение

`ApplicativePlus` - это моноидальный аппликативный функтор, 
[`Applicative`](applicative.md) комбинированный с [`PlusEmpty`](plus-empty.md).

`ApplicativePlus` должен удовлетворять законам своих родителей.


## Код

```dotty
trait ApplicativePlus[F[_]] extends Applicative[F], PlusEmpty[F]
```

### Связанный список

```dotty
given ApplicativePlus[List] with
  override def unit[A](a: => A): List[A] = List(a)

  override def apply[A, B](fab: List[A => B])(fa: List[A]): List[B] =
    fab.flatMap { aToB => fa.map(aToB) }

  override def plus[A](fa1: List[A], fa2: => List[A]): List[A] = fa1 ++ fa2

  override def empty[A]: List[A] = List.empty[A]
```

## Схема

```mermaid
classDiagram
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
```

## Реализация в библиотеках

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*

// ... Все операции родителей
```


---

**Ссылки:**

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/MonadPlus.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/ApplicativePlus.html)
