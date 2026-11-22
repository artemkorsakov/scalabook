# Algebra Overview

**Algebra** is a library (or module within Cats)
that uses **type classes** to represent **algebraic structures**.
In simple terms, it allows you to describe formal mathematical properties
that data types in your program should have.

You can use these type classes to express **abstract capabilities** (and requirements)
that generic parameters should have.
Instead of being tied to a concrete type like `Int` or `String`,
you write code that works with any type `A` that can "add up" (has `Semigroup`) or "empty itself" (has `Monoid`).

**Real-world analogy:**

Imagine writing a function that adds up a list of elements.
It doesn't matter what those elements are — numbers, strings, or lists.
The key thing is that an "addition" (merging) operation is defined for them.
Algebra allows you to express this requirement in code.

## Section 1: Algebraic Properties and Terminology

This section explains basic mathematical properties that operations may have.
Understanding these terms is critical for working with the library.

Here is the rewritten table:

- **Associativity**:
    - Description: The result does not depend on the order of computation (parentheses placement).
    - Formula: `a ⊕ (b ⊕ c) = (a ⊕ b) ⊕ c`
    - Example with integers: `(2 + 3) + 4 = 2 + (3 + 4) = 9`
- **Commutativity**:
    - Description: The result does not depend on the order of arguments.
    - Formula: `a ⊕ b = b ⊕ a`
    - Example with integers: `2 + 3 = 3 + 2 = 5` (but subtraction is not!)
- **Neutral element (Identity)**:
    - Description: There exists an element that, when operated with any other, yields that other element.
    - Formula: `a ⊕ id = id ⊕ a = a`
    - Example with integers: For addition it is `0` (`5 + 0 = 5`). For multiplication it is `1`.
- **Inverse element**:
    - Description: For each element there exists a paired element that, summed, gives the neutral element.
    - Formula: `a ⊕ ¬a = ¬a ⊕ a = id`
    - Example with integers: For `5`, the inverse for addition is `-5` (`5 + (-5) = 0`).
- **Distributivity**:
    - Description: The "distributive property" for two operations.
    - Formula: `a ⊙ (b ⊕ c) = (a ⊙ b) ⊕ (a ⊙ c)`
    - Example with integers: Multiplication is distributive over addition: `2 * (3 + 4) = (2*3) + (2*4)`.
- **Idempotence**:
    - Description: Applying the operation twice equals applying it once.
    - Formula: `a ⊕ a = a`
    - Example with integers: For `max`: `max(5,5) = 5`. For a function: `abs(abs(x)) = abs(x)`.

### Important Note on Notation

Although these properties are described with symbols (`⊕`, `⊙`), they also work with regular functions:

- When you see `a ⊕ b`, it is equivalent to calling a binary function `f(a, b)`. The symbol `⊕` is just a convenient infix notation.
- When you see `¬a`, it is equivalent to calling a unary function `g(a)` (e.g., `-a`).

## Section 2: Basic Algebraic Structures

These structures are in the `algebra` package.
All of them implement the **`combine`** method, which is **always associative**.

- If there is a **neutral element**, it is called **`empty`**.
- If there is an **inverse element**, it is called **`inverse`**.

### Basic Structures Table

Here is the converted table: the last column was moved into a separate description before the table, and the table itself has only 5 columns.

---

## Structure Descriptions (column "What it means / Example")

- [**Semigroup**](../../typeclass/monoid/semigroup.md): The simplest structure: can only merge.
  *Example: string concatenation ("a" + "b") or list appending.*
- [**CommutativeSemigroup**](../../typeclass/monoid/csemigroup.md): Merge order does not matter.
  *Example: integer addition (2+3 = 3+2), but not string concatenation.*
- [**Monoid**](../../typeclass/monoid/monoid.md): Can merge and has an "empty" value.
  *Example: lists (empty list), strings (empty string), number addition (0).*
