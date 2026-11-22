# Scalabook

## About the project

**ScalaBook** is a continuously updated workbook on functional programming in Scala,
covering the following topics:

- Fundamentals of functional programming
- Algorithms and data structures
- Type classes and type-level programming in Scala
- Practical exercises to consolidate the material

In the era of multi-core processors and parallel computing, FP offers the following advantages:

- **Data safety**: immutability excludes state races.
- **Predictability**: explicit error handling through types (e.g., `Option`, `Either`) instead of exceptions.

**Example**:

```dotty
// Problem of the imperative approach: hidden errors through exceptions
def divide(a: Int, b: Int): Int =
  if b == 0 then throw new Exception("Division by zero") else a / b

// FP solution: the fact that the function is not defined for every input
// is explicitly reflected in its type
def divide(a: Int, b: Int): Option[Int] =
  if b == 0 then None else Some(a / b)
```

Using `Option` makes impossible states (such as division by zero) visible at compile time,
which reduces the number of runtime errors.

### Algorithms and data structures

In the world of big data, inefficient algorithms lead to significant resource costs.

Example:

Checking `list.length > 0` of a linked list for emptiness requires **O(n)** operations —
for a list of one million elements this is **a million** extra operations.
Whereas `list.nonEmpty` is a single operation.

Even with the rise of AI that can describe and explain code,
understanding algorithms allows you to consciously choose solutions (for example, `HashMap` vs `List` for lookup) and
optimize code in high-load systems.

### Practical focus

Theory without practice is useless. Scalabook includes:

- **Exercises**:
    - [a set of functional programming tasks](https://gitverse.ru/scalabook/fp).
    - [a set of type class tasks](https://gitverse.ru/scalabook/typeclasses)
- [**Practicum**](practice/index.md): a step-by-step creation of the "Development Plan" task-tracking service.

## Main sections

| Section | Description |
| --- | --- |
| [Functional programming](fp/index.md) | Core concepts and principles of functional programming |
| [Type classes](typeclass/index.md) | Exploring type classes and category theory |
| [Algorithms](algorithms/index.md) | Implementing classic algorithms in a functional style |
| [Data structures](data-structures/index.md) | Functional data structures |
| [Cats effect](cats-effect/index.md) | Overview of Cats effect - a pure asynchronous runtime |
| [Tooling](libs/index.md) | Overview of popular Scala libraries |
| [Practicum](practice/index.md) | Building a Scala application using functional programming principles |
| [Translations](translations/index.md) | Translations of articles, learning materials and talks into Russian. Solutions to exercises from various books and websites |
| [Resources](extra/index.md) | Useful links to books, articles, online courses and videos for deeper study |

## Project goal

Practice shows that Scala has a high entry barrier. That is the price of its expressiveness, flexibility and power.
But is the high entry barrier a problem for developers who are just starting to learn Scala and FP?
Rather, it is a problem for those who already know Scala, who have jumped across the gap,
but cannot or do not want to build a bridge across that gap.
And shout to others: "Jump too! I did it!"

**ScalaBook's goal** is to build that "bridge".
A high entry barrier is not a reason for pride in "I did it!",
but a reason to think: what have you done to lower that barrier?

Construction of the Manhattan Bridge, 1909:

![Manhattan Bridge](images/Manhattan_Bridge_Construction_1909.jpg)

## Feedback

Your feedback is fuel for the project's development.
Whether you fix typos, suggest new topics, or simply say "thank you" — every response matters.

**How can you help?**

- ✏️ **Fix a mistake:**
  click *"Edit this page"* (in the right-hand menu) and submit your correction — even a comma matters!

- 💡 **Suggest an improvement:**
  leave a comment — every objective comment matters.

- ⭐ **Add to favorites:**
  if the project is useful, add it to your favorites on [GitVerse][home] — it's the best signal we're heading in the right direction.

- 📬 **Contact directly:**
  see contacts below.

## Contacts

- **Project source code**: available on [GitVerse][home]
- **My profiles**:
    - Main: [GitVerse](https://gitverse.ru/artemkorsakov)
    - Additional: [GitHub](https://github.com/artemkorsakov)
- **Contact**: [write an email](mailto:artemkorsakov@mail.ru)
- **Collaboration**: [IT company "Cryptonite" vacancies](https://career.kryptonite.ru/vacancies/?locations=moscow&amp;search=Scala)

## Available formats

- [Website](https://scalabook.ru)
- [Epub](https://scalabook.ru/scalabook.epub)

[home]: https://gitverse.ru/artemkorsakov/scalabook