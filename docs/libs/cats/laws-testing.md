{%
showPrev = true
prevPage = glossary.html
showNext = true
nextPage = ../fs2.html
%}

# Тестирование законов

## Введение

Законы являются важной частью Cats. 
Они гарантируют, что пользовательские реализации классов типов ведут себя предсказуемо 
и соответствуют математическим требованиям, определённым для каждого класса типов. 
Cats использует библиотеку [discipline](https://github.com/typelevel/discipline) для определения законов классов типов 
и генерации тестов на основе [ScalaCheck](https://github.com/rickynils/scalacheck).

Тестирование законов позволяет разработчикам библиотек и приложений убедиться, 
что их собственные экземпляры классов типов корректны и соответствуют всем необходимым правилам. 
Это критически важно для поддержания правильности функциональных программ, где классы типов играют центральную роль.

## Начало работы

Прежде всего, вам необходимо добавить зависимость на `cats-laws` в ваш `build.sbt` файл:

```dotty
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-laws" % "@VERSION@" % Test,
)
```

Эта зависимость содержит все определения законов для классов типов Cats и инструменты для их тестирования. 
Она подключается только в области тестирования (`% Test`), так как не требуется в основном коде приложения.

## Пример: Тестирование экземпляра Functor

Давайте начнём с создания пользовательского типа данных и экземпляра `Functor` для него. 
В качестве примера возьмём бинарное дерево:

```dotty
import cats.*

sealed trait Tree[+A]
case object Leaf                                        extends Tree[Nothing]
case class Node[A](p: A, left: Tree[A], right: Tree[A]) extends Tree[A]

object Tree:
  given functorTree: Functor[Tree] = new Functor[Tree]:
    def map[A, B](tree: Tree[A])(f: A => B): Tree[B] =
      tree match
        case Leaf                 => Leaf
        case Node(p, left, right) => Node(f(p), map(left)(f), map(right)(f))
```

Мы определили простое бинарное дерево, где каждый узел содержит значение и два поддерева. 
Реализация `map` рекурсивно применяет функцию ко всем узлам, сохраняя структуру дерева.

Теперь мы хотим проверить, что наша реализация `Functor` действительно соответствует всем законам. 
Для этого Cats предоставляет тесты в пакете `cats.laws.discipline.*`. 
Каждый класс типов имеет соответствующий объект с методами для создания набора правил (`RuleSet`). 
Например, для `Functor` используется `FunctorTests`.

```dotty
cats.laws.discipline.FunctorTests[Tree].functor[Int, Int, String].all
```

Этот код создаёт `ScalaCheck` `Properties`, содержащие все законы для `Functor`.

### Создание экземпляра Eq

Большинство законов требуют сравнения значений для проверки корректности. 
Для этого необходим экземпляр `Eq`. Для простоты мы можем использовать `Eq.fromUniversalEquals`, 
который сравнивает значения через стандартное равенство Scala:

```dotty
given eqTree[A: Eq]: Eq[Tree[A]] = Eq.fromUniversalEquals
```

Однако в реальных проектах рекомендуется использовать более точные экземпляры `Eq`, 
которые учитывают семантику вашего типа данных.

### Генерация тестовых данных с Arbitrary

ScalaCheck требует экземпляры `Arbitrary` для типов данных, которые мы тестируем. 
`Arbitrary` отвечает за генерацию случайных значений для тестирования. 
Мы можем определить генератор для нашего дерева:

```dotty
import org.scalacheck.{Arbitrary, Gen}

given arbTree[A: Arbitrary]: Arbitrary[Tree[A]] =
  Arbitrary(Gen.oneOf(
    Gen.const(Leaf),
    Arbitrary.arbitrary[A].map(e => Node(e, Leaf, Leaf))
  ))
```

В этом примере мы создаём генератор, который с равной вероятностью создаёт либо пустой лист (`Leaf`), 
либо узел с одним значением и двумя пустыми поддеревьями. 
Для более сложных тестов вы можете создать более детальные генераторы, 
создающие деревья произвольной глубины и структуры.

### Интеграция с фреймворками тестирования

Теперь нам нужно преобразовать `ScalaCheck` `Properties` в тесты, которые сможет запустить наш тестовый фреймворк. 
Библиотека [discipline](https://github.com/typelevel/discipline) предоставляет вспомогательную функцию `checkAll` для трёх популярных фреймворков:

1. **Specs2**: расширьте ваш тестовый класс с помощью `org.typelevel.discipline.specs2.Discipline` (предоставляется `discipline-specs2`)
2. **ScalaTest**: расширьте ваш тестовый класс с помощью `org.typelevel.discipline.scalatest.FunSuiteDiscipline` 
   (предоставляется `discipline-scalatest`) и `org.scalatest.funsuite.AnyFunSuiteLike`
3. **MUnit**: расширьте ваш тестовый класс с помощью `munit.DisciplineSuite` (предоставляется `discipline-munit`)
4. Для других тестовых фреймворков вы можете использовать их интеграцию с ScalaCheck напрямую.

Рассмотрим пример с [weaver-tests](https://typelevel.org/weaver-test/features/discipline.html):

Добавим зависимость:

```dotty
libraryDependencies +=  "org.typelevel" %% "weaver-discipline" % "0.13.0" % Test
```

И сам сценарий тестирования: 

```dotty
import cats.laws.discipline.FunctorTests
import org.scalacheck.{Arbitrary, Gen}
import weaver.*
import weaver.discipline.*

object TreeSuite extends FunSuite with Discipline:
  checkAll("Tree.FunctorLaws", FunctorTests[Tree].functor[Int, Int, String])
```

### Запуск тестов и интерпретация результатов

Когда мы запускаем `test` в консоли sbt, ScalaCheck тестирует, выполняются ли законы `Functor` для нашего типа `Tree`. 
Вы должны увидеть что-то подобное:

```
[info] ru.scalabook.libs.cats.TreeSuite
[info] + Tree.FunctorLaws: functor.covariant composition 24ms
[info] + Tree.FunctorLaws: functor.covariant identity 17ms
[info] + Tree.FunctorLaws: functor.invariant composition 7ms
[info] + Tree.FunctorLaws: functor.invariant identity 1ms
[info] + Tree.FunctorLaws: functor.mapOrKeepToMapEquivalence 3ms
```

Каждый из этих тестов проверяет конкретный закон:

- **covariant composition** — проверяет, что `map(f).map(g)` эквивалентно `map(f andThen g)` (композиция)
- **covariant identity** — проверяет, что `map(identity)` эквивалентно `identity` (тождественность)
- **invariant composition** и **invariant identity** — аналогичные законы для инвариантного функтора
- **mapOrKeepToMapEquivalence** — проверяет, что `fa.mapOrKeep(pf)` эквивалентно `fa.map(a => pf.applyOrElse(a, identity[A1]))`

Успешно доказано, что тип данных `Tree` удовлетворяет законам `Functor`.

## Тестирование экземпляров cats.kernel

Для большинства классов типов, включённых в Cats, описанный выше подход отлично работает. 
Однако тесты законов для классов типов из `cats.kernel` (например, `Semigroup`, `Monoid`, `Group`, `Semilattice`) 
находятся в пакете `cats.kernel.laws.discipline.*`.

Давайте протестируем это, определив экземпляр `Semigroup` для нашего типа `Tree`:

```dotty
given semigroupTree[A: Semigroup]: Semigroup[Tree[A]] =
  new Semigroup[Tree[A]]:
    def combine(x: Tree[A], y: Tree[A]): Tree[A] = (x, y) match
      case (Leaf, _)                                          => Leaf
      case (_, Leaf)                                          => Leaf
      case (Node(xp, xLeft, xRight), Node(yp, yLeft, yRight)) =>
        Node(
          Semigroup[A].combine(xp, yp),
          combine(xLeft, yLeft),
          combine(xRight, yRight)
        )
```

Здесь мы реализуем операцию `combine` для деревьев: если одно из деревьев пустое (`Leaf`), результат также пустой; 
в противном случае мы комбинируем значения узлов и рекурсивно комбинируем левые и правые поддеревья.

Затем мы можем добавить тесты для `Semigroup` в нашу тестовую suite:

```dotty
import cats.kernel.laws.discipline.SemigroupTests
import cats.laws.discipline.FunctorTests
import org.scalacheck.{Arbitrary, Gen}
import weaver.*
import weaver.discipline.*

object TreeSuite extends FunSuite with Discipline:
  checkAll("Tree.FunctorLaws", FunctorTests[Tree].functor[Int, Int, String])

  checkAll("Tree[Int].SemigroupLaws", SemigroupTests[Tree[Int]].semigroup)
```

Теперь наши тесты проверяют не только законы функтора, но и законы полугруппы, включая ассоциативность операции `combine`.

```
[info] + Tree[Int].SemigroupLaws: semigroup.associative 2ms
[info] + Tree[Int].SemigroupLaws: semigroup.combineAllOption 6ms
[info] + Tree[Int].SemigroupLaws: semigroup.intercalateCombineAllOption 9ms
[info] + Tree[Int].SemigroupLaws: semigroup.intercalateIntercalates 1ms
[info] + Tree[Int].SemigroupLaws: semigroup.intercalateRepeat1 0ms
[info] + Tree[Int].SemigroupLaws: semigroup.intercalateRepeat2 1ms
[info] + Tree[Int].SemigroupLaws: semigroup.repeat1 1ms
[info] + Tree[Int].SemigroupLaws: semigroup.repeat2 0ms
[info] + Tree[Int].SemigroupLaws: semigroup.reverseCombineAllOption 5ms
[info] + Tree[Int].SemigroupLaws: semigroup.reverseRepeat1 0ms
[info] + Tree[Int].SemigroupLaws: semigroup.reverseRepeat2 1ms
[info] + Tree[Int].SemigroupLaws: semigroup.reverseReverses 0ms
```

## Важные соображения

### Выбор правильного Eq

Использование `Eq.fromUniversalEquals` может быть недостаточным для сложных типов данных. 
Рекомендуется определять более точные экземпляры `Eq`, которые учитывают структурное равенство 
и не полагаются на `toString` или другие недетерминированные представления.

### Качество генерации данных

Эффективность тестирования законов сильно зависит от качества генераторов `Arbitrary`. 
Хорошие генераторы должны покрывать различные крайние случаи и обеспечивать достаточное разнообразие тестовых данных.

### Время выполнения

Тесты законов могут быть ресурсоёмкими, особенно для сложных типов данных и классов типов с большим количеством законов. 
Убедитесь, что ваши генераторы эффективны, а тесты не занимают слишком много времени.

### Тестирование всех классов типов

Для полного покрытия рекомендуется тестировать все классы типов, которые вы реализуете для ваших типов данных. 
Это включает не только основные классы (`Functor`, `Applicative`, `Monad`), 
но и классы из `cats.kernel` (`Semigroup`, `Monoid`), а также специализированные классы (`Traverse`, `Foldable` и т.д.).

---

**Ссылки:**

- [Cats home page][home]

[home]: https://typelevel.org/cats/
