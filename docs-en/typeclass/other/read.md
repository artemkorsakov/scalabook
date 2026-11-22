# Read

`Read` is a type class that provides an abstraction for converting strings into objects of a specific type.
It is the inverse operation of the [`Show`](show.md) type class and is used for parsing strings into values.

**Where is it used?**

- **Data parsing**: Converting strings obtained from files, network requests, or user input into objects.
- **Configuration**: Reading and interpreting settings from configuration files.
- **Serialization**: Deserializing data from text formats (e.g., JSON, CSV).
- **Validation**: Checking and converting strings into valid values, such as numbers or dates.

**Usage examples:**

- Converting the string `"42"` into the number `42`.
- Parsing the string `"2023-10-01"` into a date object.
- Reading a configuration parameter `"timeout=30"` and converting it to an integer.
- Deserializing a JSON string into a user object.

**Advantages:**

- **Type safety**: `Read` requires explicit implementation for each type, making parsing more predictable.
- **Flexibility**: You can define custom rules for parsing various types.
- **Integration with libraries**: Used in libraries for working with configurations, serialization, and validation.

**Alternative to standard methods:**

Unlike methods like `Integer.parseInt`, `Read` provides a more universal and type-safe approach,
helping avoid errors related to incorrect string format.

## Formal definition

`Read` is a type class that provides an abstraction for converting strings into objects of a specific type.
The `read` function takes a string and returns the type that is a member of `Read`.

For a correct implementation of `Read`, the following laws must hold:

- **Consistency**: For a valid string `s` corresponding to type `A`,
  `read(s)` should return `Right(value)`, where `value` is a valid value of type `A`.
- **Reversibility**: If `read(s)` returns `Right(value)`,
  then `Show[A].show(value)` should return a string equivalent to `s` (if `Show` is also implemented).
- **Error handling**: For invalid strings, `read(s)` should return `Left` with an error description.

## Code definition in Scala

```dotty
trait Read[F]:
  def read(s: String): Either[String, F]
```

- **Type parameter `A`**: `Read` is parameterized by type `A` for which parsing is defined.
- **`read` method**: The `read` method takes a string and returns either a successfully parsed value of type `A`,
  or an error message as a string.

### Usage example

```dotty
object Read:
  given Read[Int] with
    def read(s: String): Either[String, Int] =
      s.toIntOption.toRight(s"Invalid integer: $s")

summon[Read[Int]].read("42")
// Right(42)
summon[Read[Int]].read("abc")
// Left("Invalid integer: abc")
```

---

**References:**

- [Repository with exercises][sample]
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Read.html)

[sample]: https://gitverse.ru/scalabook/typeclasses
