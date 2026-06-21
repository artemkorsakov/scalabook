{%
showPrev = true
prevPage = apply.html
showNext = true
nextPage = bind.html
%}

# Applicative

**Applicative** — это концепция из функционального программирования, расширяющая возможности [функтора](functor.md) 
и добавляющая возможность [применения функции](apply.md), находящейся внутри контекста, к значению, 
которое также находится внутри контекста. 
Это позволяет работать с эффектами (например, вычислениями, которые могут завершиться ошибкой, 
или асинхронными вычислениями) в более удобной и композируемой манере.

**Основные идеи Applicative:**

- **Функтор** позволяет применять функцию к значению внутри контекста (например, `Option`, `List`, `Future`).
- **Applicative** добавляет возможность применять функцию, которая сама находится внутри контекста, к значению, 
  также находящемуся внутри контекста.

`Applicative` полезен, когда у вас есть функция, которая принимает несколько аргументов, 
и каждый из этих аргументов находится внутри контекста (например, `Option`, `Either`, `Future`). 
Вместо того чтобы вручную извлекать значения из контекста и применять функцию, 
`Applicative` позволяет сделать это более элегантно.

**Преимущества Applicative:**

- **Композируемость**: `Applicative` позволяет комбинировать вычисления, которые находятся внутри контекста, 
  без необходимости вручную извлекать значения.
- **Чистота кода**: Код становится более декларативным и легче читаемым.
- **Поддержка эффектов**: `Applicative` работает с различными эффектами, такими как `Option`, `Either`, `Future`, `IO` и другими.

## Примеры

Вот несколько примеров использования **Applicative**:

### Пример 1: Валидация формы

Представьте, что у вас есть форма с несколькими полями (например, имя, email, пароль). 
Каждое поле нужно проверить на корректность (например, email должен содержать "@", пароль должен быть не короче 8 символов). 
Если все поля корректны, вы хотите создать объект пользователя. 
Если есть ошибки, вы хотите собрать их все, а не останавливаться на первой.

- **Контекст**: `Validated` (из библиотеки Cats), который позволяет накапливать ошибки.
- **Применение**: Вы проверяете каждое поле и комбинируете результаты с помощью `Applicative`. 
  Если все поля корректны, создается объект пользователя. Если есть ошибки, возвращается список всех ошибок.

### Пример 2: Параллельные HTTP-запросы

У вас есть несколько независимых HTTP-запросов, которые нужно выполнить параллельно 
(например, получить данные о пользователе, его заказах и истории платежей). 
После выполнения всех запросов вы хотите объединить результаты в один объект.

- **Контекст**: `Future` (асинхронные вычисления).
- **Применение**: Вы запускаете все запросы параллельно и комбинируете их результаты с помощью `Applicative`. 
  Если все запросы успешны, вы объединяете данные. 
  Если хотя бы один запрос завершился ошибкой, весь процесс завершается с ошибкой.

### Пример 3: Работа с конфигурацией

У вас есть конфигурация приложения, которая состоит из нескольких параметров 
(например, URL базы данных, порт сервера, режим отладки). Каждый параметр может быть задан или отсутствовать. 
Вы хотите прочитать все параметры и создать объект конфигурации, только если все параметры присутствуют.

- **Контекст**: `Option` (значение может быть задано или отсутствовать).
- **Применение**: Вы читаете каждый параметр и комбинируете их с помощью `Applicative`. 
  Если все параметры заданы, создается объект конфигурации. Если хотя бы один параметр отсутствует, результат будет `None`.

### Пример 4: Комбинирование списков

У вас есть два списка: один содержит имена, другой — фамилии. Вы хотите создать все возможные комбинации имен и фамилий.

- **Контекст**: `List` (список значений).
- **Применение**: Вы комбинируете два списка с помощью `Applicative`, создавая все возможные пары имен и фамилий.

### Пример 5: Работа с деревьями

У вас есть два дерева, и вы хотите объединить их в одно, применяя функцию к соответствующим узлам.

- **Контекст**: Пользовательский тип данных `Tree`, который представляет собой дерево.
- **Применение**: Вы комбинируете два дерева с помощью `Applicative`, создавая новое дерево, 
  где каждый узел является результатом применения функции к соответствующим узлам исходных деревьев.

### Пример 6: Агрегация данных из нескольких источников

У вас есть несколько источников данных (например, база данных, кэш, внешний API). 
Вы хотите получить данные из всех источников и объединить их в один объект.

- **Контекст**: `IO` (из библиотеки Cats Effect), который представляет собой отложенное вычисление с эффектами.
- **Применение**: Вы получаете данные из каждого источника и комбинируете их с помощью `Applicative`. 
  Если все источники вернули данные, вы объединяете их. 
  Если хотя бы один источник вернул ошибку, весь процесс завершается с ошибкой.

