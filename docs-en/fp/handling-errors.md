# Error Handling

## Fundamental Principles of Functional Error Handling and Reporting

In functional programming, throwing exceptions is considered a side effect,
which contradicts the principles of pure functional programming. 
Instead of using the exception mechanism, the functional approach proposes:

1. Representing errors and failures as ordinary return values
2. Using higher-order functions to abstract common error handling patterns
3. Implementing error recovery mechanisms through function composition

This solution offers several key advantages:

- **Safety**: explicit error representation makes error handling mandatory
- **Referential Transparency**: preserving the property of expression substitutability
- **Logic consolidation**: ability to centrally handle errors, similar to try/catch blocks

## Overview of the Traditional Exception-Based Approach

The main problem with exceptions is that they **violate the principles of functional programming**, 
such as purity, transparency, and type safety.

### 1. Violation of Referential Transparency

**Referential transparency** means that an expression can be replaced with its result without changing the program's behavior. 
Exceptions violate this rule.

- **Context dependence:** The result of a function depends not only on its arguments, 
  but also on the state of the system (e.g., whether a file exists, whether the network is available).

- **Example:**
  ```dotty
  // IMPURE function
  def readConfigFile(configPath: String): String =
      val source = scala.io.Source.fromFile(configPath)
      try
        source.mkString
      finally
        source.close()
  ```

  Calling `readConfigFile("app.conf")`:
    - If the file exists, it will return its contents (e.g., `"port=8080"`).
    - If the file doesn't exist, it will throw `FileNotFoundException`.

  **Problem:** We cannot mentally replace the function call `readConfigFile("app.conf")` with its result 
  without knowing the context (whether the file exists or not). This breaks the ability to reason about code using simple **substitution model**.

- **Confusing control flow:** Execution flow happens not through combination of expressions, 
  but through "jumps" (`throw` -> `catch`) to different parts of the code, which is harder to analyze.

### 2. Lack of Type Safety

Type safety is the guarantee that the compiler will check the correctness of the program at compile time. 
Exceptions bypass this type system.

- **Hidden contracts:** A function's signature is its contract. Exceptions hide a crucial part of this contract — potential errors.

  **Example:**

  ```dotty
  // Function contract: Int => Int
  // Actual contract: Int => Int OR throws Exception
  def failingFn(n: Int): Int =
      if n < 0 then throw new Exception("Negative number!")
      else n * 2
  ```

- **The compiler won't help:** The compiler _does not_ require the caller to handle the exception. The error will only manifest at **Runtime**.

  **Example of dangerous code that will compile:**

  ```dotty
  val result: Int = failingFn(-5) // Compiler is happy. Types match.
  val finalResult = result + 10   // This line will never execute for n=-5
  ```

  **Execution result:** The program will crash with `Exception: Negative number!`, and the compiler didn't warn us about this possibility.

- **Errors remain unnoticed:** If a developer forgot that `failingFn` can throw an exception, 
  or didn't check its documentation/source code, the error will only be detected during testing or by the end user.

### Key Takeaway:

Exceptions are useful for **critical, unrecoverable errors** (e.g., out of memory), 
which are handled at the highest level of abstraction.

However, for **expected domain errors** (invalid user input, missing data, network unavailability), 
they are a poor tool, as they make code unreliable and unpredictable. 

## Alternative Approaches to Error Handling

Let's consider a practical example — implementing a function to compute the average value, which is undefined for an empty list:

```dotty
def mean(xs: Seq[Double]): Double =
  if xs.isEmpty then
    throw new ArithmeticException("mean of empty list!")
  else xs.sum / xs.length
```

This function is a partial function — it is not defined for all possible inputs 
(in this case, not for empty lists).

### Alternative 1: Return Special Values

The first approach involves returning special values:

- For an empty list, return `Double.NaN`
- In other contexts, `null` or other sentinel values might be used

**Problems with this approach:**

1. **Implicit error propagation:**
    - Calling code may forget to check the special value and won't be warned by the compiler, 
      which can cause subsequent code to misbehave.
    - Errors are discovered significantly later than where they occur

2. **Increased boilerplate:**
    - Requires many explicit checks of returned values by the caller with explicit `if` operators 
      to verify whether the caller received a "real" result
    - Especially problematic with chains of function calls, each using error codes
      that must be checked and somehow aggregated

3. **Limitations of polymorphic code:**
    - Not all types have suitable sentinel values
    - Example: function `max[A](xs: Seq[A])(greater: (A,A) => Boolean): A` cannot return a valid value 
      for an empty list. Here, `null` cannot be used either, since `null` is only allowed for reference types,
      and `A` might actually be a primitive like `Double` or `Int`.

4. **Violation of uniformity:**
    - Requires special conventions between functions: proper use of `mean` will require
      callers to do something other than call `mean` and use the result
    - Complicates use with higher-order functions: providing functions with special policies 
      makes it difficult to pass them to higher-order functions that must handle all arguments uniformly.

### Alternative 2: Undefined Case Handler Parameter

The second approach involves passing a default value — forcing the caller to provide an argument
that specifies what to do when we don't know how to handle the input:

```dotty
def mean(xs: Seq[Double], onEmpty: Double): Double =
  if xs.isEmpty then onEmpty
  else xs.sum / xs.length
```

**Limitations of this solution:**

1. **Tight coupling** to a specific type of error handling
2. **Insufficient flexibility:**
    - Unable to abort computation on error. What if the mean is called as part of a larger computation,
      and we'd like to abort that computation if the mean is undefined?
    - No mechanism for selecting alternative computation branches
3. **Early decision-making** about error handling, which may not be optimal for the calling code

We need a more flexible mechanism that defers the decision about error handling to the appropriate level of abstraction.

## The Option Data Type: Safe Representation of Missing Values

### Concept and Basic Definition

The functional approach to handling missing or undefined values consists of 
explicitly indicating the possibility of no result through the return type. 
This allows shifting the responsibility for handling such cases to the calling code, 
while maintaining type safety and referential transparency.

The `Option` type is an algebraic data type (ADT) defined as follows:

```dotty
enum Option[+A]:
  case Some(get: A)  // Represents the presence of a value of type A
  case None          // Represents the absence of a value
```

Key features:

- Covariance (`+A`) allows natural work with type hierarchies
- Two variants: `Some` for existing values and `None` for missing values
- Explicit handling model instead of implicit exceptions