- [**Band**](../../typeclass/monoid/band.md): Each element is idempotent with respect to `combine`.
  *Example: `min` or `max` operation (min(5,5) = 5).*
- [**Semilattice**](../../typeclass/monoid/semilattice.md): Commutative version of Band.
  *Example: set intersection (idempotent and commutative).*
- [**Group**](../../typeclass/monoid/group.md): Full-fledged group: has addition, zero, and subtraction.
  *Example: integers with + (has 0 and -a).*
- [**CommutativeMonoid**](../../typeclass/monoid/cmonoid.md): Commutative monoid.
  *Example: multiplication of positive numbers (order does not matter, has 1).*
- [**CommutativeGroup**](../../typeclass/monoid/abgroup.md): Abelian group.
  *Example: integer addition (has 0, has -a, and 2+3 = 3+2).*
- [**BoundedSemilattice**](../../typeclass/monoid/bounded-semilattice.md): Semilattice with a bound (neutral element).
  *Example: for `min` operation, the neutral element is +∞ (maximum value).*

| Name                     | Associativity | Commutativity | Neutral | Inverse | Idempotence |
|:-------------------------|:--------------|:--------------|:--------|:--------|:------------|
| **Semigroup**            | ✓             |               |         |         |             |
| **CommutativeSemigroup** | ✓             | ✓             |         |         |             |
| **Monoid**               | ✓             |               | ✓       |         |             |
| **Band**                 | ✓             |               |         |         | ✓           |
| **Semilattice**          | ✓             | ✓             |         |         | ✓           |
| **Group**                | ✓             |               | ✓       | ✓       |             |
| **CommutativeMonoid**    | ✓             | ✓             | ✓       |         |             |
| **CommutativeGroup**     | ✓             | ✓             | ✓       | ✓       |             |
| **BoundedSemilattice**   | ✓             | ✓             | ✓       |         | ✓           |

## Section 3: Ring-like Structures

The `algebra.ring` package contains more complex structures combining **two operations**:

1. **Additive** (addition): called **`plus`**.
2. **Multiplicative** (multiplication): called **`times`**.

**General rules for all ring-like structures:**

- Both operations (`+` and `*`) are **associative**.
- Addition (`+`) is **commutative**.
- There is always a **`zero`** (the neutral element for addition, analogous to `0`).

### Structure Descriptions (column "Brief description")

- [**Semiring**](../../typeclass/monoid/semiring.md): Semiring.
  Can add and multiply, but has no subtraction (`negate`) and no mandatory unit (`1`).
  *Example: natural numbers (without zero?).*
- [**Rng**](../../typeclass/monoid/rng.md) (Ring without 'i' - identity): Ring without unit (missing letter 'i' in the word Ring).
  *Example: even integers (multiplication exists, unit '1' does not).*
- [**Rig**](../../typeclass/monoid/rig.md) (Ring without 'n' - negative): Ring without "negatives" (no subtraction, but has multiplication and 1).
  *Example: natural numbers with 0 and 1.*
- [**CommutativeSemiring**](../../typeclass/monoid/csemiring.md): Commutative semiring (multiplication order does not matter).
- [**CommutativeRig**](../../typeclass/monoid/rig.md): Commutative Rig (multiplication order does not matter).
- [**Ring**](../../typeclass/monoid/ring.md): Full-fledged ring: has addition, subtraction, multiplication, and unit.
  *Example: integers (`Int`).*
- [**CommutativeRing**](../../typeclass/monoid/cring.md): Commutative ring.
  *Example: integers (3*4 = 4*3).*
- [**Semifield**](../../typeclass/monoid/semifield.md): Semifield: has multiplication, unit, and inverse (division), but no subtraction.
- [**CommutativeSemifield**](../../typeclass/monoid/csemifield.md): Commutative semifield.
- [**GCDRing**](../../typeclass/monoid/gcdring.md): **GCD rings**.
- [**EuclideanRing**](../../typeclass/monoid/euclidean-ring.md): **Euclidean ring**.
- [**Field**](../../typeclass/monoid/field.md): **Field**: has everything: addition, subtraction, multiplication, division (except by zero).
  *Example: rational numbers, real numbers (use Double with caution).*

