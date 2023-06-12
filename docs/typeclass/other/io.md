# IO

Тип данных ввода-вывода.

## Описание

```scala
sealed trait IO[+A]:
  private[effect] def apply(rw: World[RealWorld]): Trampoline[(World[RealWorld], A)]
```

## Реализация

### Реализация в ScalaZ

```scala
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

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/IO+Monad.html)
- [Towards an Effect System in Scala, Part 2: IO Monad](https://apocalisp.wordpress.com/2011/12/19/towards-an-effect-system-in-scala-part-2-io-monad/)