## Формальное определение

`Applicative` расширяет [`Apply`](apply.md) (и [`InvariantApplicative`](invariant-applicative.md)) 
и позволяет работать с несколькими «ящиками».
`Applicative`, дополнительно к операциям `Apply`, реализует операцию `unit` (другие названия: `point`, `pure`),
оборачивающую значение произвольного типа `A` в `Applicative`.

## Код

```dotty
trait Applicative[F[_]] extends Apply[F], InvariantApplicative[F]:
  def unit[A](a: => A): F[A]
  
  override def xunit0[A](a: => A): F[A] = unit(a)

  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  override def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
    fa.map2(fb)((a, b) => (a, b))

  extension [A](fa: F[A])
    def map[B](f: A => B): F[B] =
      ??? // Упражнение

    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      ??? // Упражнение
```

### Комбинаторы Applicative

`Applicative` определяет некоторые комбинаторы:

```dotty
def sequence[A](fas: List[F[A]]): F[List[A]] =
  traverse(fas)(identity)

def traverse[A,B](as: List[A])(f: A => F[B]): F[List[B]] =
  as.foldRight(unit(List[B]()))((a, acc) => f(a).map2(acc)(_ :: _))

def replicateM[A](n: Int, fa: F[A]): F[List[A]] =
  sequence(List.fill(n)(fa))

def product[A, B](fa: F[A], fb: F[B]): F[(A,B)] =
  fa.map2(fb)((a, b) => (a, b))
```

### Законы

Для `Applicative` должны соблюдаться следующие законы (помимо законов родительских классов типов):

> Ниже подразумеваются следующие сигнатуры: `a: A, f: A => B, fa: F[A], fb: F[B], fc: F[C], fab: F[A => B], fbc: F[B => C]`

- Тождественность (identity): `apply[A, A](unit(identity))(fa) == fa` (`unit` тождественной функции - это тождественность для `apply`)
- `unit(a).map(f) == unit(f(a))`
- `fa.map(f) == apply(unit(f))(fa)`
- Гомоморфизм (Homomorphism): `apply(unit(f))(unit(a)) == unit(f(a))`
  (Другими словами, применение идиоматической функции к `unit` такое же, как и к `unit` обычного применения функции.
  Точнее, `unit` — это гомоморфизм из `A` в `F[A]` в отношении применения функции.)
- Обмен (interchange): `apply(fab)(unit(a)) == apply(unit((f: A => B) => f(a)))(fab)`
  (Этот закон, по сути, говорит о том, что `unit` не разрешено воздействовать на любую реализацию аппликативного функтора.
  Если один аргумент, который нужно применить, является `unit`, то другой может появиться в любой позиции.
  Другими словами, не должно иметь значения, когда мы выполняем `unit`.)
- Тождественность слева (left identity): `unit(()).map2(fa)((_, a) => a) == fa`
- Тождественность справа (right identity): `fa.map2(unit(()))((a, _) => a) == fa`
- Ассоциативность (associativity): `fa.product(fb).product(fc) == fa.product(fb.product(fc)).map(assoc)`, где
  `def assoc[A, B, C](p: (A, (B, C))): ((A, B), C) = p match { case (a, (b, c)) => ((a, b), c) }`
- Естественность (naturality): `fa.map2(fb)((a, b) => (f(a), g(b))) == fa.map(f).product(fb.map(g))`
- Композиция (composition):
  `apply(fbc)(apply(fab)(fa)) == apply(apply(apply(unit((g: B => C) => (f: A => B) => g compose f))(fbc))(fab))(fa)`
  (Применить `fab` к `fa`, а затем применить `fbc` к этому — то же самое,
  что применить композицию к `fbc`, затем `fab`, а затем применить составную функцию к `fa`.)

## Упражнения

### Упражнение 2.5.1: Реализация `map` и `map2` через `unit` и `apply`.

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

Аппликативный функтор предоставляет две основные операции: `unit` (поднятие значения в контекст) 
и `apply` (применение функции в контексте к значению в контексте). Используя только эти примитивы,
выразите ковариантное отображение `map` и бинарное отображение `map2`.

Реализуйте методы `map` и `map2` в трейте `Applicative`, используя только `unit` и `apply`.

```dotty
trait Applicative[F[_]] extends Apply[F], InvariantApplicative[F]:
  /** Поднимает значение в контекст аппликативного функтора. */
  def unit[A](a: => A): F[A]

  /** Применяет функцию в контексте к значению в контексте. */
  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  extension [A](fa: F[A])
    /** Упражнение 2.5.1: Реализация `map` через `unit` и `apply`.
      */
    def map[B](f: A => B): F[B] =
      ???

    /** Упражнение 2.5.1: Реализация `map2` через `unit` и `apply`.
      */
    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      ???
```

