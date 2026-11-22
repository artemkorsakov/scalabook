# Laws Testing

## Introduction

Laws are an important part of Cats. 
They ensure that user-defined type class instances behave predictably 
and comply with the mathematical requirements defined for each type class. 
Cats uses the [discipline](https://github.com/typelevel/discipline) library to define type class laws 
and generate tests based on [ScalaCheck](https://github.com/rickynils/scalacheck).

Laws testing allows library and application developers to verify 
that their own type class instances are correct and satisfy all required rules. 
This is critically important for maintaining the correctness of functional programs, where type classes play a central role.

## Getting Started

First, you need to add the `cats-laws` dependency to your `build.sbt` file:

```dotty
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-laws" % "@VERSION@" % Test,
)
```

This dependency contains all law definitions for Cats type classes and tools for testing them. 
It is scoped only to the test section (`% Test`) since it is not needed in application code.

## Example: Testing a Functor Instance

Let's start by creating a custom data type and a `Functor` instance for it. 
We'll use a binary tree as an example:

```dotty
import cats.*

sealed trait Tree[+A]
case object Leaf                                        extends Tree[Nothing]
case class Node[A](p: A, left: Tree[A], right: Tree[A]) extends Tree[A]

object Tree:
  given functorTree: Functor[Tree] = new Functor[Tree]:
    def map[A, B](tree: Tree[A])(f: A => B): Tree[B] =
      tree match
        case Leaf                 => Leaf
        case Node(p, left, right) => Node(f(p), map(left)(f), map(right)(f))
```

We've defined a simple binary tree where each node holds a value and two subtrees. 
The `map` implementation recursively applies the function to all nodes, preserving the tree structure.

Now we want to verify that our `Functor` implementation indeed satisfies all laws. 
For this, Cats provides tests in the `cats.laws.discipline.*` package. 
Each type class has a corresponding object with methods for creating a set of rules (`RuleSet`). 
For example, `FunctorTests` is used for `Functor`.

```dotty
cats.laws.discipline.FunctorTests[Tree].functor[Int, Int, String].all
```

This code creates a `ScalaCheck` `Properties` containing all laws for `Functor`.

### Creating an Eq Instance

Most laws require comparing values to verify correctness. 
For this purpose, an `Eq` instance is needed. For simplicity, we can use `Eq.fromUniversalEquals`, 
which compares values using Scala's standard equality:

```dotty
given eqTree[A: Eq]: Eq[Tree[A]] = Eq.fromUniversalEquals
```

However, in real projects it is recommended to use more precise `Eq` instances 
that account for the semantics of your data type.

### Generating Test Data with Arbitrary

ScalaCheck requires `Arbitrary` instances for the data types we test. 
`Arbitrary` is responsible for generating random values for testing. 
We can define a generator for our tree:

```dotty
import org.scalacheck.{Arbitrary, Gen}

given arbTree[A: Arbitrary]: Arbitrary[Tree[A]] =
  Arbitrary(Gen.oneOf(
    Gen.const(Leaf),
    Arbitrary.arbitrary[A].map(e => Node(e, Leaf, Leaf))
  ))
```

In this example, we create a generator that with equal probability produces either an empty leaf (`Leaf`) 
or a node with one value and two empty subtrees. 
For more complex tests, you can create more detailed generators 
that produce trees of arbitrary depth and structure.

### Integration with Testing Frameworks

Now we need to convert `ScalaCheck` `Properties` into tests that our test framework can run. 
The [discipline](https://github.com/typelevel/discipline) library provides a helper function `checkAll` for three popular frameworks:

1. **Specs2**: extend your test class with `org.typelevel.discipline.specs2.Discipline` (provided by `discipline-specs2`)
2. **ScalaTest**: extend your test class with `org.typelevel.discipline.scalatest.FunSuiteDiscipline` 
   (provided by `discipline-scalatest`) and `org.scalatest.funsuite.AnyFunSuiteLike`
3. **MUnit**: extend your test class with `munit.DisciplineSuite` (provided by `discipline-munit`)
4. For other test frameworks, you can use their ScalaCheck integration directly.

Let's consider an example with [weaver-tests](https://typelevel.org/weaver-test/features/discipline.html):

Add this dependency:

```dotty
libraryDependencies +=  "org.typelevel" %% "weaver-discipline" % "0.13.0" % Test
```

And the test suite itself: 

```dotty
import cats.laws.discipline.FunctorTests
import org.scalacheck.{Arbitrary, Gen}
import weaver.*
import weaver.discipline.*

object TreeSuite extends FunSuite with Discipline:
  checkAll("Tree.FunctorLaws", FunctorTests[Tree].functor[Int, Int, String])
```

### Running Tests and Interpreting Results

When we run `test` in the sbt console, ScalaCheck tests whether the `Functor` laws hold for our `Tree` type. 
You should see something like:

```
[info] ru.scalabook.libs.cats.TreeSuite
[info] + Tree.FunctorLaws: functor.covariant composition 24ms
[info] + Tree.FunctorLaws: functor.covariant identity 17ms
[info] + Tree.FunctorLaws: functor.invariant composition 7ms
[info] + Tree.FunctorLaws: functor.invariant identity 1ms
[info] + Tree.FunctorLaws: functor.mapOrKeepToMapEquivalence 3ms
```

Each of these tests checks a specific law:

- **covariant composition** — verifies that `map(f).map(g)` is equivalent to `map(f andThen g)` (composition)
- **covariant identity** — verifies that `map(identity)` is equivalent to `identity` (identity)
- **invariant composition** and **invariant identity** — similar laws for the invariant functor
- **mapOrKeepToMapEquivalence** — verifies that `fa.mapOrKeep(pf)` is equivalent to `fa.map(a => pf.applyOrElse(a, identity[A1]))`

It has been successfully demonstrated that the `Tree` data type satisfies the `Functor` laws.

## Testing cats.kernel Instances

For most type classes included in Cats, the approach described above works well. 
However, law tests for type classes from `cats.kernel` (e.g., `Semigroup`, `Monoid`, `Group`, `Semilattice`) 
are located in the `cats.kernel.laws.discipline.*` package.

Let's test this by defining a `Semigroup` instance for our `Tree` type:

```dotty
given semigroupTree[A: Semigroup]: Semigroup[Tree[A]] =
  new Semigroup[Tree[A]]:
    def combine(x: Tree[A], y: Tree[A]): Tree[A] = (x, y) match
      case (Leaf, _)                                          => Leaf
      case (_, Leaf)                                          => Leaf
      case (Node(xp, xLeft, xRight), Node(yp, yLeft, yRight)) =>
        Node(
          Semigroup[A].combine(xp, yp),
          combine(xLeft, yLeft),
          combine(xRight, yRight)
        )
```

Here we implement the `combine` operation for trees: if either tree is empty (`Leaf`), the result is also empty; 
otherwise, we combine the node values and recursively combine the left and right subtrees.

Then we can add tests for `Semigroup` to our test suite:

```dotty
import cats.kernel.laws.discipline.SemigroupTests
import cats.laws.discipline.FunctorTests
import org.scalacheck.{Arbitrary, Gen}
import weaver.*
import weaver.discipline.*

object TreeSuite extends FunSuite with Discipline:
  checkAll("Tree.FunctorLaws", FunctorTests[Tree].functor[Int, Int, String])

  checkAll("Tree[Int].SemigroupLaws", SemigroupTests[Tree[Int]].semigroup)
```

Now our tests check not only functor laws but also semigroup laws, including the associativity of the `combine` operation.

```
[info] + Tree.Int].SemigroupLaws: semigroup.associative 2ms
[info] + Tree[Int].SemigroupLaws: semigroup.combineAllOption 6ms
[info] + Tree[Int].SemigroupLaws: semigroup.intercalateCombineAllOption 9ms
[info] + Tree[Int].SemigroupLaws: semigroup.intercalateIntercalates 1ms
[info] + Tree[Int].SemigroupLaws: semigroup.intercalateRepeat1 0ms
[info] + Tree[Int].SemigroupLaws: semigroup.intercalateRepeat2 1ms
[info] + Tree[Int].SemigroupLaws: semigroup.repeat1 1ms
[info] + Tree[Int].SemigroupLaws: semigroup.repeat2 0ms
[info] + Tree[Int].SemigroupLaws: semigroup.reverseCombineAllOption 5ms
[info] + Tree[Int].SemigroupLaws: semigroup.reverseRepeat1 0ms
[info] + Tree[Int].SemigroupLaws: semigroup.reverseRepeat2 1ms
[info] + Tree[Int].SemigroupLaws: semigroup.reverseReverses 0ms
```

## Important Considerations

### Choosing the Right Eq

Using `Eq.fromUniversalEquals` may be insufficient for complex data types. 
It is recommended to define more precise `Eq` instances that account for structural equality 
and do not rely on `toString` or other non-deterministic representations.

### Quality of Data Generation

The effectiveness of laws testing heavily depends on the quality of `Arbitrary` generators. 
Good generators should cover various edge cases and provide sufficient diversity of test data.

### Execution Time

Laws tests can be resource-intensive, especially for complex data types and type classes with many laws. 
Ensure your generators are efficient and tests do not take too long to run.

### Testing All Type Classes

For full coverage, it is recommended to test all type classes you define for your data types. 
This includes not only the core classes (`Functor`, `Applicative`, `Monad`) 
but also classes from `cats.kernel` (`Semigroup`, `Monoid`), as well as specialized classes (`Traverse`, `Foldable`, etc.).

---

**References:**

- [Cats home page][home]

[home]: https://typelevel.org/cats/
