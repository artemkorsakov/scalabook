# Show

**The `Show` type class** is an abstraction that provides the ability to convert values of any type
into their string representation.
It is an alternative to the `toString` method that allows flexible customization of output formatting for various data types.

The `Show` type class defines one primary operation:

- **`show`** — a function that takes a value of type `A` and returns its string representation of type `String`.

**Where is it used?**

- **Logging**:
  Logs often need to output values of various types in a readable format. `Show` allows customizing formatting for each type.
- **Debugging**:
  When debugging code, `Show` can be used to output values in a developer-friendly format.
- **Custom output**:
  In applications that need to display data to users,
  `Show` allows customizing formatting depending on the context
  (e.g., displaying numbers with a specific number of decimal places).
- **Serialization**:
  `Show` can be used to convert data to strings before saving or transmitting over a network.

**Advantages over `toString`:**

- **Flexibility**: `Show` allows customizing the string representation for each type separately.
- **Safety**: `Show` is independent of the `toString` implementation, which may be inconvenient or unsafe
  (e.g., `toString` for arrays in Java outputs a hash code).
- **Extensibility**: `Show` can be implemented for any types, including third-party libraries, without modifying their source code.

## Formal definition

`Show` is a type class that provides an abstraction for converting objects into their string representation.

For a correct implementation of `Show`, the following laws must hold:

- **Consistency**: For any value `x` of type `A`, `show(x)` should return the same string representation on every call.
- **Readability**: The string representation should be understandable and useful to a human.

## Code definition in Scala

```dotty
trait Show[A]:
  def show(a: A): String
```

- **Type parameter `A`**: `Show` is parameterized by type `A` for which string representation is defined.
- **`show` method**: The `show` method takes a value of type `A` and returns its string representation.

## Implementation in libraries

### Implementation in Cats

```dotty
import cats.*
import cats.implicits.*
123.show     // 123
"abc".show   // abc
```

### Implementation in ScalaZ

```dotty
import scalaz.*
import Scalaz.*
1.0.shows // "1.0"
```

---

**References:**

- [Repository with exercises][sample]
- [Cats](https://typelevel.org/cats/typeclasses/show.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Show.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Show.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Show.html)

[sample]: https://gitverse.ru/scalabook/typeclasses
