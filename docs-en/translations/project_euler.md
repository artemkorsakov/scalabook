---
layout: default
title: Project Euler
parent: Translations
summary: Solutions to Project Euler problems implemented in Scala.
nav_order: 2
---

# Project Euler
{: .no_toc }

{% include toc.html %}

# Project Euler

> [Project Euler](https://projecteuler.net/) is a website dedicated to a series of mathematical and computational problems to be solved with computer programs.
> Here you can find solutions written in Scala.

<div class="book-container">

<div class="book" style="background: url('/assets/img/project_euler.jpg') rgba(255, 255, 255, 0.4) no-repeat; background-size: contain;"></div>

</div>

## Problem Groups

* [Problems 1-20](project_euler/project_euler_1_20.md)
* [Problems 21-40](project_euler/project_euler_21_40.md)
* [Problems 41-60](project_euler/project_euler_41_60.md)
* [Problems 61-80](project_euler/project_euler_61_80.md)
* [Problems 81-100](project_euler/project_euler_81_100.md)

---

### What is Project Euler?

Project Euler is a collection of challenging mathematical and computational problems that require both mathematical insight and programming skills to solve.

The problems range from relatively simple number theory puzzles to complex algorithmic challenges. They are designed to:

1. Encourage creative thinking
2. Teach new mathematical concepts
3. Demonstrate the power of combining mathematics and programming

---

### How to Use This Guide

Each section groups related problems by complexity and mathematical area:

- **Problems 1-20**: Basic number theory and simple algorithms
- **Problems 21-40**: Sequences, divisibility, and combinatorics
- **Problems 41-60**: Prime numbers, arithmetic progressions, and optimization
- **Problems 61-80**: Cyclic numbers, polygonal numbers, and advanced algorithms
- **Problems 81-100**: Matrix operations and complex problem-solving

---

## Solving Problems in Scala

Scala's functional nature makes it well-suited for solving Project Euler problems:

```scala
// Example: Sum of multiples of 3 or 5 below 1000
val result = (1 until 1000).filter(n => n % 3 == 0 || n % 5 == 0).sum
println(result) // 233168
```

Scala's type system, immutability, and rich standard library help write clean and efficient solutions.
