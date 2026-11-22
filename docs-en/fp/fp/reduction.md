# Reduction

## Concept of Reduction

_Reduction_ (or _rewriting_, from English _reduction_) is the process of sequentially transforming a complex expression
into its simplest equivalent form through the application of transformation rules.
This process is analogous to mathematical computation, where we gradually simplify expressions to their final value.

## Reduction Example in Practice

Consider a detailed breakdown of computing the expression `square(3 + 4)`:

1. **First computation strategy** (inner rewriting): `square(3 + 4) => square(7) => 7 * 7 => 49`
2. **Alternative approach** (outer rewriting): `square(3 + 4) => (3 + 4) * (3 + 4) => 7 * (3 + 4) => 7 * 7 => 49`

Key observations:

- Both paths lead to the same result (`49`)
- The final expression `49` is in _canonical form_ (normal form) — it cannot be reduced further
- The order of applying reductions may differ, but the property of
  _confluence_ ([Church-Rosser property](https://ru.wikipedia.org/wiki/%D0%A2%D0%B5%D0%BE%D1%80%D0%B5%D0%BC%D0%B0_%D0%A7%D1%91%D1%80%D1%87%D0%B0_%E2%80%94_%D0%A0%D0%BE%D1%81%D1%81%D0%B5%D1%80%D0%B0)) is preserved

## Formal Expression System

Consider a formal language for representing integers with operations:

- Base case: `zero` — an atomic expression
- Recursive cases:
    - `(succ e)` — the next number (_successor_)
    - `(pred e)` — the previous number (_predecessor_)

**Reduction rules**:

1. `(succ (pred e)) => e`
2. `(pred (succ e)) => e`

## Algorithm for Normalizing Expressions

Step-by-step simplification of the expression `(succ(pred(succ(pred(pred zero)))))`:

1. **First iteration**:
    We find a nested pair `succ(pred ...)`:
    ```
    (succ (pred (succ (pred (pred zero)))))
    => (succ (pred (pred zero)))
    ```
2. **Second iteration**:
    Apply the same rule:
    ```
    (succ (pred (pred zero)))
    => (pred zero)
    ```
3. **Result**:
    The expression `(pred zero)` contains no reducible `succ/pred` pairs, therefore:
     - It is in canonical form
     - Further reduction is not possible

## Implementation

```dotty
enum Expr:
  self =>

  case Zero
  case Succ(expr: Expr)
  case Pred(expr: Expr)

  def normalize: Expr =
    self match
      case Succ(Pred(e)) => e.normalize // Rule 1
      case Pred(Succ(e)) => e.normalize // Rule 2
      case Succ(e)       => Succ(e.normalize) // Recursive case
      case Pred(e)       => Pred(e.normalize) // Recursive case
      case _             => self // Base case

import Expr.*
val expr = Succ(Pred(Succ(Pred(Pred(Zero)))))
expr.normalize // Result: Pred(Zero)
```

## The Meaning of Reduction in Functional Programming

Here are a few reasons why reduction applies in functional programming:

- **Pure functions**: A functional programming language is built on pure functions,
  which have no side effects and return the same value for the same inputs.
  This makes reduction possible because each function can be reduced to its value independently
  of context.
- **Refactoring and optimization**: Because functions can be reduced to their values,
  code becomes easier to refactor and optimize.
  For example, complex expressions can be replaced with simple values without changing program behavior.
- **Simplicity of analysis and correctness verification**: Since every expression can be reduced to its value,
  analyzing the program becomes simpler. You can verify the program's correctness by
  examining each function separately and ensuring it returns the correct value.

## Exercise 1.3.1: Step-by-Step Reduction of Arithmetic Expressions

> [Repository with exercises and instructions][sample]

**Goal**: Implement a function for performing one step of arithmetic expression reduction.

**Context**:

Arithmetic expressions are represented using an algebraic data type (ADT) `Expr`, which includes:

- Numeric literals (`Num`)
- Arithmetic operations (`Add`, `Sub`, `Mul`)
- Variables (`Var`)

**Task**:

Implement `oneStepReduction`, which performs one step of expression reduction following these rules:

1. If both operands of a binary operation are numbers — compute the result
2. If at least one operand is not a number — apply reduction recursively to sub-expressions
3. Variables and numbers are not reduced further

```dotty
enum Expr:
  case Num(value: Int)
  case Add(left: Expr, right: Expr)
  case Sub(left: Expr, right: Expr)
  case Mul(left: Expr, right: Expr)
  case Var(name: String)

def oneStepReduction(expr: Expr): Expr = ???
```

**Requirements**:

1. The function must return an expression of the same type `Expr`
2. Each call performs exactly one step of reduction
3. Reduction uses "call-by-value" strategy (arguments first)

**Examples**:

```dotty
// Addition computation
Add(Num(2), Num(3))                  => Num(5)   
// Reduce right sub-expression       
Add(Var("x"), Mul(Num(2), Num(3)))   => Add(Var("x"), Num(6)) 
// Cannot be reduced
Mul(Var("y"), Sub(Var("x"), Num(1))) => No change   
```

@:note(🔍 Show Solution)

The `oneStepReduction` function performs step-by-step reduction of arithmetic expressions,
recursively applying operations to numbers: for `Add`, `Sub`, and `Mul` expressions with numeric operands,
the result is computed, while for expressions with variables or nested operations,
reduction is recursively applied to sub-expressions
until numeric values or irreducible variables are obtained.

```dotty
def oneStepReduction(expr: Expr): Expr =
  import Expr.*
  expr match
    // Base cases — don't reduce further
    case Num(_) | Var(_) => expr

    // Addition reduction
    case Add(Num(x), Num(y)) => Num(x + y)
    case Add(e1, e2) => Add(oneStepReduction(e1), oneStepReduction(e2))

    // Subtraction reduction
    case Sub(Num(x), Num(y)) => Num(x - y)
    case Sub(e1, e2) => Sub(oneStepReduction(e1), oneStepReduction(e2))

    // Multiplication reduction
    case Mul(Num(x), Num(y)) => Num(x * y)
    case Mul(e1, e2) => Mul(oneStepReduction(e1), oneStepReduction(e2))
```

@:@

## Exercise 1.3.2: Checking Canonical Form

**Goal**: Implement a check function to determine if an expression is in canonical (normal) form.

**Definition**:

An expression is considered canonical if it cannot be reduced further (i.e., applying `oneStepReduction` does not
change it).

**Task**:

Implement `isCanonical`, which returns `true` if the expression is in canonical form.

```dotty
def isCanonical(expr: Expr): Boolean = ???
```

**Requirements**:

1. The function must correctly determine if further reduction is possible
2. Must work for all variants of `Expr`

**Examples**:

```dotty
Num(5)                     => true   // Number — canonical form
Var("x")                   => true   // Variable — canonical form
Add(Num(2), Num(3))        => false  // Can be reduced to Num(5)
Add(Var("x"), Num(3))      => true   // Cannot be reduced further
```

@:note(🔍 Show Solution)

The `isCanonical` function checks whether the expression `expr` is in canonical form (i.e., fully reduced),
by comparing the original expression with the result of applying one step of reduction to it:
if they are equal, it means no further simplification is possible, and the expression is considered canonical.

```dotty
def isCanonical(expr: Expr): Boolean =
  oneStepReduction(expr) == expr
```

@:@

## Exercise 1.3.3: Full Expression Reduction

**Goal**: Implement a function for fully reducing an expression to canonical form.

**Task**:

Implement `reduction`, which sequentially applies `oneStepReduction` until
the expression reaches canonical form.

```dotty
def reduction(expr: Expr): Expr = ???
```

**Requirements**:

1. The function must use recursion for efficiency
2. Must correctly handle all expression variants
3. Must terminate when canonical form is reached

**Examples**:

```dotty
Add(Num(2), Num(3))                           => Num(5)
Sub(Mul(Add(Num(1), Num(1)), Num(3)), Num(2)) => Num(4)
```

@:note(🔍 Show Solution)

The `reduction` function performs full reduction of an expression to canonical form,
using a helper recursive function `loop`: at each step, it checks whether the
current expression is canonical (no further simplification possible),
and if not — applies one-step reduction
and repeats the process until an irreducible state is reached.

```dotty
def reduction(expr: Expr): Expr =
  def loop(current: Expr): Expr =
    if isCanonical(current) then current
    else loop(oneStepReduction(current))
  
  loop(expr)
```

@:@

## Exercise 1.3.4: Optimizing Arithmetic Expressions via Extended Reduction

**Goal**:

Implement an enhanced one-step reduction mechanism for arithmetic expressions
that not only computes simple operations but also applies basic algebraic optimizations to simplify expressions.

**Context**:

In real compilers and interpreters, simple arithmetic optimizations (like reducing operations with `0` or `1`)
significantly improve performance. In this exercise, we extend the basic reduction by adding such optimizations.

**Task**:

Implement `optimizedOneStepReduction` to support the following optimizations:

1. Simplify operations with zero:
    - `x + 0` → `x`
    - `0 + x` → `x`
    - `x - 0` → `x`
    - `x - x` → `0`
    - `0 * x` → `0`
    - `x * 0` → `0`
2. Simplify operations with one:
    - `x * 1` → `x`
    - `1 * x` → `x`

**Requirements**:

1. Preserve all capabilities from the previous version of reduction
2. Add handling of edge cases with `0` and `1`
3. Guarantee that each function application performs exactly one step of reduction
4. Support correct operation with variables

**Input and output examples**:

```dotty
// Base cases
Add(Num(2), Num(3))             => Num(5)       // Normal computation
Add(Var("x"), Num(0))           => Var("x")     // Optimization +0
Sub(Num(5), Num(0))             => Num(5)       // Optimization -0
Mul(Num(0), Var("y"))           => Num(0)       // Optimization *0

// Complex cases
Add(Add(Num(2), Num(0)), Num(3)) => Add(Num(2), Num(3))
Mul(Sub(Num(1), Num(1)), Var("x")) => Mul(Num(0), Var("x"))

// No-change cases
Add(Var("x"), Var("y"))         => Add(Var("x"), Var("y"))
```

This exercise demonstrates how even simple optimizations can significantly improve computational efficiency,
especially when working with large or complex expressions.

@:note(🔍 Show Solution)

The `optimizedOneStepReduction` function performs one-step reduction of arithmetic expressions with additional optimizations:
in addition to basic operation computation between numbers,
it applies algebraic simplification rules (e.g., `x + 0 → x`, `x - x → 0`, `0 * x → 0`)
and recursively processes sub-expressions, allowing a simpler form to be obtained at each step.

```dotty
def optimizedOneStepReduction(expr: Expr): Expr =
  import Expr.*
  expr match
    // Base cases — no changes
    case Num(_) | Var(_) => expr

    // Optimizations for addition
    case Add(Num(0), e)      => e // 0 + x → x
    case Add(e, Num(0))      => e // x + 0 → x
    case Add(Num(x), Num(y)) => Num(x + y)
    case Add(e1, e2) =>
      Add(optimizedOneStepReduction(e1), optimizedOneStepReduction(e2))

    // Optimizations for subtraction
    case Sub(e, Num(0))          => e      // x - 0 → x
    case Sub(e1, e2) if e1 == e2 => Num(0) // x - x → 0
    case Sub(Num(x), Num(y))     => Num(x - y)
    case Sub(e1, e2) =>
      Sub(optimizedOneStepReduction(e1), optimizedOneStepReduction(e2))

    // Optimizations for multiplication
    case Mul(Num(0), _)      => Num(0) // 0 * x → 0
    case Mul(_, Num(0))      => Num(0) // x * 0 → 0
    case Mul(Num(1), e)      => e      // 1 * x → x (additional optimization)
    case Mul(e, Num(1))      => e      // x * 1 → x (additional optimization)
    case Mul(Num(x), Num(y)) => Num(x * y)
    case Mul(e1, e2) =>
      Mul(optimizedOneStepReduction(e1), optimizedOneStepReduction(e2))
  end match
end optimizedOneStepReduction
```

@:@

---

**Links:**

- [Link to repository with exercises][sample]
- [Harrison - Introduction to Functional Programming](https://funprog-ru.github.io/)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)

[sample]: https://gitverse.ru/scalabook/fp