**Примеры:**

```dotty
// Для Option
val opt = Some(5)
val optMapped = opt.map(_ * 3)             // Some(15)
val optMapped2 = opt.map2(Some(10))(_ + _) // Some(15)

// Для List
val list = List(1, 2, 3)
val listMapped = list.map(_ * 2)         // List(2, 4, 6)
val listMapped2 = list.map2(List(10, 20))(_ + _) // List(11, 21, 12, 22)
```

@:note(🔍 Показать решение)

Для реализации `map` мы поднимаем функцию `f` в контекст с помощью `unit`, получая `F[A => B]`, 
а затем применяем её к `fa` с помощью `apply`. 
Для реализации `map2` мы используем каррирование: 
сначала поднимаем каррированную версию бинарной функции `f.curried: A => B => C` в контекст через `unit`, 
затем дважды применяем `apply`: первый раз к `fa` (получая `F[B => C]`), второй раз к `fb` (получая `F[C]`). 

```dotty
trait Applicative[F[_]] extends Apply[F], InvariantApplicative[F]:
  def unit[A](a: => A): F[A]

  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  extension [A](fa: F[A])
    def map[B](f: A => B): F[B] =
      apply(unit(f))(fa)

    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      apply(apply(unit(f.curried))(fa))(fb)
```

@:@

### Упражнение 2.5.2: Реализация Applicative для типа Option

Реализуйте экземпляр `Applicative` для `Option`:

- `unit` должен поднимать значение в контекст `Option`
- `apply` должен применять функцию внутри `Option` к значению внутри `Option`. 

```dotty
given optionApplicative: Applicative[Option] with
  def unit[A](a: => A): Option[A] = ???
  
  def apply[A, B](fab: Option[A => B])(fa: Option[A]): Option[B] = ???
```

**Примеры:**

```dotty
import Applicative.given

// unit — поднятие значения
unit(42)                  // Some(42)
unit("Hello")             // Some("Hello")
unit(true)                // Some(true)

// apply — применение функции в контексте
val optFunc = Some((x: Int) => x * 2)
apply(optFunc)(Some(21))  // Some(42)
apply(optFunc)(None)      // None

val noneFunc: Option[Int => Int] = None
apply(noneFunc)(Some(21)) // None
apply(noneFunc)(None)     // None
```

@:note(🔍 Показать решение)

В реализации `unit` достаточно обернуть переданное значение в `Some(a)`. 
Для `apply` используется сопоставление с шаблоном: в случае, когда оба аргумента являются `Some(aToB)` и `Some(a)`, 
мы извлекаем функцию и значение, применяем функцию к значению и возвращаем `Some(f(a))`. 
Во всех остальных случаях (когда хотя бы один аргумент — `None`) возвращаем `None`. 
Это соответствует семантике "опционального" вычисления, 
где результат существует только тогда, когда существуют все его компоненты. 

```dotty
given optionApplicative: Applicative[Option] with
  def unit[A](a: => A): Option[A] = Some(a)

  def apply[A, B](fab: Option[A => B])(fa: Option[A]): Option[B] =
    (fab, fa) match
      case (Some(aToB), Some(a)) => Some(aToB(a))
      case _                     => None
```

@:@

### Упражнение 2.5.3: Реализация Applicative для типа List

`List[A]` представляет недетерминированное вычисление, которое может вернуть ноль, одно или множество значений. 
Реализуйте экземпляр `Applicative` для `List`, используя семантику "декартова произведения":

- `unit` должен создавать одноэлементный список, содержащий переданное значение
- `apply` должен применять каждую функцию из списка `fab` к каждому значению из списка `fa`, 
  собирая все результаты в один список (декартово произведение)

```dotty
given listApplicative: Applicative[List] with
  def unit[A](a: => A): List[A] = ???
  
  def apply[A, B](fab: List[A => B])(fa: List[A]): List[B] = ???
```

**Примеры:**

```dotty
import Applicative.given

// unit — создание одноэлементного списка
unit(42)                  // List(42)
unit("Hello")             // List("Hello")
unit(true)                // List(true)

// apply — декартово произведение
val funcs = List((x: Int) => x + 1, (x: Int) => x * 2, (x: Int) => x - 3)
val nums = List(1, 2, 3)

apply(funcs)(nums)
// Результат: List(2, 3, 4,  2, 4, 6,  -2, -1, 0)
// Пошагово:
// (_+1): 1+1=2, 2+1=3, 3+1=4
// (_*2): 1*2=2, 2*2=4, 3*2=6
// (_-3): 1-3=-2, 2-3=-1, 3-3=0

// Пустые списки
apply(List.empty[Int => Int])(List(1, 2, 3))  // Nil
apply(List((x: Int) => x + 1))(List.empty)    // Nil
```

