# Algebraic Data Types

## What Are Algebraic Data Types?

Imagine you have a type constructor that lets you create new types by combining existing ones.
That is exactly what algebraic data types (ADTs) are.

**Simple analogy**:

- You have basic "building blocks" (simple types)
- You can glue them together in different ways (operations)
- You get complex structures (algebraic types)

ADTs come in four main varieties:

## Unit Type (Unit) — "singleton container"

```dotty
// Unit type — it has only one possible value
type UnitType = Unit
val unitValue: UnitType = () // The only value of this type

// Analogy: a box that always contains the same item
// Or a page with a single record
```

**Why is it useful?**

- It signals "absence of meaningful information"
- Like an empty page in a notebook
- In mathematics, it corresponds to the number 1 (one possible variant)

## Void Type (Nothing) — "empty container"

```dotty
// Void type — it has no values
type Void = Nothing

// Analogy: an empty box into which you cannot put anything
// Or a page on which you cannot write at all

// You CANNOT create a value of this type!
val impossible: Void = ... // Error! Type Mismatch Error!
```

**Why is it useful?**

- It signals "impossibility"
- Used for error handling and exceptional situations
- In mathematics, it corresponds to the number 0 (zero possible variants)

## Product Type — "combination of everything"

```dotty
// Product type — it contains values of all constituent types at once
type IntPair = (Int, Int) // Must contain both the first and the second number

val product: IntPair = (42, 100) // Both values are required

// Analogy: a form where you must fill in all fields
// Or a box with two compartments — both must be filled
```

**How it works**:

- The number of possible values = product of variants of each type
- If `Int` has \\(2^{32}\\) values, then `IntPair` has \\(2^{32} \times 2^{32}\\) values
- Like multiplication in mathematics: Int × Int

**Other examples**:

```dotty
case class Person(name: String, age: Int) // Both name and age
type Triple = (Boolean, Int, String) // A boolean, a number, and a string
```

## Sum Type — "choosing one of the variants"

```dotty
// Sum type — it represents a value of one of the possible types
// "Balance can be either positive or negative"
enum Balance:
  case Positive(amount: Int) // Variant 1
  case Negative(amount: Int) // Variant 2

// Usage:
val positive: Balance = Balance.Positive(100) // Chose "positive" variant
val negative: Balance = Balance.Negative(50)  // Chose "negative" variant

// Analogy: a form with "yes/no" options — you can choose only one
// Or a road fork — you can go only down one of the roads
```

**How it works**:

- The number of possible values = sum of variants of each type
- If `Positive` has \\(2^{32}\\) values and `Negative` has \\(2^{32}\\) values,
  then `Balance` has \\(2^{32} + 2^{32}\\) values
- Like addition in mathematics: Int + Int

## Combinations in Practice

**Example 1: Complex data type**

A user can be either a guest or a registered user:

```dotty
enum User:
  // Simple variant (unit type)
  case Guest
  // Product type inside a sum type!
  case RegisteredUser(name: String, email: String, age: Int)
```

**Example 2: Tree (recursive ADT)**

A binary tree can be either empty or a node with two subtrees:

```dotty
enum Tree[+A]:
  case Empty                                         // Base case
  case Node(value: A, left: Tree[A], right: Tree[A]) // Recursive case
```

## Why "Algebraic"?

Types in programming behave like numbers in mathematics! We can:

- **Count** the number of possible values of a type
- **Add** and **multiply** types
- **Prove equalities** between different types

### Law 1: `1 + 0 = 1` (One plus zero equals one)

```dotty
// Mathematics: 1 + 0 = 1
// Types: Unit + Nothing = type with one variant

enum OneOrNothing:
  case OnlyOne
// No other case classes — this is "plus zero"

// Let's check in practice:
val onlyPossibleValue: OneOrNothing = OneOrNothing.OnlyOne

// There are no other values:
// val impossible1: OneOrNothing = ???  // Does not exist!
// val impossible2: OneOrNothing = ???  // Does not exist!

// How many values? Exactly one! Just like Unit
```

