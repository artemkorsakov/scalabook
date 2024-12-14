# Полугруппа

**Полугруппа** – это важная математическая структура, 
которая лежит в основе многих понятий современной алгебры и теории категорий. 
Она состоит из множества элементов и одной бинарной операции, которая обладает свойством ассоциативности. 
В отличие от групп и моноидов, полугруппа не требует наличия нейтрального элемента 
или обратного элемента для каждого своего элемента.

Полугруппы позволяют абстрагироваться от конкретных типов данных 
и работать с операциями объединения, сложения или конкатенации, 
что делает их полезными в различных сценариях, особенно при работе с коллекциями и агрегации данных.

**Что такое полугруппа?**

**Полугруппа** — это структура, состоящая из:

- **Множества элементов** (например, числа, строки, списки).
- **Бинарной операции** (например, сложение, конкатенация), которая объединяет два элемента множества в один.

При этом выполняется одно ключевое свойство:

- **Ассоциативность**: Результат операции не зависит от порядка вычислений. Например, `(a + b) + c` равно `a + (b + c)`.

Важно отметить, что в полугруппе **отсутствует нейтральный элемент**. 
Это делает полугруппы более общим понятием, чем моноиды, и позволяет работать с типами данных, 
для которых невозможно или не имеет смысла определить нейтральный элемент.

## Формальное определение

\\((S, +)\\) является полугруппой (_semigroup_) для множества \\(S\\) и операции \\(+\\), 
если удовлетворяет следующим свойствам для любых \\(x, y, z \in S\\):

- _Замыкание_ (_closure_): для \\(\forall x, y \in S\\) выполняется \\(x + y \in S\\).
- _Ассоциативность_ (_associativity_): для \\(\forall x, y, z \in S\\) выполняется \\((x + y) + z = x + (y + z)\\).

Также говорится, что _S образует полугруппу относительно +_.

## Зачем нужны полугруппы?

Полугруппы полезны, потому что они:

- **Абстрагируют операции объединения**: Вы можете работать с разными типами данных (числа, строки, списки и т.д.), 
  используя один и тот же интерфейс для объединения.
- **Упрощают композицию**: Полугруппы позволяют легко объединять данные, 
  что особенно полезно при работе с коллекциями или агрегации результатов.
- **Расширяют возможности моноидов**: Если моноид требует наличия нейтрального элемента, 
  то полугруппа может быть применима в более широком диапазоне задач, где такой элемент не определен.
- **Улучшают читаемость кода**: Использование полугрупп делает код более декларативным и понятным, 
  так как вы явно выражаете намерение объединить данные.

## Примеры полугрупп

Некоторые примеры полугрупп, которые часто встречаются в программировании:

- Сложение чисел: Операция — сложение.
- Умножение чисел: Операция — умножение.
- Конкатенация строк: Операция — конкатенация.
- Объединение множеств: Операция — объединение.
- Списки: Операция — конкатенация списков.

> Обратите внимание, что в этих примерах отсутствует нейтральный элемент, но операция остается ассоциативной.

## Код

```dotty
trait Semigroup[A]:
  def combine(x: A, y: A): A
```

Для операции "сложения" встречаются следующие наименования:
`combine`, `append`, `mappend` (Haskell), `op`, `plus`, `times`.

Законы полугруппы принимают следующий вид:

- замыкание следует из определения функции `combine` - тип результата тот же, что и тип переменных.
  При условии использования при реализации только [чистых функций без побочных эффектов](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D1%82%D0%BE%D1%82%D0%B0_%D1%84%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D0%B8).
- ассоциативность формулируется так: `m.combine(x, m.combine(y, z)) == m.combine(m.combine(x, y), z)`

```dotty
import cats.data.*
import cats.syntax.either.*

trait Laws:
  protected def check(
      law: Boolean,
      message: => String
  ): ValidatedNel[String, Unit] =
    Either
      .cond[String, Unit](law, (), message)
      .toValidatedNel

trait SemigroupLaw extends Laws:
  def checkSemigroupLaw[A: Semigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    check(
      Semigroup[A].combine(Semigroup[A].combine(x, y), z) ==
        Semigroup[A].combine(x, Semigroup[A].combine(y, z)),
      "associative: (x + y) + z == x + (y + z)"
    )
```

## Примеры

### Непустой список

```dotty
case class NonEmptyList[+A](head: A, tail: List[A]):
  def toList: List[A] = head :: tail

given [A]: Semigroup[NonEmptyList[A]] with
  def combine(x: NonEmptyList[A], y: NonEmptyList[A]) =
    NonEmptyList(x.head, x.tail ++ y.toList)
```

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

## Дополнительные операции

Благодаря ассоциативности, можно корректно определить натуральную степень элемента полугруппы ("сложить n раз") как:

\\(a^{n} = \overset{n}{a + ... + a}\\)

Для степени элемента справедливы соотношения \\(a^{n + m} = a^{m}+a^{n}, (a^{m})^{n} = a^{mn}, \forall n,m \in \mathbb{N}\\).

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


## Реализация в библиотеках

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
- [Symbolic operators and type classes for Cats](https://typelevel.org/blog/2015/08/07/symbolic-operators.html)
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D1%83%D0%B3%D1%80%D1%83%D0%BF%D0%BF%D0%B0)