@:note(🔍 Показать решение)

В реализации `unit` достаточно обернуть переданное значение в одноэлементный список `List(a)`. 
Для `apply` используется алгоритм: 
сначала мы проходим по каждой функции `aToB` из списка `fab`, 
а затем для каждой функции применяем `map` ко всем значениям `fa`, преобразуя их с помощью этой функции. 
Результатом будет список, содержащий результаты применения каждой функции к каждому значению. 
Это и есть декартово произведение, где количество элементов результата равно `fab.length * fa.length`.

```dotty
given listApplicative: Applicative[List] with
  def unit[A](a: => A): List[A] = List(a)

  def apply[A, B](fab: List[A => B])(fa: List[A]): List[B] =
    fab match
      case f :: tail =>
        fa.map(f) ++ apply(tail)(fa)
      case Nil => Nil
```

@:@

### Упражнение 2.5.4: Реализация Applicative для типа Either

`Either[E, A]` представляет вычисление, которое может завершиться ошибкой типа `E` (ветка `Left`) 
или успешным результатом типа `A` (ветка `Right`). 
Реализуйте экземпляр `Applicative` для `Either[E, A]` с фиксированным типом ошибки `E`, следуя семантике "короткого замыкания":

- `unit` должен поднимать значение в контекст `Either`
- `apply` должен применять функцию из `Right` к значению из `Right`, возвращая `Right(f(a))`. 
  Если хотя бы один из операндов — `Left`, результатом должен быть первый встреченный `Left` (ошибка прерывает вычисление)

```dotty
given eitherApplicative[E]: Applicative[[x] =>> Either[E, x]] with
  def unit[A](a: => A): Either[E, A] = ???
  
  def apply[A, B](fab: Either[E, A => B])(fa: Either[E, A]): Either[E, B] = ???
```

**Примеры:**

```dotty
import Applicative.given

// unit — поднятие значения
unit(42): Either[String, Int]         // Right(42)
unit("Hello"): Either[String, String] // Right("Hello")
unit(true): Either[String, Boolean]   // Right(true)

// apply — применение функции в контексте
val rightFunc: Either[String, Int => Int] = Right((x: Int) => x * 2)
val rightValue: Either[String, Int] = Right(21)
apply(rightFunc)(rightValue)       // Right(42)

val leftFunc: Either[String, Int => Int] = Left("function error")
val rightValue2: Either[String, Int] = Right(21)
apply(leftFunc)(rightValue2)       // Left("function error")

val rightFunc2: Either[String, Int => Int] = Right((x: Int) => x * 2)
val leftValue: Either[String, Int] = Left("value error")
apply(rightFunc2)(leftValue)       // Left("value error")

val leftFunc2: Either[String, Int => Int] = Left("error1")
val leftValue2: Either[String, Int] = Left("error2")
apply(leftFunc2)(leftValue2)       // Left("error1") (первая ошибка)
```

@:note(🔍 Показать решение)

В реализации `unit` достаточно обернуть переданное значение в `Right(a)`. 
Для `apply` используется сопоставление с шаблоном по паре `(fab, fa)`. 
В случае, когда оба аргумента являются `Right(fx)` и `Right(a)`, мы извлекаем функцию и значение, 
применяем функцию к значению и возвращаем `Right(fx(a))`. 
Если первый аргумент — `Left(l)`, возвращаем его, игнорируя второй аргумент. 
Если первый аргумент — `Right`, но второй — `Left(l)`, возвращаем `Left(l)`. 
Эта реализация соответствует семантике "короткого замыкания": первая встреченная ошибка немедленно возвращается, 
а успешное вычисление продолжается только тогда, когда все компоненты являются `Right`. 

```dotty
given eitherApplicative[E]: Applicative[[x] =>> Either[E, x]] with
  def unit[A](a: => A): Either[E, A] = Right(a)

  def apply[A, B](fab: Either[E, A => B])(fa: Either[E, A]): Either[E, B] =
    (fab, fa) match
      case (Right(fx), Right(a)) => Right(fx(a))
      case (Left(l), _)          => Left(l)
      case (_, Left(l))          => Left(l)
```

@:@

### Упражнение 2.5.5: Реализация Applicative для типа Writer

`Writer[W, A]` представляет вычисление, которое накапливает лог типа `W` (обычно моноид) и возвращает значение типа `A`. 
Реализуйте экземпляр `Applicative` для `Writer[W, A]` с фиксированным типом лога `W`, следуя семантике накопления:

