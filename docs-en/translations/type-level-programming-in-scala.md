---
layout: default
title: Type-Level Programming in Scala
parent: Translations
summary: Advanced type-level programming techniques in Scala 3.
nav_order: 7
---

# Type-Level Programming in Scala
{: .no_toc }

{% include toc.html %}

# Type-Level Programming in Scala

Type-level programming allows us to encode computations at the type level, providing
compile-time guarantees that would be difficult or impossible to verify at runtime.

## Natural Numbers at the Type Level

```scala
sealed trait Nat
case class Zero extends Nat
case class Succ[N <: Nat](pred: N) extends Nat

type One = Succ[Zero]
type Two = Succ[One]
type Three = Succ[Two]
```

## Type-Level Addition

```scala
trait Add[A <: Nat, B <: NAT] <: Nat
case class AddZero[A <: Nat]() extends Add[Zero, A] {
  type Result = A
}

case class AddSucc[A <: Nat, B <: Nat, Sum <: Nat](
  using add: Add[A, Pred[B]] { type Result = Sum }
) extends Add[Succ[A], B] {
  type Result = Sum
}
```

## Lists at the Type Level

```scala
sealed trait List[+A]
case class CNil extends List[Nothing]
case class CCons[+H, +T <: List[?]](head: H, tail: T) extends CList[H, T]
```

## Type-Level Boolean Operations

```scala
trait Bool
case class True extends Bool
case class False extends Bool

trait And[A <: Bool, B <: Bool] <: Bool
case class AndTrue() extends And[True, True] { type Out = True }
case class AndFalse[A <: Bool]() extends And[A, False] { type Out = False }
```

## Dependent Types

Scala 3 supports some dependent type patterns:

```scala
def first[A, B] : (A, B) => A = _._1
def second[A, B] : (A, B) => B = _._2
```

## Example: Vector Length

```scala
final case class Vec[N <: Int, T](values: Array[T]):
  def unsafeAt(n: Int): T = values(n)
  def zip[T2](other: Vec[N, T2]): Vec[N, (T, T2)] =
    Vec(N, values.zip(other.values): Array[T, T2])
```

## Shapeless

The [Shapeless](https://github.com/milessabin/shapeless) library provides powerful
type-level programming utilities:

```scala
import shapeless._

// HLists
type Person = String :: Int :: Boolean :: HNil
val john: Person = "John" :: 30 :: true :: HNil

// Generic conversion
val person = Generic[Person]
```

## Further Reading

- [Type-Level Programming in Scala](https://www.slideshare.net/lpgitt/reifying-scala-type-operators)
- [Shapeless](https://github.com/milessabin/shapeless)
- [Cats](https://typelevel.org/cats/)
- Iron refinements
