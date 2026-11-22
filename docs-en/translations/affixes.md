---
layout: default
title: Affixes
parent: Translations
summary: Using Scala's affix system for type-safe units and measurements.
nav_order: 6
---

# Affixes
{: .no_toc }

{% include toc.html %}

# Affixes

Affixes in Scala 3 allow us to represent mathematical quantities with units of measurement,
providing compile-time type safety for dimensional analysis.

## Basic Concept

An affix represents a quantity with both a magnitude and a unit:

```scala
// Distance with meters
val distance: Affix[Meters, Double] = Affix(100, Meters)

// Time with seconds
val time: Affix[Seconds, Double] = Affix(10, Seconds)
```

## Defining Affixes

```scala
import cats.Monoid

opaque type Affix[Units, A] = A

object Affix:
  def apply[Units, A](value: A): Affix[Units, A] = value

  extension [A](a: A)
    def meters: Affix[Meters, A] = a
    def seconds: Affix[Seconds, A] = a
    def kilograms: Affix[Kilograms, A] = a
```

## Operations on Affixes

```scala
extension [A: Numeric](a: Affix[Units, A])
  def +(other: Affix[Units, A])(using Numeric[A]): Affix[Units, A] =
    implicitly[Numeric[A]].plus(a, other)

  def -(other: Affix[Units, A])(using Numeric[A]): Affix[Units, A] =
    implicitly[Numeric[A]].minus(a, other)

  def *(scalar: Int)(using Numeric[A]): Affix[Units, A] = a
```

## Dimensional Analysis

Affixes provide type safety for dimensional analysis:

```scala
// Valid: speed = distance / time
def speed(dist: Affix[Meters, Double], time: Affix[Seconds, Double]): Affix[MetersPerSecond, Double] =
  dist / time

// Invalid: mixing incompatible types
// addLength(10.meters, 5.seconds) // Compile error!
```

## Examples

### Computing Speed

```scala
def computeSpeed(distance: Affix[Meters, Double], duration: Affix[Seconds, Double]): Double =
  val speed = distance / duration
  speed.value
```

### Converting Units

```scala
extension [A: Numeric](a: Affix[Units, A])
  def kmToMeters: Affix[Meters, Double] = a.value * 1000
  def hoursToSeconds: Affix[Seconds, Double] = a.value * 3600
```

## Further Reading

- [Affix library documentation](https://github.com/tpolecat/affix)
- [Cats](https://typelevel.org/cats/)
- [Scala 3 Type Operators](https://docs.scala-lang.org/scala3/reference/type-operator.html)
