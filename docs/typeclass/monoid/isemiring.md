{%
showPrev = true
prevPage = csemiring.html
showNext = true
nextPage = rng.html
%}

#  Идемпотентное полукольцо

Идемпотентное полукольцо - это [полукольцо](semiring.md), которое также является [идемпотентным](band.md),
т.е. добавление значения к самому себе приводит к тому же значению.

Для полукольца с единицей должны соблюдаться все законы полукольца:

- _Замыкание сложения_ (_closure_): для \\(\forall x, y \in S\\) выполняется \\(x + y \in S\\).
- _Ассоциативность сложения_ (_associativity_): для \\(\forall x, y, z \in S\\) выполняется \\((x + y) + z = x + (y + z)\\).
- _Существование нулевого элемента_: существует \\(\exists 0 \in S\\) такой,
  что для \\(\forall x \in S\\) выполняется \\(0 + x = x + 0 = x\\)
- _Коммутативность сложения_ (_commutative_): для \\(\forall x, y \in S\\) выполняется \\(x + y = y + x\\).
- _Замыкание умножения_ (_closure_): для \\(\forall x, y \in S\\) выполняется \\(x \* y \in S\\).
- _Ассоциативность умножения_ (_associativity_): для \\(\forall x, y, z \in S\\) выполняется \\((x \* y) \* z = x \* (y \* z)\\).
- _Дистрибутивность_ (_distributivus_, _распределительный закон_):
  для \\(\forall x, y, z \in S\\) выполняется \\(x \* (y + z) = x \* y + x \* z\\) и \\((x + y) \* z = x \* z + y \* z\\).
- _Мультипликативное свойство нуля_: \\(a \* 0 = 0 \* a = 0\\)

, а также закон идемпотентности сложения:

- _Идемпотентность_ (_idempotency_): для \\(\forall x \in S\\) выполняется \\(x + x = x\\).

## Код

```dotty
trait ISemiring[A] extends Semiring[A], IdempotentMonoid[A]
```

### Законы

- Законы [полукольца](semiring.md)
- Идемпотентность формулируется так:  
  `s.combine(x, x)` должно быть равно `x`.  
  Это означает, что объединение элемента с самим собой не изменяет его.

## Схема

```mermaid
classDiagram
    class Semigroup~A~{
        +combine(x: A, y: A) A
    }
    class IdempotentSemigroup~A~
    Semigroup <|-- IdempotentSemigroup    
    class Monoid~A~{
        +empty() A
    }
    Semigroup <|-- Monoid
    class CommutativeSemigroup~A~
    Semigroup <|-- CommutativeSemigroup  
    class IdempotentMonoid~A~
    IdempotentSemigroup <|-- IdempotentMonoid
    Monoid <|-- IdempotentMonoid
    class CommutativeMonoid~A~
    Monoid <|-- CommutativeMonoid
    CommutativeSemigroup <|-- CommutativeMonoid
    class Semiring~A~
    CommutativeMonoid <|-- Semiring
    class MultiplicativeSemigroup~A~{
        +times(x: A, y: A) A
    }
    MultiplicativeSemigroup <|-- Semiring
    Semigroup .. MultiplicativeSemigroup
    class IdempotentSemiring~A~
    IdempotentMonoid <|-- IdempotentSemiring
    Semiring <|-- IdempotentSemiring
```

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D1%83%D0%BA%D0%BE%D0%BB%D1%8C%D1%86%D0%BE)

[sample]: https://gitverse.ru/scalabook/typeclasses