### Practical Application of Option

Let's consider the refactored example of the average function:

```dotty
import Option.{Some, None}

def mean(xs: Seq[Double]): Option[Double] =
  if xs.isEmpty then 
    None  // Explicit indication of no result
  else 
    Some(xs.sum / xs.length)  // Wrapping the result in Some
```

Advantages of this approach:

1. **Function totalness**: the function is defined for all inputs
2. **Signature clarity**: the return type explicitly indicates the possible absence of a result
3. **Safety**: the compiler requires handling both cases (`Some`/`None`)

### Basic Operations on Option

#### 1. Value Transformation

`map` and `flatMap` allow sequential work with possible values:

```dotty
def map[B](f: A => B): Option[B] = this match
  case Some(a) => Some(f(a))
  case None    => None

def flatMap[B](f: A => Option[B]): Option[B] = this match
  case Some(a) => f(a)
  case None    => None
```

The `map` function can be used to transform the result inside the parameter, if it exists.
You can think of this as continuing computation under the assumption that no error occurred;
it's also a way to defer error handling to later code:

`flatMap` is similar, except that
the function provided for transforming the result can itself fail.

Example usage with an employee hierarchy:

```dotty
case class Employee(
  name: String,
  department: String,
  manager: Option[Employee]  // Recursive use of Option
)

def lookupByName(name: String): Option[Employee] =
  name match
    case "Jack" => Some(Employee("Jack", "Engineering", None))
    case "Jill" => Some(Employee("Jill", "HR", lookupByName("Jack")))
    case _      => None

// Example computation chains
lookupByName("Jill").map(_.department)  // Transform an existing value
// Some("HR")

lookupByName("Jill").flatMap(_.manager) // Unwrap nested Option
// Some(Employee("Jack", "Engineering", None))
```

#### 2. Filtering and Handling Missing Values

`filter` can be used to turn successes into failures
if successful values don't match a given predicate.

`getOrElse` is used to convert from `Option[A]` to `A`,
providing a default value in case of error.

`orElse` is similar to `getOrElse`, except it returns another `Option` if the first is undefined.
This is often useful when wanting to chain potentially failing computations,
trying the second if the first failed.

```dotty
def filter(p: A => Boolean): Option[A] = this match
  case Some(a) if p(a) => Some(a)
  case _               => None

def getOrElse[B >: A](default: => B): B = this match
  case Some(a) => a
  case None    => default

def orElse[B >: A](ob: => Option[B]): Option[B] = this match
  case Some(a) => Some(a)
  case None    => ob
```

Practical examples:

```dotty
val dept = lookupByName("Jill")
  .map(_.department)
  .filter(_ != "Sales")  // Turn Some into None if condition isn't met
  .getOrElse("Default Department")

// Alternative Option on failure
lookupByName("Unknown")
  .orElse(lookupByName("Default Employee"))
```

#### 3. Composition of Operations

A typical pattern for working with `Option` is a chain of transformations with final handling:

```dotty
// Getting the manager's name with handling all possible absences
val managerName = lookupByName("Jill")
  .flatMap(_.manager)
  .map(_.name)
  .getOrElse("No manager found")
// val managerName: String = Jack  
```

### Usage Recommendations

1. **Advantages over exceptions:**
    - Preserving referential transparency
    - Explicit indication of possible errors in types
    - Composability and deferred handling

2. **When to use:**
    - For expected and handleable "errors" (missing data, invalid input parameters)
    - In public APIs for explicit indication of possible issues

3. **When not to use:**
    - For truly exceptional situations (I/O errors, fatal errors)
    - When absence of a value is a programmer error (better to use an exception)

4. **Idiomatic patterns:**
    ```dotty
    // Converting None to exception
    val result = optionValue.getOrElse(throw new IllegalStateException("Missing required value"))
  
    // Cascading alternative checks
    val config = readFileConfig()
      .orElse(readEnvConfig())
      .orElse(readDefaultConfig())
    ```

### Comparison with Similar Solutions

For more complex scenarios where you need to preserve error information, consider using `Either` or `Try`. 
`Option` is best suited for simple cases of missing values where additional error information is not required.

### Exercise 3.1.1: Safe Division with Option

> [Repository with exercises and instructions for completion][sample]

Implement a safe division function 
that uses the `Option` type to explicitly represent the possibility of a divide-by-zero error. 
This exercise demonstrates the basic principle of functional error handling - 
converting a partial function to a total one.

```dotty
def safeDivide(a: Double, b: Double): Option[Double] = ???
```

**Examples:**

```dotty
// Successful division
safeDivide(10, 2)   // Some(5.0)
safeDivide(5, 2)    // Some(2.5)

// Division by zero
safeDivide(8, 0)    // None
safeDivide(-3, 0)   // None

// Edge cases
safeDivide(0, 5)    // Some(0.0)
safeDivide(0, 0)    // None (undefined)
```

@:note(🔍 Show Solution)

The `safeDivide` function performs safe division of numbers, returning the result in `Some` when the divisor is non-zero 
and `None` when dividing by zero, which explicitly indicates an error without using exceptions. 
The alternative version `safeDivideAlt` does the same via pattern matching, 
checking the zero-divisor case as a separate pattern — 
both variants demonstrate the functional approach to error handling through the `Option` type, 
preserving code purity and type safety.

```dotty
def safeDivide(a: Double, b: Double): Option[Double] =
  if b != 0 then Some(a / b) else None

// Alternative implementation via pattern matching
def safeDivideAlt(a: Double, b: Double): Option[Double] = 
  b match
    case 0 => None
    case _ => Some(a / b)
```

@:@

### Exercise 3.1.2: Lookup Shipping Address by Dependency Chain

In this exercise, we work with a chain of dependent data where information is scattered across different entities. 
The task is to find a shipping address for an order, using `flatMap` for safe work with potentially missing values.

```dotty
case class User(id: Int, name: String, address: Option[String])
case class Order(orderId: Int, userId: Int, product: String, price: Double)

val users = List(
  User(1, "Alice", Some("123 Main St")),
  User(2, "Bob", None)
)

val orders = List(
  Order(100, 1, "Laptop", 999.99),
  Order(101, 2, "Phone", 599.99)
)

// Write a function that returns the shipping address by order id (use flatMap)
def getShippingAddress(orderId: Int): Option[String] = ???
```