- `unit` должен создавать `Writer` с пустым логом и переданным значением
- `apply` должен последовательно выполнять два вычисления: сначала `fab` (получая функцию и промежуточный лог), 
  затем `fa` (получая значение и финальный лог), после чего комбинировать логи и применять функцию к значению

```dotty
given writerApplicative[W](using monoid: Monoid[W]): Applicative[[x] =>> Writer[W, x]] with
  def unit[A](a: => A): Writer[W, A] = ???
    
  def apply[A, B](fab: Writer[W, A => B])(fa: Writer[W, A]): Writer[W, B] = ???
```

**Примеры:**

```dotty
import Applicative.given
import Monoid.given  // для String, List и других моноидов

// Лог типа String (моноид с конкатенацией)
val w1: Writer[String, Int] = unit(42)
w1.run()  // ("", 42)

val addOne: Writer[String, Int => Int] = Writer(() => ("Adding 1 ", (x: Int) => x + 1))
val value: Writer[String, Int] = Writer(() => ("Value 5 ", 5))

val result = apply(addOne)(value)
result.run()  // ("Adding 1 Value 5 ", 6)

// Лог типа List[String] (моноид с конкатенацией списков)
val log1: Writer[List[String], Int] = Writer(() => (List("Step 1"), 10))
val log2: Writer[List[String], Int => Int] = Writer(() => (List("Step 2: multiply"), (x: Int) => x * 2))

val combined = apply(log2)(log1)
combined.run()  // (List("Step 1", "Step 2: multiply"), 20)
```

@:note(🔍 Показать решение)

В реализации `unit` создаётся `Writer`, который при выполнении возвращает пару из пустого лога (`monoid.empty`) 
и переданного значения `a`. 
В реализации `apply` мы сначала вызываем `fab.run()`, чтобы получить лог `w0` и функцию `aToB`. 
Затем вызываем `fa.run()`, чтобы получить лог `w1` и значение `a`. 
После этого комбинируем логи с помощью `monoid.combine(w0, w1)` и применяем функцию `aToB` к значению `a`. 
Результат оборачивается в новый `Writer` с ленивым вычислением (`() =>`). 
Важно, что порядок выполнения сначала `fab`, затем `fa` определяет порядок накопления логов: сначала логи из `fab`, затем из `fa`. 

```dotty
given writerApplicative[W](using monoid: Monoid[W]): Applicative[[x] =>> Writer[W, x]] with
  def unit[A](a: => A): Writer[W, A] =
    Writer[W, A](() => (monoid.empty, a))

  def apply[A, B](fab: Writer[W, A => B])(fa: Writer[W, A]): Writer[W, B] =
    Writer { () =>
      val (w0, aToB) = fab.run()
      val (w1, a) = fa.run()
      (monoid.combine(w0, w1), aToB(a))
    }
```

@:@

### Упражнение 2.5.6: Реализация Applicative для типа State

`State[S, A]` представляет чистое функциональное вычисление, которое зависит от состояния и может его изменять. 
Это функция из начального состояния `S` в пару `(новое состояние, значение типа A)`. 
Реализуйте экземпляр `Applicative` для `State[S, A]` с фиксированным типом состояния `S`, 
следуя семантике последовательного выполнения:

- `unit` должен создавать `State`, который не изменяет переданное состояние и возвращает значение `a`
- `apply` должен последовательно выполнять два вычисления: сначала `fab` (получая функцию и промежуточное состояние), 
  затем `fa` на обновлённом состоянии (получая значение и финальное состояние), 
  после чего применять функцию к значению и возвращать финальное состояние

**Важно**: состояние должно передаваться через цепочку вычислений строго последовательно, как в конвейере.

```dotty
given stateApplicative[S]: Applicative[[X] =>> State[S, X]] with
  def unit[A](a: => A): State[S, A] = ???
  
  def apply[A, B](fab: State[S, A => B])(
      fa: State[S, A]
  ): State[S, B] = ???
```

**Примеры:**

```dotty
import Applicative.given

// unit — вычисление, не меняющее состояние
val st: State[Int, String] = unit("hello")
st.run(5)  // (5, "hello")

// Базовые операции с состоянием
val increment: State[Int, Int] = State(s => (s + 1, s))
val decrement: State[Int, Int] = State(s => (s - 1, s))
val getState: State[Int, Int] = State(s => (s, s))
val setState: Int => State[Int, Unit] = (n: Int) => State(_ => (n, ()))

// apply — последовательное применение
val addOneFunc: State[Int, Int => Int] = unit((x: Int) => x + 1)
val result1 = apply(addOneFunc)(getState).run(10)
// (10, 11) — состояние не изменилось (10), значение = 10 + 1 = 11

// Более сложная цепочка
val transform: State[Int, Int => Int] = unit((x: Int) => x * 2)
val result2 = apply(transform)(increment).run(5)
// increment(5) -> (6, 5)
// transform применяется к значению 5: 5 * 2 = 10
// Результат: (6, 10)
```