| Name                       | Has `negate` (subtraction)? | Has `1` (neutral for \*)? | Has `reciprocal` (inverse for \*)? | `*` commutative? | Has `gcd`/`lcm`? | Has `quot`/`mod`? |
|:---------------------------|:----------------------------|:--------------------------|:-----------------------------------|:-----------------|:-----------------|:------------------|
| **Semiring**               |                             |                           |                                    |                  |                  |                   |
| **Rng**                    | ✓                           |                           |                                    |                  |                  |                   |
| **Rig**                    |                             | ✓                         |                                    |                  |                  |                   |
| **CommutativeSemiring**    |                             |                           |                                    | ✓                |                  |                   |
| **CommutativeRng**         | ✓                           |                           |                                    | ✓                |                  |                   |
| **CommutativeRig**         |                             | ✓                         |                                    | ✓                |                  |                   |
| **Ring**                   | ✓                           | ✓                         |                                    |                  |                  |                   |
| **CommutativeRing**        | ✓                           | ✓                         |                                    | ✓                |                  |                   |
| **Semifield**              |                             | ✓                         | ✓                                  |                  |                  |                   |
| **CommutativeSemifield**   |                             | ✓                         | ✓                                  | ✓                |                  |                   |
| **GCDRing**                | ✓                           | ✓                         |                                    | ✓                | ✓                |                   |
| **EuclideanRing**          | ✓                           | ✓                         |                                    | ✓                | ✓                | ✓                 |
| **Field**                  | ✓                           | ✓                         | ✓                                  | ✓                | ✓                | ✓                 |

## Section 4: Lattice-like Structures

The `algebra.lattice` package presents structures that may resemble rings but operate by different rules.

Instead of `plus` (addition) and `times` (multiplication) here we have:

- **`meet`** — analogous to logical **AND** or **minimum** operation ("greatest lower bound").
- **`join`** — analogous to logical **OR** (OR) or **maximum** operation ("least upper bound").

**Properties of `meet` and `join`:**

- They are always associative, commutative, and idempotent (`a ∧ a = a`).
- Each separately forms a **semilattice**.

**Additional elements:**

- **`zero`**: if present, then `join(a, zero) = a` (zero is the lower bound, joining with it does not change the element).
- **`one`**: if present, then `meet(a, one) = a` (one is the upper bound).
- **Absorption law**: if both are present, then `meet(a, join(a, b)) = join(a, meet(a, b)) = a`.
- **Distributivity**: `meet` is distributive over `join` and vice versa.
- **`imp` (implication)**: a special operation meaning "if... then...".

### Lattice-like Structures Table

- [**JoinSemilattice**](../../typeclass/lattice/join-semilattice.md): Join semilattice (can only `join`).
- [**MeetSemilattice**](../../typeclass/lattice/meet-semilattice.md): Meet semilattice (can only `meet`).
- [**BoundedJoinSemilattice**](../../typeclass/lattice/bounded-join-semilattice.md): Bounded join semilattice with zero (lower bound).
- [**BoundedMeetSemilattice**](../../typeclass/lattice/bounded-meet-semilattice.md): Bounded meet semilattice with one (upper bound).
- [**Lattice**](../../typeclass/lattice/lattice.md): Full lattice (has both meet and join).
- [**DistributiveLattice**](../../typeclass/lattice/distributive-lattice.md): Distributive lattice.
  _Example: sets with `∩` and `∪`._
- [**BoundedLattice**](../../typeclass/lattice/bounded-lattice.md): Lattice with bounds (has both 0 and 1).
- [**BoundedDistributiveLattice**](../../typeclass/lattice/bounded-distributive-lattice.md): Distributive lattice with bounds.
- [**Logic**](../../typeclass/lattice/logic.md): **General logic**. Basic type class for logical systems with negation, satisfying weak De Morgan laws.
  Provides `and`, `or`, `not` and derived operations (`xor`, `nand`, `nor`, `nxor`).
  _Example: fuzzy logic, three-valued logic._
