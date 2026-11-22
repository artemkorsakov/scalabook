# Type classes

Type classes represent a powerful concept in functional programming
that allows extending the functionality of data types without modifying their source code.
This approach is especially popular in languages such as Haskell, but it is also actively used in Scala,
thanks to its hybrid nature, combining functional and object-oriented programming.

## What are type classes?

Type classes are abstract interfaces that define a set of operations or behaviors
that can be implemented for various data types.
Unlike ordinary interfaces in object-oriented programming,
type classes allow adding behavior to already existing types, even if you don't have access to their source code.
This makes them extremely flexible and convenient for building extensible program architectures.

## Why are type classes needed?

1. **Extensibility**: Type classes allow adding new functions or operations to already existing types without modifying them.
   For example, you can add support for arithmetic operations for custom data types,
   even if they were not originally designed for that.
2. **Polymorphism**: Type classes provide parametric polymorphism, which means
   that the same function can work with different data types if they implement the appropriate type class.
   This makes the code more universal and convenient for reuse.
3. **Code purity**: Using type classes helps avoid "polluting" base types with extra methods,
   which is especially important when working with libraries or third-party data types.
4. **Flexibility**: Type classes allow creating specialized implementations for different types,
   making them an ideal tool for tuning behavior depending on context.

## Examples of using type classes

Type classes are widely applied in various areas of programming:

- **Serialization and deserialization**: Type classes can define how objects of different types
  should be converted into JSON, XML, or other formats.
- **Arithmetic operations**: You can define a type class for performing arithmetic operations
  (for example, addition, multiplication) for various data types.
- **Formatting**: Type classes allow defining how objects should be represented as strings
  (for example, for output to a log or a user interface).
- **Equality checking**: You can create a type class that defines how to compare objects of different types.

## How does this work in Scala?

In Scala, type classes are implemented using traits and implicit parameters.
A trait defines the interface that must be implemented for a specific type,
and implicit parameters allow automatically providing instances of these implementations where needed.
This makes using type classes in Scala convenient and elegant.

---

**Links:**

- [Link to the repository with exercises][sample]
- [Type classes in Algebird][algebird]
- [Type classes in Cats][cats]
- [Type classes in Spire][spire]
- [Cats: Type Classes](https://typelevel.org/cats/typeclasses.html)
- [Category Theory Resources](https://github.com/prathyvsh/category-theory-resources)
- [Deriving Typeclass Instances](https://xebia.com/blog/automatically-deriving-typeclass-instances-in-scala-3/)
- [Deriving Type Class Instances](https://typelevel.org/blog/2013/06/24/deriving-instances-1.html)
- [Infinity Category Theory](https://www.scientificamerican.com/article/infinity-category-theory-offers-a-birds-eye-view-of-mathematics1)
- [On Scala, Functional Programming and Type-Classes](https://alexn.org/blog/2012/11/02/scala-functional-programming-type-classes/)
- [Scala Type-class Hierarchy - Tony Morris](https://blog.tmorris.net/posts/scala-type-class-hierarchy/index.html)
- [Some Notes in Hostility Toward Subtyping](https://apocalisp.wordpress.com/2009/08/27/hostility-toward-subtyping/)
- [Subtype type classes don't work](https://typelevel.org/blog/2016/09/30/subtype-typeclasses.html)
- [The Power of Type-classes with Scala implicit defs](https://blog.tmorris.net/posts/the-power-of-type-classes-with-scala-implicit-defs/index.html)
- [The Typeclass Pattern](https://www.youtube.com/watch?v=yYo0gANYViE)
- [Type Classes](https://typelevel.org/cats/typeclasses.html)
- [Type Classes vs Records of Functions](https://www.inner-product.com/posts/type-classes-vs-records-of-functions/)
- [Type Classes in Scala - Baeldung](https://www.baeldung.com/scala/type-classes)
- [Typeclasses in Scala](https://www.youtube.com/watch?v=sVMES4RZF-8)
- [Typeclasses in Scala 3](https://www.youtube.com/watch?app=desktop&amp;v=co4LSvVoX-s)
- [Why are Type Classes Useful in Scala?](https://blog.rockthejvm.com/why-are-typeclasses-useful/)
- [Why Type Classes are Useful](https://www.youtube.com/watch?v=bupBZKJT0EA)

[algebird]: https://twitter.github.io/algebird/typeclasses.html
[cats]: https://github.com/tpolecat/cats-infographic
[sample]: https://gitverse.ru/scalabook/typeclasses
[spire]: https://typelevel.org/spire/guide.html#type-classes-2