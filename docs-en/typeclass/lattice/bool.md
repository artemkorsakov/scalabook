# Boolean algebra

## Formal definition

A **Boolean algebra** is a [Heyting algebra](heyting.md)
that additionally satisfies the **law of excluded middle** and the **law of double negation**.
Boolean algebras model classical logic and are a fundamental structure in mathematics,
computer science, and electrical engineering.

### Axioms of Boolean algebra

1. **Heyting algebra axioms**:
    - All axioms of `Heyting` (bounded distributive lattice with implication and pseudocomplement)

2. **Additional axioms**:
    - **Law of excluded middle**: `a ∨ ¬a = 1` (for any `a`)
    - **Law of double negation**: `¬¬a = a` (for any `a`)

3. **Equivalent formulations**:
    - `¬¬a = a` ⇔ `a ∨ ¬a = 1` (in the context of Heyting algebra)
    - Law of non-contradiction: `a ∧ ¬a = 0` (already follows from Heyting)
    - De Morgan laws: `¬(a ∨ b) = ¬a ∧ ¬b`, `¬(a ∧ b) = ¬a ∨ ¬b`

### Complete set of Boolean algebra laws

| Category                   | Law               | Formula                           |
|-----------------------------|-------------------|-----------------------------------|
| Idempotence                 | join              | `a ∨ a = a`                       |
| Idempotence                 | meet              | `a ∧ a = a`                       |
| Commutativity               | join              | `a ∨ b = b ∨ a`                   |
| Commutativity               | meet              | `a ∧ b = b ∧ a`                   |
| Associativity               | join              | `(a ∨ b) ∨ c = a ∨ (b ∨ c)`       |
| Associativity               | meet              | `(a ∧ b) ∧ c = a ∧ (b ∧ c)`       |
| Distributivity              | join/meet         | `a ∨ (b ∧ c) = (a ∨ b) ∧ (a ∨ c)` |
| Distributivity              | meet/join         | `a ∧ (b ∨ c) = (a ∧ b) ∨ (a ∧ c)` |
| Absorption                  | join/meet         | `a ∨ (a ∧ b) = a`                 |
| Absorption                  | meet/join         | `a ∧ (a ∨ b) = a`                 |
| Neutrality of zero          | join              | `0 ∨ a = a`                       |
| Neutrality of one           | meet              | `1 ∧ a = a`                       |
| Law of excluded middle      | -                 | `a ∨ ¬a = 1`                      |
| Law of non-contradiction    | -                 | `a ∧ ¬a = 0`                      |
| Double negation             | -                 | `¬¬a = a`                         |
| De Morgan laws              | -                 | `¬(a ∨ b) = ¬a ∧ ¬b`              |
| De Morgan laws              | -                 | `¬(a ∧ b) = ¬a ∨ ¬b`              |

### Examples of Boolean algebras

| Example                   | Operations               | zero        | one        | Application        |
|---------------------------|--------------------------|-------------|------------|--------------------|
| Boolean values {true, false} | `!`, `&&`, `\|\|`      | `false`     | `true`     | Classical logic    |
| Power set of a set (P(U)) | `∩`, `∪`, `^c`           | `∅`         | `U`        | Set theory         |
| Binary strings            | bitwise operations       | 0...0       | 1...1      | Digital logic      |
| Divisor lattice of N      | `GCD`, `LCM`, `N/a`      | `1`         | `N`        | Number theory      |
| Functions A → Bool        | pointwise operations     | const(false)| const(true)| Predicate logic    |
| Bitmasks                  | bitwise operations       | `0x00`      | `0xFF`     | Programming        |

## Code

```dotty
trait Bool[A] extends Heyting[A] with GenBool[A]:
  self =>

  /** Implication in Boolean algebra is defined via complement and disjunction: a
    * → b = ¬a ∨ b
    */
  def imp(a: A, b: A): A = or(complement(a), b)

  /** Relative complement in Boolean algebra is defined via intersection
    * and complement: a\b = a ∧ ¬b
    */
  def without(a: A, b: A): A = and(a, complement(b))

  // xor is already defined in both Heyting and GenBool.
  // In Bool these definitions coincide, so we simply use one of them.
  override def xor(a: A, b: A): A =
    or(without(a, b), without(b, a))
```

## Diagram