**Examples:**

```dotty
// Order with existing user and address
getShippingAddress(100)  // Some("123 Main St")

// Order with existing user but no address
getShippingAddress(101)  // None

// Non-existent order
getShippingAddress(999)  // None
```

@:note(🔍 Show Solution)

The `getShippingAddress` function safely extracts the shipping address by order ID, 
using a chain of `Option` operations: first finding the order via `find`, 
then using `flatMap` to find the corresponding user and their address. 
If any step returns `None` (order not found, user doesn't exist, or address not specified), 
the entire chain automatically returns `None`, 
ensuring strict handling of missing data without exceptions and explicit checks. 
The solution demonstrates idiomatic work with nested `Option` through `flatMap` composition.

```dotty
def getShippingAddress(orderId: Int): Option[String] =
  orders
    // Find the order
    .find(_.orderId == orderId)  
    // Unwrap Option[Order]      
    .flatMap(order =>                  
      users
        // Find the user
        .find(_.id == order.userId) 
        // Get the address
        .flatMap(_.address)            
    )
```

@:@

### Exercise 3.1.3: Working with Optional Profile Fields: getEmailOrDefault

Write a function that returns the profile's `email` or `"no-email@example.com"` if `email` is missing.

```dotty
case class Profile(id: Int, name: String, email: Option[String], age: Option[Int])

val profiles = List(
  Profile(1, "Alice", Some("alice@example.com"), Some(30)),
  Profile(2, "Bob", None, Some(25)),
  Profile(3, "Charlie", Some("charlie@example.com"), None)
)

def getEmailOrDefault(profile: Profile): String = ???
```

**Examples:**

```dotty
getEmailOrDefault(profiles(0)) // "alice@example.com"
getEmailOrDefault(profiles(1)) // "no-email@example.com"
```

@:note(🔍 Show Solution)

The function returns the user's email or the default value `"no-email@example.com"` 
if email is missing, using the `getOrElse` method for safe `Option` handling.
This ensures the result is always a string, eliminating the need for subsequent `None` checks.

```dotty
def getEmailOrDefault(profile: Profile): String =
  profile.email.getOrElse("no-email@example.com")
```

@:@

### Exercise 3.1.4: Working with Optional Profile Fields: averageAge

Implement a function that computes the average age of profiles (considering only those with a specified age).

```dotty
case class Profile(id: Int, name: String, email: Option[String], age: Option[Int])

val profiles = List(
  Profile(1, "Alice", Some("alice@example.com"), Some(30)),
  Profile(2, "Bob", None, Some(25)),
  Profile(3, "Charlie", Some("charlie@example.com"), None)
)

def averageAge(profiles: List[Profile]): Option[Double] = ???
```

**Examples:**

```dotty
averageAge(profiles)          // Some(27.5) = (30 + 25) / 2
averageAge(List(Profile(4, "Diana", None, None))) // None
```

@:note(🔍 Show Solution)

The function computes the average age, first extracting all specified ages via `flatMap`,
and returns `Some` with the result if there is data, or `None` if no profile has an age specified.
The solution correctly handles empty collections while preserving type safety.

```dotty
def averageAge(profiles: List[Profile]): Option[Double] =
  val ages = profiles.flatMap(_.age)
  if ages.isEmpty then None
  else Some(ages.sum.toDouble / ages.length)
```

@:@

### Exercise 3.1.5: Working with Optional Profile Fields: nameEmailPairs

Write a function that returns a list of (name, email) pairs only for profiles with an email.

```dotty
case class Profile(id: Int, name: String, email: Option[String], age: Option[Int])

val profiles = List(
  Profile(1, "Alice", Some("alice@example.com"), Some(30)),
  Profile(2, "Bob", None, Some(25)),
  Profile(3, "Charlie", Some("charlie@example.com"), None)
)

def nameEmailPairs(profiles: List[Profile]): List[(String, String)] = ???
```

**Examples:**

```dotty
nameEmailPairs(profiles) 
// List(("Alice", "alice@example.com"), ("Charlie", "charlie@example.com"))
```

@:note(🔍 Show Solution)
  
The function builds a list of "name-email" pairs only for profiles with an email specified,
using a combination of `flatMap` for filtering and `map` for data transformation.
This effectively discards profiles without email and creates a result list with only valid data.

```dotty
def nameEmailPairs(profiles: List[Profile]): List[(String, String)] =
  profiles.flatMap: p =>
    p.email.map(email => (p.name, email))
```

@:@


## The Either Data Type: Extended Error Handling in Functional Programming

### Introduction to Value-Based Error Handling

In functional programming, there is a fundamental principle: 
all effects, including errors and exceptions, should be explicitly represented in return types. 
This approach provides several key advantages:

1. **Transparency** — the function signature explicitly indicates possible errors
2. **Safety** — the compiler requires handling all possible cases
3. **Composability** — errors can be handled and passed through call chains

### Limitations of the Option Type

While `Option` is a simple and useful tool for representing missing values, it has significant limitations:

```dotty
def findUser(id: Int): Option[User] = ...
```

In this example, when the function returns `None`, we cannot determine:

- Whether a user with that ID exists
- Whether an error occurred while accessing the database
- Whether there were network connectivity issues

`Option` simply reports that the value is absent, but doesn't explain the reason.

### The Either Type: Concept and Definition

`Either` is an algebraic data type that extends the capabilities of `Option`, 
allowing storage of error information. Its definition is:

```dotty
enum Either[+E, +A]:
  case Left(value: E)  // For storing error information
  case Right(value: A) // For storing successful results
```

Key features:

- Covariance on both type parameters (`+E`, `+A`)
- Two constructors: `Left` and `Right`
- By convention: `Right` means "correct" (successful) result

### Practical Application of Either

#### Example 1: Computing Average Value

```dotty
def mean(xs: Seq[Double]): Either[String, Double] =
  if xs.isEmpty then
    Left("Error: cannot compute average of an empty list")
  else
    Right(xs.sum / xs.length)

// Usage examples:
mean(Seq(1.0, 2.0, 3.0)) 
// Right(2.0)
mean(Seq.empty)           
// Left("Error: cannot compute average of an empty list")
```

#### Example 2: Safe Execution of Code with Exceptions

```dotty
import scala.util.control.NonFatal

def safeDivide(a: Int, b: Int): Either[Throwable, Int] =
  try Right(a / b)
  catch case NonFatal(ex) => Left(ex)

// Examples:
safeDivide(10, 2)  // Right(5)
safeDivide(10, 0)  // Left(java.lang.ArithmeticException: / by zero)
```

### Comparison with Option

| Feature            | Option              | Either                         |
|--------------------|---------------------|--------------------------------|
| Error storage      | No (only `None`)    | Yes (arbitrary type in `Left`) |
| Signature          | `Option[A]`         | `Either[E, A]`                 |
| Successful result  | `Some(value)`       | `Right(value)`                 |
| Error              | `None`              | `Left(error)`                  |
| Informative        | Low                 | High                           |

### Extended Usage Examples

#### Handling Multiple Errors

```dotty
case class ValidationError(message: String)

def validateAge(age: Int): Either[ValidationError, Int] =
  if age < 0 then Left(ValidationError("Age cannot be negative"))
  else if age > 120 then Left(ValidationError("Age is too high"))
  else Right(age)

def createUser(name: String, age: Int): Either[ValidationError, User] =
  for
    validAge <- validateAge(age)
    // Other validations...
  yield User(name, validAge)
```

#### Composing Either

```dotty
def parseNumber(s: String): Either[String, Int] =
  try Right(s.toInt)
  catch case _: NumberFormatException => Left(s"Cannot convert '$s' to a number")

def calculate(a: String, b: String): Either[String, Int] =
  for
    num1 <- parseNumber(a)
    num2 <- parseNumber(b)
    result <- safeDivide(num1, num2).left.map(_ => "Division by zero")
  yield result

// Examples:
calculate("10", "2")  // Right(5)
calculate("10", "0")  // Left("Division by zero")
calculate("a", "2")   // Left("Cannot convert 'a' to a number")
```

### Usage Recommendations

1. **When to use Either:**
    - When it's important to convey error information
    - For validating input data
    - When working with external systems (databases, APIs)

2. **When to use Option:**
    - When the reason for the missing value is not important
    - For simple "value present or not" cases

3. **Best Practices:**
    - Use meaningful error types (not just `String`)
    - Create error hierarchies for complex scenarios
    - Document possible errors in ScalaDoc

### Exercise 3.2.1: Safe Computation of Square Root

Implement a square root computation function that explicitly handles the case of negative numbers, 
returning an informative error message instead of throwing an exception. 
The function should preserve referential transparency and explicitly indicate the possible error in its return type.

```dotty
def safeSqrt(x: Double): Either[String, Double] = ???
```

**Examples:**

```dotty
safeSqrt(16.0)   // Right(4.0)
safeSqrt(0.0)    // Right(0.0)
safeSqrt(-9.0)   // Left("Negative number")
safeSqrt(2.25)   // Right(1.5)
```

@:note(🔍 Show Solution)

The `safeSqrt` function implements safe computation of square root, 
returning `Right` with the result for non-negative numbers 
and `Left` with message "Negative number" for negative inputs. 
This demonstrates a typical functional approach to error handling through the `Either` type, where:

- Successful results are wrapped in `Right`
- Errors with descriptions are returned in `Left`
- Condition checking is done explicitly before computation
- Referential transparency is preserved (same inputs always yield same results)
- Exceptions are replaced with explicit return of error values

```dotty
def safeSqrt(x: Double): Either[String, Double] =
  if x < 0 then 
    Left("Negative number")
  else 
    Right(Math.sqrt(x))
```

@:@

### Exercise 3.2.2: Age Validation with Either

Implement a parsing and validation function for age from a string that handles all possible errors: 
incorrect number format and out-of-range values (from 1 to 120). 
The function should return understandable error messages in `Left` and a valid age in `Right`.

```dotty  
def parseAge(s: String): Either[String, Int] = ???  
```

**Examples:**

```dotty
parseAge("25")      // Right(25)
parseAge("150")     // Left("Age must be between 1 and 120")
parseAge("abc")     // Left("Invalid age format")
parseAge("0")       // Left("Age must be between 1 and 120")
parseAge("")        // Left("Invalid age format")
```

@:note(🔍 Show Solution)

The `parseAge` function implements safe parsing and validation of age from a string,
using `Either` to handle possible errors. 
It first tries to convert the string to a number via `toIntOption`, 
returning a format error on failure, then checks the age range (1-120), 
returning the appropriate error if out of bounds, 
and only upon passing all checks returns the valid age in `Right`. 
The solution demonstrates cascading condition checking with explicit error returns, typical of the functional approach.

```dotty
def parseAge(s: String): Either[String, Int] =
  s.toIntOption match
    case None => Left("Invalid age format")
    case Some(age) if age < 1 || age > 120 =>
      Left("Age must be between 1 and 120")
    case Some(age) => Right(age)
```

@:@

### Exercise 3.2.3: User Validation with Either: createUser

Implement two versions of the `createUser` function with different error-handling strategies: 
the first returns the first error found, the second collects all validation errors. 
The exercise demonstrates `Either` composition and different approaches to validating complex data.

Implement the `createUser` function that:

- Accepts a name (non-empty) and age (string)
- Uses `parseAge` from the previous exercise
- Returns `Right(User)` if all checks pass
- Returns `Left` with a description of the first error  

```dotty  
case class User(name: String, age: Int)  
  
def createUser(name: String, ageStr: String): Either[String, User] = ???   
```  

**Examples:**

```dotty
createUser("Alice", "30") // Right(User("Alice", 30))
createUser("", "25")      // Left("Name cannot be empty")
createUser("Bob", "abc")  // Left("Invalid age format")
createUser("", "abc")     // Left("Name cannot be empty")
```

@:note(🔍 Show Solution)

The `createUser` function implements basic user validation, checking that the name is non-empty 
and using `parseAge` to validate the age, returning the first found error in `Left`. 

```dotty
def createUser(name: String, ageStr: String): Either[String, User] =
  if name.isEmpty then
    Left("Name cannot be empty")
  else
    parseAge(ageStr).map(age => User(name, age))
```

@:@

### Exercise 3.2.4: User Validation with Either: createUserDetailed

An improved version of `createUser`:

- Collects ALL validation errors (name and age)
- Uses `Either` with error type `List[String]`

```dotty  
case class User(name: String, age: Int)  
  
def createUserDetailed(name: String, ageStr: String): Either[List[String], User] = ???  
```  

**Examples:**

```dotty
createUserDetailed("Alice", "30")
// Right(User("Alice", 30))
createUserDetailed("", "abc")
// Left(List("Name cannot be empty", "Invalid age format"))
createUserDetailed("", "150")
// Left(List("Name cannot be empty", "Age must be between 1 and 120"))
```

@:note(🔍 Show Solution)

The enhanced `createUserDetailed` applies a more advanced validation strategy:
using `Either.cond` to check the name and combining with the result of `parseAge`, collecting all possible errors into a list.
The solution demonstrates two approaches to error handling — "fail-fast" (first error only)
and "full validation" (all errors), using pattern matching for `Either` composition.
Both functions return either a valid user in `Right`
or a list of errors in `Left`, preserving type safety.

```dotty
def createUserDetailed(
    name: String,
    ageStr: String
): Either[List[String], User] =
  val nameValid =
    Either.cond(name.nonEmpty, name, List("Name cannot be empty"))
  val ageValid = parseAge(ageStr).left.map(_ :: Nil)

  (nameValid, ageValid) match
    case (Right(n), Right(a))     => Right(User(n, a))
    case (Left(nErr), Left(aErr)) => Left(nErr ++ aErr)
    case (Left(err), _)           => Left(err)
    case (_, Left(err))           => Left(err)
```

@:@

## Ior: Inclusive Error Handling and Warnings

### Introduction to Inclusive Logical Relations

In functional programming, there is often a need to pass not only successful results or errors, 
but also **warnings** — important information that isn't critical but may be useful to calling code. 
The standard `Either` type (exclusive OR) doesn't allow returning both a result 
and additional error information simultaneously — it represents only **one of two variants**.

The `Ior` type (short for "inclusive-or") solves this problem, 
allowing storage of **either** a value of type `A`, **or** a value of type `B`, **or both values simultaneously**.

### The Ior Type: Concept and Definition

`Ior` is an algebraic data type that represents an inclusive relation between two types:

```dotty
enum Ior[+A, +B]:
  case Left(value: A)          // Only a value of type A
  case Right(value: B)         // Only a value of type B
  case Both(left: A, right: B) // Both A and B simultaneously
```

Key features:

- Three constructors: `Left`, `Right`, and `Both`
- Covariance on both type parameters (`+A`, `+B`)
- **Right-biased**: `map` and `flatMap` operations work on the right side (type `B`)
- Analogy with `Either`, but with the additional ability to store both values

| Feature         | Either                           | Ior                                              |
|-----------------|----------------------------------|--------------------------------------------------|
| Variants        | `Left(error)` or `Right(value)`  | `Left(a)`, `Right(b)`, or `Both(a, b)`           |
| Semantics       | Exclusive OR (either A or B)     | Inclusive OR (either A, either B, or both)       |
| Error storage   | Error only                       | Error or warning + result                        |
| Informative     | Limited                          | Extended (ability to pass additional data)       |

### Practical Application of Ior

In the future, we will use the Ior implementation from the [cats-effect](../cats-effect/index.md) library.

#### Creating Ior Values

```dotty
import cats.data.*

// Creation via factory methods:
val right = Ior.right[String, Int](3)
// Right(3) - successful result
val left = Ior.left[String, Int]("Error")
// Left("Error") - error only
val both = Ior.both("Warning", 3)
// Both("Warning", 3) - warning + result

// Creation via syntax (requires import):
import cats.syntax.all.*

val rightSyntax = 3.rightIor      // Ior.right(3)
val leftSyntax  = "Error".leftIor // Ior.left("Error")
```

#### Example 1: Validation with Accumulation of Warnings

A classic example of using `Ior` — data validation, 
where we want to accumulate **warnings** (non-critical notes), 
but stop only on **critical errors**:

```dotty
import cats.syntax.all.*
import cats.data.{NonEmptyChain as Nec, Ior}

// Type for accumulating errors/warnings
type Failures = Nec[String]

case class Username(value: String) extends AnyVal
case class Password(value: String) extends AnyVal
case class User(name: Username, pw: Password)

def validateUsername(u: String): Failures Ior Username =
  if u.isEmpty then
    // Critical error - Only Left
    Nec.one("Username cannot be empty").leftIor
  else if u.contains(".") then
    // Warning + successful result
    Ior.both(Nec.one("Using dots in usernames is deprecated"), Username(u))
  else
    // Success without warnings
    Username(u).rightIor

def validatePassword(p: String): Failures Ior Password =
  if p.length < 8 then
    // Critical error
    Nec.one("Password is too short").leftIor
  else if p.length < 10 then
    // Warning + successful result
    Ior.both(Nec.one("Password should be longer"), Password(p))
  else
    Password(p).rightIor

def validateUser(name: String, password: String): Failures Ior User =
  (validateUsername(name), validatePassword(password)).mapN(User.apply)
```

#### Example 2: Processing Validation Results

```dotty
validateUser("John", "password12")
// Right(User(Username("John"), Password("password12"))) - success without warnings

validateUser("john.doe", "password")
// Both(
//   Chain("Using dots in usernames is deprecated", "Password should be longer"),
//   User(Username(john.doe),Password(password))
// )

validateUser("jane", "short")
// Left("Error: password is too short")
```

#### Extracting Values

To extract data from `Ior`, use the `fold` method, which accepts three functions — one for each case:

```dotty
validateUser("john.doe", "password").fold(
  // Errors only
  errorNec => s"Error: ${errorNec.head}",
  // Success only
  user => s"Success: $user",
  // Warnings + success
  (warnings, user) =>
    s"Warning: ${user.name.value}; The following warnings were found: ${warnings.head}"
)
// "Warning: john.doe; The following warnings were found: Using dots in usernames is deprecated"

validateUser("John", "password12").fold(
  errorNec => s"Error: ${errorNec.head}",
  user => s"Success: $user",
  (warnings, user) =>
    s"Warning: ${user.name.value}; The following warnings were found: ${warnings.head}"
)
// "Success: User(Username(John),Password(password12))"
```

### Usage with NonEmptyChain

A specialized alias with `NonEmptyChain` exists for `Ior` for accumulating errors on the left side:

```dotty
import cats.data.NonEmptyChain

type IorNec[B, A] = Ior[NonEmptyChain[B], A]
```

This allows convenient accumulation of multiple errors or warnings:

```dotty
import cats.syntax.all.*
import cats.data.{ IorNec, NonEmptyChain}

val left: IorNec[String, Int] = Ior.fromEither("Error".leftNec[Int])
// Ior.left(NonEmptyChain("Error"))
```

### Transformations

`Ior` supports transformation into other data types. Important: **all transformations discard the left value if Both is present**:

```dotty
// Both turns into Either.Right, losing the warning
Ior.both("Warning", 42).toEither
// Right(42)

// Right turns into Either.Right
Ior.right[String, Int](42).toEither
// Right(42)

// Left turns into Either.Left
Ior.left[String, Int]("Error").toEither
// Left("Error")
```

### Usage Recommendations

#### 1. When to Use Ior

- **Validation with warnings**: when you need to return both a result and non-fatal notes
- **Systems with error accumulation**: when errors accumulate but don't stop computation
- **Logging**: when you need to return results along with diagnostic information
- **APIs with warnings**: when an API specification allows returning warnings

#### 2. When to Use Either

- **Critical errors**: when the error stops further computations
- **Exceptional situations**: when you need only an error or only a result
- **Simple error handling**: when warning information is not required

### Comparison with Option and Either

| Feature                                   | Option                 | Either             | Ior                          |
|-------------------------------------------|------------------------|--------------------|------------------------------|
| Variants                                  | `Some`/`None`          | `Left`/`Right`     | `Left`/`Right`/`Both`        |
| Error information                         | No                     | Yes                | Yes (including warnings)     |
| Simultaneous result and warning           | No                     | No                 | Yes (`Both`)                 |
| Semantics                                 | Present/Absent         | Either A or B      | Either A, Either B, or Both  |
| Application                               | Missing value          | Error              | Error + Warning              |
| Error accumulation                        | No                     | No                 | Yes (with `Semigroup`)       |

### Conclusion

`Ior` is a powerful tool for scenarios where plain `Either` isn't enough. 
It allows explicit passing of both errors and warnings, while still enabling work with successful results. 
This makes it the ideal choice for:

- APIs returning warnings alongside data
- Validation with partial success
- Systems where warnings shouldn't stop execution

## Validated: Error Accumulation During Validation

### Introduction to the Error Accumulation Problem

Imagine filling out a web form to register an account. You enter a username and password and submit the form. 
The response says the username cannot contain dashes. You fix it and submit again. 
Now the message says no special characters are allowed. You fix it again and submit. 
The password must contain at least one uppercase letter. You fix it and submit. The password must contain at least one digit.

It would be much more convenient to receive **all errors at once**. 
Checking the username for dashes can be done independently of checking for special characters, 
just as checking a password against various requirements. 
An incorrectly filled field in a config can be checked separately from another field.

This is exactly what the `Validated` type is for.

### The Validated Type: Concept and Definition

`Validated` is an algebraic data type representing a computation result 
that can be either successful (`Valid`) or contain an error (`Invalid`). 
Unlike `Either`, `Validated` is designed for **error accumulation** via `Applicative` and **is not a monad**.

#### Type Structure

```dotty
sealed abstract class Validated[+E, +A] extends Product with Serializable

final case class Valid[+A](a: A)   extends Validated[Nothing, A]
final case class Invalid[+E](e: E) extends Validated[E, Nothing]
```

#### Comparison with Either

| Feature      | Either                     | Validated                        |
|--------------|----------------------------|----------------------------------|
| Semantics    | First error (fail-fast)    | All errors (error-accumulation)  |
| Monad        | Yes (has `flatMap`)        | No (only `Applicative`)          |
| Composition  | For-comprehension          | `mapN`, `tupleN`                 |
| Error type   | Single error               | Set of errors (with `Semigroup`) |
| Application  | Sequential validation      | Parallel validation              |

### First Approach: Fail-fast with Either

Let's consider an example of validating a registration form using `Either`. Our data model:

```dotty
final case class RegistrationData(
    username: String,
    password: String,
    firstName: String,
    lastName: String,
    age: Int
)
```

Error model:

```dotty
sealed trait DomainValidation:
  def errorMessage: String

case object UsernameHasSpecialCharacters extends DomainValidation:
  def errorMessage: String =
    "Username cannot contain special characters."

case object PasswordDoesNotMeetCriteria extends DomainValidation:
  def errorMessage: String =
    "Password must contain at least 10 characters, including uppercase and lowercase letters, a digit, and a special character."

case object FirstNameHasSpecialCharacters extends DomainValidation:
  def errorMessage: String =
    "First name cannot contain spaces, digits, or special characters."

case object LastNameHasSpecialCharacters extends DomainValidation:
  def errorMessage: String =
    "Last name cannot contain spaces, digits, or special characters."

case object AgeIsInvalid extends DomainValidation:
  def errorMessage: String = "Age must be between 18 and 75."
```

Validator implementation with `Either`:

```dotty
import cats.syntax.all.*

sealed trait FormValidator:
  def validateUserName(userName: String): Either[DomainValidation, String] =
    Either.cond(
      userName.matches("^[a-zA-Z0-9]+$"),
      userName,
      UsernameHasSpecialCharacters
    )

  def validatePassword(password: String): Either[DomainValidation, String] =
    Either.cond(
      password.matches(
        "(?=^.{10,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$"
      ),
      password,
      PasswordDoesNotMeetCriteria
    )

  def validateFirstName(firstName: String): Either[DomainValidation, String] =
    Either.cond(
      firstName.matches("^[a-zA-Z]+$"),
      firstName,
      FirstNameHasSpecialCharacters
    )

  def validateLastName(lastName: String): Either[DomainValidation, String] =
    Either.cond(
      lastName.matches("^[a-zA-Z]+$"),
      lastName,
      LastNameHasSpecialCharacters
    )

  def validateAge(age: Int): Either[DomainValidation, Int] =
    Either.cond(
      age >= 18 && age <= 75,
      age,
      AgeIsInvalid
    )

  def validateForm(
      username: String,
      password: String,
      firstName: String,
      lastName: String,
      age: Int
  ): Either[DomainValidation, RegistrationData] =
    for
      validatedUserName  <- validateUserName(username)
      validatedPassword  <- validatePassword(password)
      validatedFirstName <- validateFirstName(firstName)
      validatedLastName  <- validateLastName(lastName)
      validatedAge       <- validateAge(age)
    yield RegistrationData(
      validatedUserName,
      validatedPassword,
      validatedFirstName,
      validatedLastName,
      validatedAge
    )
end FormValidator

object FormValidator extends FormValidator
```

The problem: for-comprehension **stops at the first error** (fail-fast). 
If multiple fields are invalid, we'll see only the first error:

```dotty
FormValidator.validateForm(
  username = "fakeUs3rname",
  password = "password",
  firstName = "John",
  lastName = "Doe",
  age = 15
)
// Left(UsernameHasSpecialCharacters) - only the first error!
```

### Meeting Validated and Applicative

To accumulate all errors, we use `Validated` with `NonEmptyChain`:

```dotty
import cats.data.*
import cats.data.Validated.*
import cats.syntax.all.*

sealed trait FormValidatorNec:
  type ValidationResult[A] = ValidatedNec[DomainValidation, A]

  private def validateUserName(userName: String): ValidationResult[String] =
    if userName.matches("^[a-zA-Z0-9]+$") then userName.validNec
    else UsernameHasSpecialCharacters.invalidNec

  private def validatePassword(password: String): ValidationResult[String] =
    if password.matches(
        "(?=^.{10,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$"
      )
    then password.validNec
    else PasswordDoesNotMeetCriteria.invalidNec

  private def validateFirstName(firstName: String): ValidationResult[String] =
    if firstName.matches("^[a-zA-Z]+$") then firstName.validNec
    else FirstNameHasSpecialCharacters.invalidNec

  private def validateLastName(lastName: String): ValidationResult[String] =
    if lastName.matches("^[a-zA-Z]+$") then lastName.validNec
    else LastNameHasSpecialCharacters.invalidNec

  private def validateAge(age: Int): ValidationResult[Int] =
    if age >= 18 && age <= 75 then age.validNec
    else AgeIsInvalid.invalidNec

  def validateForm(
      username: String,
      password: String,
      firstName: String,
      lastName: String,
      age: Int
  ): ValidationResult[RegistrationData] =
    (
      validateUserName(username),
      validatePassword(password),
      validateFirstName(firstName),
      validateLastName(lastName),
      validateAge(age)
    ).mapN(RegistrationData.apply)
end FormValidatorNec

object FormValidatorNec extends FormValidatorNec
```

#### Key Changes

1. **`ValidatedNec[DomainValidation, A]`** — alias for `Validated[NonEmptyChain[DomainValidation], A]`
2. **`.validNec` and `.invalidNec`** — combinators for lifting into `ValidatedNec`
3. **`.mapN(...)`** — applicative syntax for error accumulation
4. **`NonEmptyChain`** — data structure guaranteeing at least one element

#### Execution Result

```dotty
// Successful validation
FormValidatorNec.validateForm(
  username = "Joe",
  password = "Passw0r$1234",
  firstName = "John",
  lastName = "Doe",
  age = 21
)
// Valid(RegistrationData(Joe,Passw0r$1234,John,Doe,21))

// Validation with errors - ALL errors collected!
FormValidatorNec.validateForm(
  username = "Joe%%%",
  password = "password",
  firstName = "John",
  lastName = "Doe",
  age = 21
)
// Invalid(Chain(UsernameHasSpecialCharacters, PasswordDoesNotMeetCriteria))
```

### Error Accumulation: Semigroup

`Validated` accumulates errors via [`Semigroup`](../typeclass/monoid/semigroup.md). 
`NonEmptyChain` has a `Semigroup` instance by default, combining two chains:

```dotty
NonEmptyChain.one[DomainValidation](UsernameHasSpecialCharacters) |+|
NonEmptyChain[DomainValidation](FirstNameHasSpecialCharacters, LastNameHasSpecialCharacters)
```

You can define your own way of combining errors by providing a custom `Semigroup` implementation.

### Transformations: Validated ↔ Either

#### From Either to Validated

```dotty
FormValidator.validateUserName("Joe%%%").toValidatedNec
// Invalid(NonEmptyChain(UsernameHasSpecialCharacters))
```

#### From Validated to Either

```dotty
FormValidatorNec.validateForm(
  username = "Joe",
  password = "Passw0r$1234",
  firstName = "John",
  lastName = "Doe",
  age = 21
).toEither
// Right(RegistrationData(Joe,Passw0r$1234,John,Doe,21))

FormValidatorNec.validateForm(
  username = "Joe123#",
  password = "password",
  firstName = "John",
  lastName = "Doe",
  age = 5
).toEither
// Left(Chain(UsernameHasSpecialCharacters, PasswordDoesNotMeetCriteria, AgeIsInvalid))
```

### Practical Example: Config Parsing

Let's consider a more complex example — parsing a configuration file:

```dotty
import cats.data.*
import cats.syntax.all.*
import cats.Semigroup

// Read type class for parsing values
trait Read[A]:
  def read(s: String): Option[A]

object Read:
  def apply[A: Read as r]: Read[A] = r

  given Read[String] =
    (s: String) => Some(s).filter(_.nonEmpty)

  given Read[Int] =
    (s: String) =>
      if s.matches("-?[0-9]+") then Some(s.toInt)
      else None

// Error model
sealed abstract class ConfigError
final case class MissingConfig(field: String) extends ConfigError
final case class ParseError(field: String)    extends ConfigError

// Configuration
case class Config(map: Map[String, String]):
  def parse[A: Read](key: String): Validated[ConfigError, A] =
    map.get(key) match
      case None        => Validated.Invalid(MissingConfig(key))
      case Some(value) =>
        Read[A].read(value) match
          case None    => Validated.Invalid(ParseError(key))
          case Some(a) => Validated.Valid(a)
```

#### Parallel Validation

```dotty
import cats.data.*
import cats.syntax.all.*
import Read.given

case class ConnectionParams(url: String, port: Int)

val config = Config(Map(("endpoint", ""), ("port", "not an int")))

given Semigroup[NonEmptyChain[ConfigError]] =
  SemigroupK[NonEmptyChain].algebra[ConfigError]

// Collect all errors
val result = (
  config.parse[String]("endpoint").toValidatedNec,
  config.parse[Int]("port").toValidatedNec
).mapN(ConnectionParams.apply)
// Invalid(Chain(ParseError(endpoint), ParseError(port)))
```

### Why is Validated Not a Monad?

If `Validated` were a monad, it would have a `flatMap` method. 
However, `flatMap` implies sequential (fail-fast) processing, 
which contradicts the primary goal of `Validated` — error accumulation.

```dotty
import cats.Monad

// Attempt to create a Monad for Validated (doesn't work correctly)
given validatedMonad[E]: Monad[[A] =>> Validated[E, A]] =
  new Monad[[A] =>> Validated[E, A]]:
    def flatMap[A, B](fa: Validated[E, A])(f: A => Validated[E, B])
        : Validated[E, B] =
      fa match
        case Validated.Valid(a)       => f(a)
        case i @ Validated.Invalid(_) => i

    def pure[A](x: A): Validated[E, A] = Validated.Valid(x)

    @annotation.tailrec
    def tailRecM[A, B](a: A)(f: A => Validated[E, Either[A, B]])
        : Validated[E, B] =
      f(a) match
        case Validated.Valid(Right(b)) => Validated.Valid(b)
        case Validated.Valid(Left(a))  => tailRecM(a)(f)
        case i @ Validated.Invalid(_)  => i
        
given validatedApplicative[E: Semigroup as semi]
    : Applicative[[A] =>> Validated[E, A]] =
  new Applicative[[A] =>> Validated[E, A]]:
    def pure[A](x: A): Validated[E, A] =
      Validated.valid(x)

    def ap[A, B](ff: Validated[E, A => B])(fa: Validated[E, A])
        : Validated[E, B] =
      (ff, fa) match
        case (Validated.Valid(f), Validated.Valid(a)) =>
          Validated.Valid(f(a))
        case (i @ Validated.Invalid(_), Validated.Valid(_)) => i
        case (Validated.Valid(_), i @ Validated.Invalid(_)) => i
        case (Validated.Invalid(e1), Validated.Invalid(e2)) =>
          Validated.Invalid(semi.combine(e1, e2))
```

According to [the monad laws](../typeclass/monad/monad.md), the following must hold:

```dotty
fab.ap(fa) <-> fab.flatMap(f => fa.map(f))
```

The problem: the law above doesn't hold. `flatMap` and `ap` (applicative) become **inconsistent**:

```dotty
val fab = Validated.invalidNec[String, Int => Double]("Error 1")
val fa  = Validated.invalidNec[String, Int]("Error 2")

// Using Monad (fail-fast)
val monad = validatedMonad[NonEmptyChain[String]]
monad.ap(fab)(fa)
// Invalid(Error 1) - first error
monad.flatMap(fab)(f => fa.map(f))
// Invalid(Error 1) - first error

// Using Applicative (accumulation)
val applicative = validatedApplicative[NonEmptyChain[String]]
applicative.ap(fab)(fa)
// Invalid(Error 1, Error 2) - both errors
```

**Conclusion**: `Validated` cannot be a monad, as that would violate the `FlatMap` laws.

### Validated vs Either: When to Use What

| Scenario                                                        | Recommendation    |
|-----------------------------------------------------------------|-------------------|
| Need to accumulate all errors                                   | Use `Validated`   |
| Need sequential validation (dependent checks)                   | Use `Either`      |
| Working in for-comprehension                                    | Use `Either`      |
| Parallel checking of independent fields                         | Use `Validated`   |
| API requiring all errors at once                                | Use `Validated`   |
| First error stops the process                                   | Use `Either`      |

### Sequential Validation Within Validated

Sometimes sequential checking is required within accumulating validation. `Validated` provides methods for this:

#### `andThen` (flatMap analog)

```dotty
def positive(field: String, i: Int): Validated[ConfigError, Int] =
  if i >= 0 then Validated.Valid(i)
  else Validated.Invalid(ParseError(field))

val houseNumber = config.parse[Int]("house_number").andThen { n =>
  positive("house_number", n)
}
// Invalid(MissingConfig(house_number))
```

#### `withEither` (temporary transformation)

```dotty
def positiveEither(field: String, i: Int): Either[ConfigError, Int] =
  if i >= 0 then Right(i)
  else Left(ParseError(field))

val houseNumber = config.parse[Int]("house_number").withEither {
  (either: Either[ConfigError, Int]) =>
    either.flatMap { i =>
      positiveEither("house_number", i)
    }
}
// Invalid(MissingConfig(house_number))
```

### Conclusion

`Validated` is a powerful tool for scenarios requiring error accumulation. 
Unlike `Either`, it is not a monad, but provides rich capabilities for parallel validation via `Applicative`. 
Its use is especially justified when:

- Validating forms with many fields
- Parsing configurations
- Checking API input data
- Any scenarios where it's important to inform the user of all issues at once

---

**References:**

- [Link to the exercises repository][sample]
- [Cats: Ior](https://typelevel.org/cats/datatypes/ior.html)
- [Cats: Validated](https://typelevel.org/cats/datatypes/validated.html)
- [Functional Programming in Scala, Second Edition, Chapter 4](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [I hate NULL and all its variants!](https://alexn.org/blog/2010/05/25/i-hate-null/)
- [Making something out of nothing (or, why None is better than NaN and NULL)](https://blog.janestreet.com/making-something-out-of-nothing-or-why-none-is-better-than-nan-and-null/)
- [Martians vs Monads: Null Considered Harmful](https://james-iry.blogspot.com/2007/08/martians-vs-monads-null-considered.html)
- [Maybe in Java](https://blog.tmorris.net/posts/maybe-in-java/index.html)
- [Null, NullPointerException and dealing with it](https://scalac.io/blog/null-pointer-exception-npe/)
- [Null References: The Billion Dollar Mistake](https://www.infoq.com/presentations/Null-References-The-Billion-Dollar-Mistake-Tony-Hoare/)
- [Scala Option - a Gentle Introduction](https://www.youtube.com/watch?v=xywCiwNwPEU&amp;list=PLmtsMNDRU0Bzj7INIrLugi3a_WClwQuiS)
- [The Difference Between Null, Nothing, Nil, None and Unit in Scala - Rock the JVM](https://www.youtube.com/watch?v=0_-jRrdg9Dw&amp;list=PLmtsMNDRU0Bzj7INIrLugi3a_WClwQuiS)

[sample]: https://gitverse.ru/scalabook/fp