@:note(🔍 Показать решение)

В реализации `unit` создаётся `State`, который принимает состояние `s` и возвращает пару `(s, a)` — 
состояние не изменяется, возвращается переданное значение `a`. 
В реализации `apply` мы создаём новый `State`, который последовательно выполняет два вычисления: 
сначала вызываем `fab.run(s)`, получая промежуточное состояние `s0` и функцию `aToB`. 
Затем вызываем `fa.run(s0)` на обновлённом состоянии, получая финальное состояние `s1` и значение `a`. 
После этого применяем функцию `aToB` к значению `a` и возвращаем пару `(s1, aToB(a))`. 
Важно, что состояние передаётся строго последовательно: результат первого вычисления становится входом для второго. 
Это позволяет компоновать вычисления с состоянием, сохраняя чистоту и предсказуемость. 

```dotty
given stateApplicative[S]: Applicative[[X] =>> State[S, X]] with
    def unit[A](a: => A): State[S, A] =
      State[S, A](s => (s, a))

    def apply[A, B](fab: State[S, A => B])(
        fa: State[S, A]
    ): State[S, B] =
      State { s =>
        val (s0, aToB) = fab.run(s)
        val (s1, a)    = fa.run(s0)
        (s1, aToB(a))
      }
```

@:@

### Упражнение 2.5.7: Композиция двух аппликативных функторов

Важнейшее свойство аппликативных функторов: они замкнуты относительно композиции. 
Если `F` и `G` — аппликативные функторы, то `F[G[A]]` также является аппликативным функтором. 
Это позволяет комбинировать эффекты вложенных контекстов.

В отличие от [монад](monad.md), аппликативные функторы всегда можно скомпоновать, и результат будет аппликативным функтором. 
Это означает, что два последовательно зависимых аппликативных эффекта могут быть объединены в один без потери аппликативной структуры.
Это называется композицией над `Applicative`.

Реализуйте экземпляр `Applicative` для композиции `F[G[A]]`.

```dotty
given compositeApplicative[F[_]: Applicative, G[_]: Applicative]
    : Applicative[[X] =>> F[G[X]]] with
  def unit[A](a: => A): F[G[A]] = ???
  
  def apply[A, B](fab: F[G[A => B]])(fa: F[G[A]]): F[G[B]] = ???
```

**Примеры:**

```dotty
import Applicative.given

// Пример: композиция Option и List
type OptionList[A] = List[Option[A]]

// unit — создание вложенного значения
unit(42): OptionList[Int]  // List(Some(42))

// apply — комбинирует все возможные варианты
val funcs: OptionList[Int => String] = List(Some((x: Int) => x.toString), None, Some((x: Int) => s"num:$x"))
val values: OptionList[Int] = List(Some(1), Some(2), None)

val result = apply(funcs)(values)
// Результат: 
// List(
//   Some("1"), Some("2"), None,        // от Some(_.toString)
//   None, None, None,                  // от None
//   Some("num:1"), Some("num:2"), None // от Some("num:"+_)
// )
```

@:note(🔍 Показать решение)

Реализация композиции использует аппликативные операции внешнего функтора `F` и внутреннего `G`. 
В `unit` мы сначала поднимаем значение `a` в контекст `G` с помощью `Applicative[G].unit(a)`, 
а затем поднимаем результат в контекст `F` с помощью `Applicative[F].unit(...)`. 

В `apply` мы получаем `fab: F[G[A => B]]` и `fa: F[G[A]]`. 
Используя `Applicative[F].map`, мы преобразуем внутреннее `G[A => B]` в функцию `G[A] => G[B]` с помощью `Applicative[G].apply`. 
В результате получаем `F[G[A] => G[B]]`. 
Затем применяем это к `fa` с помощью `Applicative[F].apply`, получая `F[G[B]]`. 
Таким образом, внешний слой `F` управляет композицией, а внутренний `G` отвечает за применение функций к значениям.

```dotty
given compositeApplicative[F[_]: Applicative, G[_]: Applicative]
    : Applicative[[X] =>> F[G[X]]] with
  override def unit[A](a: => A): F[G[A]] = 
    Applicative[F].unit(Applicative[G].unit(a))

  override def apply[A, B](fab: F[G[A => B]])(fa: F[G[A]]): F[G[B]] =
    val applicativeF = Applicative[F]
    val applicativeG = Applicative[G]
    val tmp: F[G[A] => G[B]] = 
      applicativeF.map(fab)(ga2b => applicativeG.apply(ga2b))
    applicativeF.apply(tmp)(fa)
```

