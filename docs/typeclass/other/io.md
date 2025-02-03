# IO

## Формальное определение

Тип данных ввода-вывода.

## Определение в виде кода на Scala

```dotty
sealed trait IO[+A]:
  private[effect] def apply(rw: World[RealWorld]): Trampoline[(World[RealWorld], A)]
```

## Реализация в библиотеках

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*
import scalaz.effect.*
import scalaz.effect.IO.*

val action1 =
  for
    x <- readLn
    _ <- putStrLn("Hello, " + x + "!")
  yield ()
action1.unsafePerformIO()
```

---

**Ссылки:**

- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/IO+Monad.html)
- [Towards an Effect System in Scala, Part 2: IO Monad](https://apocalisp.wordpress.com/2011/12/19/towards-an-effect-system-in-scala-part-2-io-monad/)