```mermaid
classDiagram
   class JoinSemilattice~A~ {
      +join(lhs: A, rhs: A) A
   }
   class BoundedJoinSemilattice~A~ {
      +zero: A
      +isZero(a: A)(using Equal[A]) Boolean
   }
   class MeetSemilattice~A~ {
      +meet(lhs: A, rhs: A) A
   }
   class BoundedMeetSemilattice~A~ {
      +one: A
      +isOne(a: A)(using Equal[A]) Boolean
   }
   class Lattice~A~ {
      +dual() Lattice~A~
   }
   class DistributiveLattice~A~
   class BoundedLattice~A~
   class BoundedDistributiveLattice~A~
   class Logic~A~ {
      +and(a: A, b: A) A
      +or(a: A, b: A) A
      +not(a: A) A
      +xor(a: A, b: A) A
      +nand(a: A, b: A) A
      +nor(a: A, b: A) A
      +nxor(a: A, b: A) A
   }
   class DeMorgan~A~ {
      +imp(a: A, b: A) A
   }
   class Heyting~A~ {
      +and(a: A, b: A) A
      +or(a: A, b: A) A
      +imp(a: A, b: A) A
      +complement(a: A) A
      +xor(a: A, b: A) A
      +nand(a: A, b: A) A
      +nor(a: A, b: A) A
      +nxor(a: A, b: A) A
   }
   class GenBool~A~ {
      +and(a: A, b: A) A
      +or(a: A, b: A) A
      +zero: A
      +without(a: A, b: A) A
      +xor(a: A, b: A) A
   }
   class Bool~A~ 

   JoinSemilattice <|-- Lattice
   JoinSemilattice <|-- BoundedJoinSemilattice
   MeetSemilattice <|-- Lattice
   MeetSemilattice <|-- BoundedMeetSemilattice
   BoundedJoinSemilattice <|-- BoundedLattice
   BoundedMeetSemilattice <|-- BoundedLattice
   Lattice <|-- BoundedLattice
   BoundedLattice <|-- BoundedDistributiveLattice
   Lattice <|-- DistributiveLattice
   DistributiveLattice <|-- BoundedDistributiveLattice
   BoundedDistributiveLattice <|-- Logic
   Logic <|-- DeMorgan
   BoundedDistributiveLattice <|-- Heyting
   BoundedJoinSemilattice <|-- GenBool
   DistributiveLattice <|-- GenBool
   DeMorgan <|-- Bool
   Heyting <|-- Bool
   GenBool <|-- Bool
 ```

## Exercises

### Exercise 5.3.1: Boolean algebra for Boolean

> [Repository with exercises and instructions][sample]

Implement `Bool` for the type `Boolean`:

```dotty
given Bool[Boolean] with
  override def one: Boolean = ???

  override def and(a: Boolean, b: Boolean): Boolean = ???

  override def or(a: Boolean, b: Boolean): Boolean = ???

  override def zero: Boolean = ???

  override def complement(a: Boolean): Boolean = ???
```

@:note(🔍 Show solution)

This implementation provides a classical Boolean algebra for the built-in `Boolean` type,
where `and` corresponds to logical AND (`&&`), `or` to logical OR (`||`),
`complement` to logical NOT (`!`), and `zero = false` and
`one = true` are the neutral elements for AND and OR respectively,
fully satisfying the laws of classical two-valued logic,
including the law of excluded middle and double negation.

```dotty
given Bool[Boolean] with
  override def one: Boolean = true

  override def and(a: Boolean, b: Boolean): Boolean = a && b

  override def or(a: Boolean, b: Boolean): Boolean = a || b

  override def zero: Boolean = false

  override def complement(a: Boolean): Boolean = !a
```

@:@

## Implementation in libraries

### Implementation in Cats

```dotty
import algebra.lattice.Bool
import cats.*
import cats.implicits.*

// Boolean algebra for bitmasks
case class BitMask(value: Int)

given BitMaskBool: Bool[BitMask] with
  def and(a: BitMask, b: BitMask): BitMask =
    BitMask(a.value & b.value)

  def or(a: BitMask, b: BitMask): BitMask =
    BitMask(a.value | b.value)

  def complement(a: BitMask): BitMask =
    BitMask(~a.value)
  
  def zero: BitMask = BitMask(0)
  def one: BitMask  = BitMask(-1) // all bits set

// Usage
val mask1 = BitMask(0b1010)
val mask2 = BitMask(0b1100)
val bool  = summon[Bool[BitMask]]

println(bool.and(mask1, mask2)) // BitMask(0b1000)
println(bool.or(mask1, mask2))  // BitMask(0b1110)
println(bool.complement(mask1)) // BitMask(0b...0101)
println(bool.xor(mask1, mask2)) // BitMask(0b0110)
```

## Summary

`Bool` is a full classical Boolean algebra, combining the properties of `Heyting` and `GenBool`. Key aspects:

1. **Law of excluded middle**: `a ∨ ¬a = 1` is guaranteed
2. **Double negation**: `¬¬a = a` is guaranteed
3. **Full classical logic**: all laws of classical logic hold
4. **Structure combination**: inherits and combines `Heyting` and `GenBool`
5. **Duality**: has a dual algebra with swapped operations

Boolean algebras are fundamental for:

- **Classical logic**: foundation of mathematical logic
- **Set theory**: operations on subsets
- **Digital circuit design**: logic circuit design
- **Programming**: bitwise operations, conditional expressions
- **Database theory**: relational algebra and queries
- **Cryptography**: Boolean functions and S-boxes

---

**Links:**

- [Link to the exercises repository][sample]
- [Cats](https://typelevel.org/cats/)

[sample]: https://gitverse.ru/scalabook/typeclasses