@:@

### Упражнение 2.5.8: Композиция двух аппликативных функторов как пара (произведение)

Важное свойство аппликативных функторов: они замкнуты относительно произведения (прямого произведения). 
Если `F` и `G` являются аппликативными функторами, то пара `(F[A], G[A])` также является аппликативным функтором. 
Это позволяет выполнять два независимых аппликативных эффекта одновременно и объединять их результаты в один продукт.

В отличие от композиции (вложенности), произведение представляет собой **параллельную композицию** эффектов: 
оба вычисления выполняются независимо, и их результаты собираются в кортеж. 
Это особенно полезно, когда нужно совместить два разных контекста, 
например, валидацию (`Either`) и логирование (`Writer`), 
или опциональность (`Option`) и недетерминизм (`List`).

Реализуйте экземпляр `Applicative` для произведения `(F[A], G[A])`.

```dotty
given tupleApplicative[F[_]: Applicative, G[_]: Applicative]
    : Applicative[[X] =>> (F[X], G[X])] with
  type FG[A] = (F[A], G[A])

  def unit[A](a: => A): FG[A] = ???
  
  def apply[A, B](fab: FG[A => B])(fa: FG[A]): FG[B] = ???
```

**Примеры:**

```dotty
import Applicative.given

// Пример 1: Произведение Option и List
type OptionList[A] = (Option[A], List[A])

// unit — создание пары из двух единичных контекстов
unit(42): OptionList[Int]  // (Some(42), List(42))
unit("hello"): OptionList[String]  // (Some("hello"), List("hello"))

// apply — покомпонентное применение
val fab: (Option[Int => String], List[Int => String]) = 
  (Some((x: Int) => x.toString), List((x: Int) => x + 1, (x: Int) => x * 2))
val fa: (Option[Int], List[Int]) = (Some(5), List(1, 2, 3))

val result = apply(fab)(fa)
// Результат: (Some("5"), List(2, 3, 4, 2, 4, 6))
// Option часть: Some(5.toString) = Some("5")
// List часть: декартово произведение: (1+1=2, 2+1=3, 3+1=4, 1*2=2, 2*2=4, 3*2=6)
```

@:note(🔍 Показать решение)

Реализация произведения двух аппликативных функторов выполняется покомпонентно. 
В методе `unit` мы создаём пару из результатов вызова `unit` для каждого функтора: `(Applicative[F].unit(a), Applicative[G].unit(a))`. 

В методе `apply` мы получаем пару функций `fab = (ff: F[A => B], fg: G[A => B])` и пару значений `fa = (faF: F[A], faG: G[A])`. 
Результатом будет пара, где первая компонента — результат применения `ff` к `faF` с помощью `Applicative[F].apply`, 
а вторая компонента — результат применения `fg` к `faG` с помощью `Applicative[G].apply`. 
Таким образом, оба вычисления выполняются независимо, и их результаты объединяются в кортеж. 
Это демонстрирует, что аппликативные функторы замкнуты относительно произведения, что позволяет комбинировать независимые эффекты параллельно.

```dotty
given tupleApplicative[F[_]: Applicative, G[_]: Applicative]
    : Applicative[[X] =>> (F[X], G[X])] with
  type FG[A] = (F[A], G[A])

  def unit[A](a: => A): FG[A] =
    (Applicative[F].unit(a), Applicative[G].unit(a))

  def apply[A, B](fab: FG[A => B])(fa: FG[A]): FG[B] =
    (
     Applicative[F].apply(fab._1)(fa._1), 
     Applicative[G].apply(fab._2)(fa._2)
    )
```

@:@

Произведение (`(F[X], G[X])`) и композиция (`F[G[A]]`) позволяют комбинировать идиоматические (аппликативные) вычисления двумя разными способами;
Обычно они называются параллельной и последовательной композицией соответственно.
Тот факт, что можно составлять аппликативы, и они остаются аппликативными, очень полезен.

### Упражнение 2.5.9: Эквивалентность определений Applicative — реализация `map` и `apply` через `map2`

Аппликативный функтор может быть определён различными, но эквивалентными способами. 
Обычно `Applicative` определяется через `unit` и `apply`, 
но существует альтернативное определение через `unit` и `map2`. 
Покажите эквивалентность этих подходов, реализовав `map` и `apply` через `unit` и `map2`.

В данном упражнении предполагается, что `map2` уже реализован, и вы должны выразить `map` и `apply` через него.

