# Commutative Semiring

A commutative semiring is a [semiring](semiring.md) with a commutative multiplication operation:
for \\(\forall x, y \in S\\), \\(x * y = y * x\\) holds.

All laws of a semiring must hold for a commutative semiring:

- _Additive closure_ (_closure_): for \\(\forall x, y \in S\\), \\(x + y \in S\\).
- _Additive associativity_ (_associativity_): for \\(\forall x, y, z \in S\\), \\((x + y) + z = x + (y + z)\\).
- _Existence of zero element_: there exists \\(\exists 0 \in S\\) such that
  for \\(\forall x \in S\\), \\(0 + x = x + 0 = x\\)
- _Additive commutativity_ (_commutative_): for \\(\forall x, y \in S\\), \\(x + y = y + x\\).
- _Multiplicative closure_ (_closure_): for \\(\forall x, y \in S\\), \\(x \* y \in S\\).
- _Multiplicative associativity_ (_associativity_): for \\(\forall x, y, z \in S\\), \\((x \* y) \* z = x \* (y \* z)\\).
- _Distributivity_ (_distributivus_, _distributive law_):
  for \\(\forall x, y, z \in S\\), \\(x \* (y + z) = x \* y + x \* z\\) and \\((x + y) \* z = x \* z + y \* z\\).
- _Multiplicative zero property_: \\(a \* 0 = 0 \* a = 0\\)

as well as the law of multiplicative commutativity:

- _Multiplicative commutativity_ (_commutative_): for \\(\forall x, y \in S\\), \\(x \* y = y \* x\\).

## Code

```dotty
trait CSemiring[A] extends Semiring[A]
```

### Laws

- Laws of a [semiring](semiring.md)
- Commutativity of multiplication means that the order of elements when multiplying does not matter.
  In other words, the result of the `times` operation will be the same regardless of the order in which elements `x` and `y` are passed.
  This can be written as: `cs.times(x, y) == cs.times(y, x)`

## Diagram

```mermaid
classDiagram
    class Semigroup~A~{
        +combine(x: A, y: A) A
    }  
    class Monoid~A~{
        +empty() A
    }
    Semigroup <|-- Monoid
    class CommutativeSemigroup~A~
    Semigroup <|-- CommutativeSemigroup  
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
    class CommutativeSemiring~A~
    Semiring <|-- CommutativeSemiring
```

## Implementation in Libraries

---

**References:**

- [Repository with exercises and instructions for completion][sample]
- [Wikipedia](https://en.wikipedia.org/wiki/Semiring)

[sample]: https://gitverse.ru/scalabook/typeclasses
