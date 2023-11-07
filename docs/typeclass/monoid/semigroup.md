# Полугруппа

## Формальное определение

\\((S, +)\\) является полугруппой (_semigroup_) для множества \\(S\\) и операции \\(+\\), 
если удовлетворяет следующим свойствам для любых \\(x, y, z \in S\\):

- _Замыкание_ (_closure_): для \\(\forall x, y \in S\\) выполняется \\(x + y \in S\\).
- _Ассоциативность_ (_associativity_): для \\(\forall x, y, z \in S\\) выполняется \\((x + y) + z = x + (y + z)\\).

Также говорится, что _S образует полугруппу относительно +_.

Благодаря ассоциативности, можно корректно определить натуральную степень элемента полугруппы ("сложить n раз") как:

\\(a^{n} = \overset{n}{a + ... + a}\\)

Для степени элемента справедливы соотношения \\(a^{n + m} = a^{m}+a^{n}, (a^{m})^{n} = a^{mn}, \forall n,m \in \mathbb{N}\\).

## Определение в виде кода на Scala

```dotty
trait Semigroup[A]:
  def combine(x: A, y: A): A

  def combineN(x: A, n: Int :| Greater[0]): A =
    @tailrec def loop(b: A, k: Int, acc: A): A =
      if k == 1 then combine(b, acc)
      else
        val x = if (k & 1) == 1 then combine(b, acc) else acc
        loop(combine(b, b), k >>> 1, x)

    if n == 1 then x else loop(x, n - 1, x)
```

> О том, что такое `Int :| Greater[0]` рассказывается в [статье об уточняющих типах](../../libs/refined.md).

Для операции сложения встречаются следующие наименования:
`combine`, `append`, `mappend` (Haskell), `op`, `plus`, `times`.

## Законы в виде кода на Scala

Законы полугруппы принимают следующий вид:

- замыкание следует из определения функции `combine` - тип результата тот же, что и тип переменных.
  При условии использования при реализации только [чистых функций без побочных эффектов](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D1%82%D0%BE%D1%82%D0%B0_%D1%84%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%B8).
- ассоциативность формулируется так: `m.combine(x, m.combine(y, z)) == m.combine(m.combine(x, y), z)`

```dotty
import cats.data.*
import cats.syntax.either.*

trait SemigroupLaw:
  def checkSemigroupLaw[A](x: A, y: A, z: A)(using
      sg: Semigroup[A]
  ): ValidatedNel[String, Unit] =
    import sg.combine
    Either
      .cond[String, Unit](
        combine(combine(x, y), z) == combine(x, combine(y, z)),
        (),
        "combine must be associative: (x + y) + z == x + (y + z)"
      )
      .toValidatedNel
```

## Примеры

### Натуральные числа N образуют полугруппу относительно сложения

```dotty
given sumSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x + y
```

### Натуральные числа N образуют полугруппу относительно умножения

```dotty
given productSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x * y
```

### Строки образуют полугруппу относительно конкатенации

```dotty
given stringSemigroupInstance: Semigroup[String] = (x: String, y: String) => x + y
```

### Последовательность образует полугруппу относительно операции объединения

```dotty
given listSemigroupInstance[T]: Semigroup[List[T]] =
  (x: List[T], y: List[T]) => x ++ y
```

### Кортеж от двух и более полугрупп также является полугруппой

```dotty
given nestedSemigroupInstance[A, B](using aSemigroup: Semigroup[A], bSemigroup: Semigroup[B]): Semigroup[(A, B)] =
  (x: (A, B), y: (A, B)) => (aSemigroup.combine(x._1, y._1), bSemigroup.combine(x._2, y._2))
```

## Реализация

### Реализация в Cats

```dotty
import cats.*
import cats.implicits.*

1 |+| 2                                     // 3
("hello", 123) |+| ("world", 321)           // ("helloworld",444)
```

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*

List(1, 2) |+| List(3)      // List(1, 2, 3)
List(1, 2) mappend List(3)  // List(1, 2, 3)
1 |+| 2                     // 3
```

### Реализация в Spire

```dotty
import spire.algebra.Semigroup
import spire.syntax.semigroup.*

1 |+| 2                           // 3
("hello", 123) |+| ("world", 321) // ("helloworld",444)

Semigroup.combineN(3, 5)          // 15
```

---

**Ссылки:**

- [Algebird](https://twitter.github.io/algebird/typeclasses/semigroup.html)
- [Cats](https://typelevel.org/cats/typeclasses/semigroup.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Semigroup.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monoid.html#Monoid)
- [Math world](https://mathworld.wolfram.com/Semigroup.html)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#definition-of-a-semigroup)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Semigroup.html)
- [Spire home page](https://typelevel.org/spire)
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D1%83%D0%B3%D1%80%D1%83%D0%BF%D0%BF%D0%B0)