**What is happening here**:

- `Unit` (unit type) = 1 possible value
- `Nothing` (void type) = 0 possible values
- `Unit + Nothing` = 1 + 0 = 1 possible value

**Real-life analogy**:

- You have one candy (`OnlyOne`)
- And a promise to give zero more candies (`Nothing`)
- In the end, you still have one candy

> The law generalizes to any type: `A + Nothing = A`

### Law 2: `1 * A = A` (One times A equals A)

```dotty
// Mathematics: 1 × A = A
// Types: (Unit, Int) is equivalent to just Int

type UnitTimesInt = (Unit, Int)  // Unit × Int

// Prove equivalence with transformations:
def toInt(uti: UnitTimesInt): Int = uti._2  // Just take the second value
def fromInt(i: Int): UnitTimesInt = ((), i) // Add the unit value

// Check:
val number: Int = 42
val wrapped: UnitTimesInt = fromInt(number)  // ((), 42)
val unwrapped: Int = toInt(wrapped)          // 42

// number == unwrapped  
// We lost nothing!
```

**Why it works**:

- `Unit` has only one value `()`
- When multiplying `(Unit, Int)` we get pairs: `((), 0)`, `((), 1)`, `((), 2)`...
- But the first part is always `()` — it adds no information!
- In effect, these are the same numbers: 0, 1, 2...

**Real-life analogy**:

- Writing numbers in a notebook: `(notebook, number)`
- But the notebook is always the same — it doesn't change the numbers
- We can remove the notebook and work only with numbers

### Law 3: `A + A = 2 * A` (Sum equals product)

```dotty
// Mathematics: Int + Int = 2 × Int
// Types: TwoInts is equivalent to (Boolean, Int)

// Representation 1: as a sum (addition)
sealed trait TwoInts
object TwoInts:
  case class First(value: Int) extends TwoInts  // Left Int
  case class Second(value: Int) extends TwoInts // Right Int

// Representation 2: as a product (multiplication)
type TwoTimesInt = (Boolean, Int)  // Boolean × Int

// Prove equivalence with transformations:
def fromTwoInts(ti: TwoInts): TwoTimesInt = ti match
  case TwoInts.First(value)  => (true, value)  // true means "from the left"
  case TwoInts.Second(value) => (false, value) // false means "from the right"

def toTwoInts(ti: TwoTimesInt): TwoInts = ti match
  case (true, value)  => TwoInts.First(value)
  case (false, value) => TwoInts.Second(value)

// Check:
val sumStyle: TwoInts = TwoInts.First(100)  // Left number 100
val productStyle: TwoTimesInt = (true, 100) // Same in another form

val convertedToProduct: TwoTimesInt = fromTwoInts(sumStyle) // (true, 100)
val convertedBackToSum: TwoInts = toTwoInts(productStyle)   // First(100)
```

**Let's break it down**:

**TwoInts (sum) has values**:

```
First(0), First(1), First(2), ... First(2³²-1)   // 2³² values
Second(0), Second(1), ... Second(2³²-1)          // another 2³² values
```

**Total**: \\(2^{32} + 2^{32} = 2 \times 2^{32}\\) values

**TwoTimesInt (product) has values**:

```
(true, 0), (true, 1), (true, 2), ... (true, 2³²-1)   // 2³² values
(false, 0), (false, 1), ... (false, 2³²-1)           // another 2³² values
```

**Total**: \\(2 \times 2^{32}\\) values — the same!

**Real-life analogy**:

- **Sum**: you have two boxes of numbers — you can take a number from the left or the right
- **Product**: you have a tag (true/false) and a number — the tag tells you which box the number came from
- These are two different forms of the same information!

### Why This Matters in Practice

#### 1. **Data Representation Optimization**

