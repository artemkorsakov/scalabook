# Полугруппа

`(S, +)` является полугруппой (_semigroup_) для множества `S` и операции `+`, 
если удовлетворяет следующим свойствам для любых `x, y, z ∈ S`:

- _Замыкание_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ S`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`

Также говорится, что _S образует полугруппу относительно +_.

## Описание

```dotty
trait Semigroup[A]:
  def combine(x: A, y: A): A
  
  def combineN(x: A, n: Int): Option[A] =
    if n <= 0 then None
    else if n == 1 then Some(x)
    else combineN(x, n - 1).map(y => combine(x, y))
```

Используя операцию "сложения" можно определить операцию "сложить k раз" (`combineN`).

Для операции сложения встречаются следующие наименования: 
`combine`, `append`, `mappend` (Haskell), `op`, `plus`, `times`.

Законы полугруппы принимают следующий вид:

- замыкание следует из определения функции `combine` - тип результата тот же, что и тип переменных
- ассоциативность формулируется так: `m.combine(x, m.combine(y, z)) == m.combine(m.combine(x, y), z)`

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
