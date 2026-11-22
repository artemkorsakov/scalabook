# Semifield

A semifield is a [rig](rig.md) in which every non-zero element has a multiplicative inverse.

The following laws must hold for a semifield:

- _Closure under addition_ (_closure_): for \\(\forall x, y \in S\\), \\(x + y \in S\\).
- _Associativity of addition_ (_associativity_): for \\(\forall x, y, z \in S\\), \\((x + y) + z = x + (y + z)\\).
- _Existence of a zero element_: there exists \\(\exists 0 \in S\\) such that
  for \\(\forall x \in S\\), \\(0 + x = x + 0 = x\\).
- _Commutativity of addition_ (_commutative_): for \\(\forall x, y \in S\\), \\(x + y = y + x\\).
- _Closure under multiplication_ (_closure_): for \\(\forall x, y \in S\\), \\(x \* y \in S\\).
- _Associativity of multiplication_ (_associativity_): for \\(\forall x, y, z \in S\\), \\((x \* y) \* z = x \* (y \* z)\\).
- _Distributivity_ (_distributivity_):
  for \\(\forall x, y, z \in S\\), \\(x \* (y + z) = x \* y + x \* z\\) and \\((x + y) \* z = x \* z + y \* z\\).
- _Multiplicative zero property_: \\(a \* 0 = 0 \* a = 0\\).
- _Existence of a multiplicative identity_: \\(\exists 1 \in S \quad \forall x \in S \quad x \* 1 = 1 \* x = x\\).
- _Existence of an inverse element_: for any \\(a \in F\\), \\(a \neq 0\\), there exists an element \\(a^{-1} \in F\\) such that \\(a \cdot a^{-1} = 1\\).

## Code

```dotty
trait Semifield[A] extends Rig[A]:
  extension (a: A)
    def reciprocal: A

  def div(x: A, y: A): A = times(x, y.reciprocal)
```

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
    class Rig~A~{
        +one() A
    }
    Semiring <|-- Rig
    class Semifield~A~{
      +reciprocal(x: A) A
      +div(x: A, y: A) A
    }
    Rig <|-- Semifield
```

---

**References:**

- [Link to the exercises repository][sample]
- [Spire home page](https://typelevel.org/spire/)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D1%83%D0%BA%D0%BE%D0%BB%D1%8C%D1%86%D0%BE)

[sample]: https://gitverse.ru/scalabook/typeclasses