```dotty
// Either a strict type for global use:
enum Result:
  case Success(data: String)
  case Failure(error: String)

// Or a simplified representation for a local scope:
type OptimizedResult = (Boolean, String) // true=Success, false=Failure
```

#### 2. **Understanding Type Complexity**

```dotty
// How many possible values?
case class User(premium: Boolean, age: Int, country: String)

// Count: 2 (Boolean) × ~100 (approximate age) × 200 (countries) ≈ 40,000 variants
```

#### 3. **Refactoring and Transformations**

You can safely transform between:

- `Either[A, A]` ⇔ `(Boolean, A)`
- `Option[A]` ⇔ `Either[Unit, A]`
- And many other equivalences!

This helps you choose the optimal data representation for each task!

## Recursive Types and Lists

A recursive type is a type that is defined **in terms of itself**.
Like a Russian nesting doll that contains a smaller doll of the same kind inside.

### Lists: Mathematical Formula `μX.(1 + int*X)`

Let's break this formula down:

`μX.(1 + int*X)` means:

- `μX` — "smallest solution to the equation X = ..." (recursion)
- `1` — empty list (the end)
- `int*X` — a list element and the rest of the list
- `+` — either empty, or element + rest

```dotty
// Implement this formula in code:
enum IntList:
  // 1 — empty list (base case of recursion)
  case Nil

  // int * X — element and tail (recursive case)
  case Cons(head: Int, tail: IntList)

object IntList:
  // Convenience constructors
  // Empty list
  def apply(): IntList = Nil
  // List with one element
  def apply(x: Int): IntList = Cons(x, Nil)
```

### Expanding the List Formula

Starting equation: `X = 1 + int × X`

Substitute the self-reference:

```
X = 1 + int × X
  = 1 + int × (1 + int × X)
  = 1 + int + int × int × X
  = 1 + int + int × int × (1 + int × X)
  = 1 + int + int² + int³ × X
  = ... and so on infinitely
```

**What this means in practice**:

`1 (empty) + int (one element) + int² (two elements) + int³ (three elements) + ...`:

- The list can be: empty (`1`)
- Or contain: one element (`int`)
- Or contain: two elements (`int × int`)
- Or contain: three elements (`int × int × int`)
- ... and so forth

### Binary Trees: Formula `μX.(int + X*X)`

Now consider binary trees:

`μX.(int + X*X)` means:

- `μX` — recursive type
- `int` — leaf with a value (the end)
- `X*X` — node with two subtrees
- `+` — either leaf, or node with two subtrees

```dotty
// Binary tree implementation:
enum BinTree:
  // int — leaf with a value
  case Leaf(value: Int)
  // X * X — node with left and right subtrees
  case Branch(left: BinTree, right: BinTree)

import BinTree.*

// Example tree:
//     •
//    / \
//   1   •
//      / \
//     2   3
val example: BinTree =
  Branch(
    Leaf(1),   // Left subtree
    Branch(    // Right subtree
      Leaf(2), //   Left sub-subtree
      Leaf(3)  //   Right sub-subtree
    )
  )
```

### Expanding the Tree Formula

Starting equation: `X = int + X × X`

Substitute the self-reference:

```
X = int + X × X
  = int + (int + X × X) × (int + X × X)
  = int + (int × int) + (int × X × X) + (X × X × int) + (X × X × X × X)
  = ... gets complex very fast!
```

**What this means in practice**:

- The tree can be: a simple leaf (`int`)
- Or: a node with two leaves (`int × int`)
- Or: a node with a leaf and a subtree (`int × (int × int)`)
- Or even more complex structures

### Comparing Lists and Trees

| Aspect             | List                    | Binary Tree               |
|--------------------|-------------------------|---------------------------|
| **Formula**        | `μX.(1 + int×X)`        | `μX.(int + X×X)`          |
| **Structure**      | Linear chain            | Hierarchical              |
| **Recursion**      | One link forward        | Two links (left/right)    |
| **Base case**      | Empty list (`Nil`)      | Leaf (`Leaf`)             |
| **Example**        | `[1]→[2]→[3]→END`       | Tree with branches        |

