# Introduction to Functional Programming with Scala

This book is my translation and adaptation of a series of English-language articles on functional programming in Scala. I've added many new details, examples, and exercises based on my own experience as a practitioner of functional programming.

## What this book is

This book is a practical guide to functional programming in Scala. It's designed for developers who want to use the power of functional programming to write reliable and elegant code. The book assumes basic familiarity with Scala and the general principles of programming.

## What this book is not

This is not an introductory programming textbook. It's also not a deep dive into the mathematical foundations of functional programming. Instead, it's a practical guide for developers who want to use functional programming in their daily work.

## What you will learn from this book

After reading this book, you will be able to:

- **Understand the philosophy of functional programming**: understand the basic principles and paradigms of functional programming in Scala.
- **Compose functions and work with higher-order functions**: use functions as first-class values to write expressive, concise, and reusable code. Work with pure functions, which are central to functional programming and guarantee that the result of a function depends only on its inputs.
- **Use algebraic data types**: define custom data types and model domain models using sum and product types (ADT). Learn how to define and use ADT (algebraic data types), which are one of the main tools for modeling domain models.
- **Work with higher-kinded types (HKT)**: understand what higher-kinded types are, how they support generic programming, and use the Kleisli and Bimonad patterns.
- **Work with the effect system in Scala**: understand and use the effect system in Scala, including the `Try`, `Future`, and `Either` types. Learn how to build a custom effect system using `Free` monads and the `Cats Effect` library.
- **Use the `Cats Effect` library and the `IO` effect**: understand and use the `IO` effect in `Cats Effect` to write safe and composable effects. Learn how to use the `Concurrent` typeclass to work with concurrent effects using `Concurrent[IO]` and `Resource` for resource management. Learn about the `Async` typeclass and how to work with external asynchronous APIs that use callbacks or `Future`, and how to use a scheduler to manage delays and periodic tasks in your programs using `Scheduler`. Work with the environment and environment effects using `Reader` and the `Environment` monad.
- **Use the ZIO framework**: understand and use the ZIO framework to build reliable and testable applications in Scala. Learn about the `ZIO` effect type, `ZEnvironment`, and `ZLayer` for dependency injection. Learn how to compose effects with `ZStream`, work with concurrent effects using `ZIO` and `ZQueue`, work with `Deferred`, `Ref`, `Promise`, `CountDownLatch`, and `TMap`, use `ZPool` for thread pool management, and work with `TArray` and transactional memory.
- **Work with the effect system in Scala**: understand and use the effect system in Scala, including the `Try`, `Future`, and `Either` types. Learn how to build a custom effect system using `Free` monads and the `Cats Effect` library. Work with the environment and environment effects using `Reader` and the `Environment` monad.
- **Work with the `IO` effect in Cats Effect**: learn the difference between effect construction and effect execution, effect memoization, effect composition using `flatMap`, `map`, and `product`, safe exception handling, and concurrent effects with `parMap2` and `race`.
- **Use typeclasses and implicit conversions**: learn how the standard library uses typeclasses, use the "Law of Demeter" approach to build interfaces, and use implicit conversions to extend existing types. Work with extension methods and extension functions to extend existing types.
- **Work with macro systems and type-level programming**: learn the basics of working with macros and type-level programming in Scala, including how to work with type-level integers, conditionals, and pattern matching, and how to build a complete type-level calculation system.

## Book structure

### Part 1. Functional paradigms

This part introduces the basic concepts and paradigms of functional programming. In the first chapters you will learn the main features of functional programming: immutability, pure functions, and the main advantages and disadvantages of this paradigm.

You will also learn about the key principles of functional programming and functional patterns: immutability, higher-order functions, referential transparency, currying, composition of functions, lazy evaluation, and pattern matching. The first part will also introduce the `Reader` monad as the main tool for working with a global environment in functional style.

### Part 2. Data representation

This part is dedicated to the use of algebraic data types (ADT) for modeling domain models. You will learn about sum and product types, the types of `Option`, `Either`, and `Validated`, their purpose, and how to use them when modeling applications. You will also learn about `cats` and `Discipline`.

### Part 3. Effect systems

In the third part you will learn about the basics of functional programming and how to work with effect systems in Scala. We will discuss the monad, the use of the `Try`, `Future`, and `Either` types in the standard library. In this part you will learn about `State` monad, `Cokleisli`, `Bimonad`, and `Free` monad. You will also learn how to build your own effect system using `Free` monad. Finally, you'll learn how to build a custom effect system using the `Free` monad.

Next, you will learn the basics of the `Cats Effect` library and how to use it to work with the `IO` effect. You will learn about effect construction and execution, effect memoization, and effect composition using `flatMap`, `map`, and `product`. The `Cats Effect` part also includes the `Concurrent` typeclass for working with concurrent effects using `Concurrent[IO]`, the `Resource` type for resource management, `Async` typeclass for working with external asynchronous APIs using callbacks or `Future`, and `Scheduler` for managing delays and periodic tasks.

Then you will learn about ZIO: the `ZIO` effect type, `ZEnvironment`, `ZLayer` for dependency injection, `ZStream` for composing effects, `ZIO` and `ZQueue` for working with concurrent effects, `Deferred`, `Ref`, `Promise`, `CountDownLatch`, and `TMap`, `ZPool` for thread pool management, and `TArray` and transactional memory.

Finally, you will learn the basics of `Reader` monad and `Environment`, the main tool for working with a global environment in functional style.

### Part 4. Advanced features

The final part of the book covers advanced language features and techniques used in real projects: working with typeclasses, the macro system, and type-level programming.

## Additional chapters

In this edition of the book, I've added several chapters based on my personal experience and practical problems that I've encountered in my work. These chapters explore topics that I found interesting or important. You'll find sections on SICP (which will be periodically updated) at the end of the book.

## Code examples

All examples in this book are written in Scala 3. To run them, you will need at least Scala version 3.7.1.

To run code examples, use the `dotty` interpreter, located in the `examples/modules/scalabook-examples/src/main/scala-3` directory.

## Conventions

- **Scala code**: Examples use the `.dotty` extension for Scala 3 syntax highlighting.
- **Terminal output**: Results of executing code in the REPL.
- **Code comments**: Often contain additional explanations in code.

## Exercises

Each chapter contains exercises to consolidate the material. They range from simple questions to tasks that require writing small programs.

## Feedback

I would appreciate your feedback and suggestions. If you find a mistake or have ideas for improving the material, please let me know through [GitHub issues](https://github.com/avansutten/scalabuke/issues).