```dotty
trait Applicative[F[_]] extends Apply[F], InvariantApplicative[F]:
  /** Поднимает значение в контекст аппликативного функтора. */
  def unit[A](a: => A): F[A]

  /** Бинарное отображение — комбинирует два значения 
    * в контексте с помощью бинарной функции. 
    */
  extension [A](fa: F[A])
    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C]

  /** Упражнение 2.5.9: Реализация `map` через `unit` и `map2`. */
  extension [A](fa: F[A])
    def mapViaUnitAndMap2[B](f: A => B): F[B] =
      ???

  /** Упражнение 2.5.9: Реализация `map` через `unit` и `map2`. */
  def applyViaUnitAndMap2[A, B](fab: F[A => B])(fa: F[A]): F[B] =
    ???
```

**Примеры:**

```dotty
import Applicative.given

// Для Option
val opt = Some(5)
val optMapped = opt.mapViaUnitAndMap2(_ * 3)  // Some(15)
val optFunc = Some((x: Int) => x * 2)
val optApplied = applyViaUnitAndMap2(optFunc)(opt)  // Some(10)
```

@:note(🔍 Показать решение)

Для реализации `mapViaUnitAndMap2` мы используем `map2` с `unit(())` в качестве второго аргумента. 
Бинарная функция `(a, _) => f(a)` игнорирует второй аргумент (единицу) и применяет `f` к первому аргументу. 
Таким образом, `fa.map2(unit(()))((a, _) => f(a))` эффективно применяет `f` к значению внутри `fa`, 
игнорируя пустой контекст `unit(())`. 
Это работает, потому что `map2` комбинирует два контекста, 
а `unit(())` представляет собой "пустой" эффект, который не влияет на результат.

Для реализации `applyViaUnitAndMap2` мы используем `fab.map2(fa)((f, a) => f(a))`. 
Здесь `fab` содержит функцию в контексте, `fa` содержит значение в контексте, 
а бинарная функция `(f, a) => f(a)` просто применяет функцию к аргументу. 
`map2` заботится о правильной комбинации эффектов обоих контекстов, 
применяя функцию `f` к значению `a` внутри соответствующих контекстов. 
Это демонстрирует, что `apply` может быть выражен через `map2`, 
что подтверждает эквивалентность двух альтернативных определений аппликативного функтора.

```dotty
extension [A](fa: F[A])
  def mapViaUnitAndMap2[B](f: A => B): F[B] =
    fa.map2(unit(()))((a, _) => f(a))

def applyViaUnitAndMap2[A, B](fab: F[A => B])(fa: F[A]): F[B] =
  fab.map2(fa)((f, a) => f(a))
```

@:@

## Схема

```mermaid
classDiagram
    class InvariantFunctor~A~{
        +imap[B](fa: F[A], f: A => B, g: B => A) F[B]
    }
    class Semigroupal~A~{
        +product[A, B](fa: F[A], fb: F[B]) F[Tuple2[A, B]]
    }
    class Functor~A~{
        +map[B](fa: F[A], f: A => B) F[B]
    }
    InvariantFunctor <|-- Functor    
    class InvariantApplicative~A~{
        +xunit0[A](a: => A) F[A]
    }
    InvariantFunctor <|-- InvariantApplicative   
    class Apply~A~{
        +apply[A, B](fab: F[A => B])(fa: F[A]) F[B]
    }
    Functor <|-- Apply
    Semigroupal <|-- Apply
    class Applicative~A~{
        +unit[A](a: => A) F[A]
    }
    Apply <|-- Applicative
    InvariantApplicative <|-- Applicative
```

## Реализация в библиотеках

### Реализация в Cats

```dotty
import cats.*, cats.data.*, cats.syntax.all.*

Applicative[List].pure(1)    // List(1)
Applicative[Option].pure(1)  // Some(1)
```

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*

// ... Все операции родителей

1.point[List]  // List(1)
1.η[List]      // List(1)
```

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Классические абстракции используемые в ФП](https://habr.com/ru/articles/896660/)
- [Algebird](https://twitter.github.io/algebird/typeclasses/applicative.html)
- [Applicative Programming with Effects](https://www.staff.city.ac.uk/~ross/papers/Applicative.html)
- [Cats](https://typelevel.org/cats/typeclasses/applicative.html)
- [Functional Programming in Scala, Second Edition](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Functional Programming in Scala, Second Edition, Wiki](https://github.com/fpinscala/fpinscala/wiki/Chapter-12:-Applicative-and-traversable-functors)
- [Herding Cats](http://eed3si9n.com/herding-cats/Applicative.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Applicative.html)
- [Scala with Cats](https://scalawithcats.com/dist/scala-with-cats.html#apply-and-applicative)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Applicative.html)
- [SKB – Scala Applicative](https://leobenkel.com/2021/01/skb-scala-applicative/)
- [Tour of Scala](https://tourofscala.com/scala/applicative)

[sample]: https://gitverse.ru/scalabook/typeclasses