## Summary

### Main Advantages of ADTs

#### **Type Safety**

**The compiler becomes your ally** — it checks that you have handled all possible cases:

```dotty
enum PaymentMethod:
  case Cash
  case Card(number: String)
  case Crypto(wallet: String)

def processPayment(method: PaymentMethod) = method match
  case PaymentMethod.Cash      => println("Cash accepted")
  case PaymentMethod.Card(num) => println(s"Card: $num")
  // Compiler will warn: "match may not be exhaustive"
  // We forgot the Crypto case!
```

**Result**: It is impossible to accidentally miss an important processing case.

#### **Accurate Modeling**

**Types exactly reflect the domain** — you cannot create an invalid state:

```dotty
// Bad: you can create a user without both name and email simultaneously
case class User(name: Option[String], email: Option[String])

// Good: accurately describe possible states
enum UserState:
  case Guest
  case Registered(name: String, email: String)
```

**Result**: Code clearly expresses business logic.

#### **Easy Extension**

**Add new operations without modifying existing code**:

```dotty
enum Shape:
  case Circle(radius: Double)
  case Rectangle(width: Double, height: Double)

// You can add a new operation:
def toSvg(shape: Shape): String = shape match
  case Shape.Circle(r)       => s"<circle r='$r'/>"
  case Shape.Rectangle(w, h) => s"<rect width='$w' height='$h'/>"
```

**Result**: The system easily adapts to new requirements.

#### **Improved Readability**

Types become self-documenting — just from a function's signature you can tell what it does and what data it accepts.

#### **Composability**

Complex types are built from simple ones, like Lego blocks:

```dotty
type UserAction  = Either[LoginCommand, PurchaseCommand]
type UserSession = (UserId, List[UserAction])
```

#### **Simplified Refactoring**

When changing a data type, the compiler will precisely indicate all places that need updating.

### Philosophical Aspect

ADTs change the approach to programming:

- **Instead of**: "Write code and hope it works"
- **We get**: "Design types, and the code basically writes itself"

### Conclusion

Algebraic data types are not just a technical trick, but a **way of thinking** that allows:

- ✅ **Preventing errors** at compile time
- ✅ **Clearly expressing** intentions in code
- ✅ **Building reliable** systems that are hard to break
- ✅ **Developing applications** quickly without fear of breaking something

> Well-designed types ≈ fewer bugs ≈ more time for implementing features! 

ADTs turn the compiler from a "supervisor looking for errors" into an "assistant preventing errors" — and that changes everything!

---

**Links:**

- [Link to repository with exercises][sample]
- [Algebraic Data Types and Their Use in Programming - Roman Dushkin](https://fprog.ru/2009/issue2/roman-dushkin-algebraic-data-types/)
- [Algebraic data type - wikipedia](https://wiki.haskell.org/Algebraic_data_type)
- [Algebraic Data Types](https://web.archive.org/web/20151003164111/https://www.fpcomplete.com/school/starting-with-haskell/introduction-to-haskell/2-algebraic-data-types)
- [Church-encoded payment types by Mark Seemann](https://blog.ploeh.dk/2018/06/18/church-encoded-payment-types/)
- [Sum Types, Visitors, and the Expression Problem](https://koerbitz.me/posts/Sum-Types-Visitors-and-the-Expression-Problem.html)
- [The Algebra of Data, and the Calculus of Mutation - Kalani Thielen](http://static.v25media.com/the-algebra-of-data-and-the-calculus-of-mutation.html)
- [The Expression Problem and its solutions](https://eli.thegreenplace.net/2016/the-expression-problem-and-its-solutions/)
- [Visitor as a sum type by Mark Seemann](https://blog.ploeh.dk/2018/06/25/visitor-as-a-sum-type/)

[sample]: https://gitverse.ru/scalabook/fp