- [**DeMorgan**](../../typeclass/lattice/de-morgan.md): **De Morgan algebra**. Extends `Logic`, adding full De Morgan laws and double negation (`¬¬a = a`).
  However, it does not guarantee the law of excluded middle (`a ∨ ¬a = 1`) or the law of non-contradiction (`a ∧ ¬a = 0`).
  _Example: fuzzy logic, logic with floating truth values._
- [**Heyting**](../../typeclass/lattice/heyting.md): **Heyting algebra**. Lattice with implication (`imp`) and pseudocomplement (`complement`).
  Guarantees the law of non-contradiction (`a ∧ ¬a = 0`), but not the law of excluded middle. Models intuitionistic logic.
  _Example: logic without law of excluded middle, type systems with implication._
- [**GenBool**](../../typeclass/lattice/gen-bool.md): **General Boolean algebra**. Boolean algebra without top bound (`top`).
  Has relative complement (`without`), but no absolute complement.
  Useful for working with sets without a universal set.
  _Example: sets with operations ∩, ∪, \ (difference), but without a universal set._
- [**Bool**](../../typeclass/lattice/bool.md): **Boolean algebra**. Classical logic with law of excluded middle (`a ∨ ¬a = true`),
  law of non-contradiction (`a ∧ ¬a = false`), double negation, and full De Morgan laws.
  Combines properties of `Heyting` and `GenBool`.
  _Example: `true`/`false`, bitmasks, classical logic._

| Name                               | Has `join` (OR)? | Has `meet` (AND)? | Has `zero`? | Has `one`? | Distributive? | Has `imp`? | Excludes middle? |
|:-----------------------------------|:----------------:|:-----------------:|:-----------:|:----------:|:-------------:|:----------:|:----------------:|
| **JoinSemilattice**                |        ✓         |                   |             |            |               |            |                  |
| **MeetSemilattice**                |                  |        ✓          |             |            |               |            |                  |
| **BoundedJoinSemilattice**         |        ✓         |                   |      ✓      |            |               |            |                  |
| **BoundedMeetSemilattice**         |                  |        ✓          |             |     ✓      |               |            |                  |
| **Lattice**                        |        ✓         |        ✓          |             |            |               |            |                  |
| **DistributiveLattice**            |        ✓         |        ✓          |             |            |       ✓       |            |                  |
| **BoundedLattice**                 |        ✓         |        ✓          |      ✓      |     ✓      |               |            |                  |
| **BoundedDistributiveLattice**     |        ✓         |        ✓          |      ✓      |     ✓      |       ✓       |            |                  |
| **Heyting**                        |        ✓         |        ✓          |      ✓      |     ✓      |       ✓       |     ✓      |                  |
| **GenBool**                        |        ✓         |        ✓          |      ✓      |            |       ✓       |            |                  |
| **Bool**                           |        ✓         |        ✓          |      ✓      |     ✓      |       ✓       |     ✓      |        ✓         |

### Important Relationships Between Structures

1. **Connection to rings:** `BoundedDistributiveLattice` (bounded distributive lattice) gives you [`CommutativeRig`
   (commutative semiring)](../../typeclass/monoid/crig.md), but **not the other way around**.
   Semirings do not have distributivity of `+` over `*` in the sense defined for lattices.
2. **Bool and rings:** Boolean algebra (`Bool`) generates a **[Boolean ring (`BoolRing`)](../../typeclass/monoid/bool-ring.md)**,
   where every element is its own negation.
   However, note that the `.asBoolRing` method **is not** an extension of `.asCommutativeRig`,
   because the `plus` (addition) operation in Boolean ring is defined differently (as XOR), not as `join` (OR).

---

**References:**

- [Cats home page][home]

[home]: https://typelevel.org/cats/
