# Profunctor

`Profunctor` — это класс типов в функциональном программировании, 
который обобщает понятие [**бифунктора**](../bifunctor/bifunctor.md) (функтора двух аргументов) для случая, 
когда первый аргумент **контравариантен**, а второй **ковариантен**. 
Это означает, что `Profunctor` может преобразовывать входные данные (первый аргумент) 
и выходные данные (второй аргумент) независимо друг от друга.

Основные операции, предоставляемые `Profunctor`:

- **`dimap`**: Преобразует входные и выходные данные с помощью двух функций.
- **`lmap`** (или **`mapfst`**): Преобразует только входные данные.
- **`rmap`** (или **`mapsnd`**): Преобразует только выходные данные.

**Краткие примеры**

- **Оптики (Lens)**:
    - Есть Lens, которая работает с полем `age` в структуре `Person`.
    - С помощью `Profunctor` можно преобразовать Lens так, чтобы она работала с полем `birthYear`, вычисляя возраст на лету.

## Формальное определение

Пусть \\(P\\) — это профунктор, где:

- \\(P: C^\text{op} \times D \to \text{Set}\\) — это функтор из произведения категорий \\(C^\text{op}\\) 
  (противоположной категории \\(C\\)) и \\(D\\) в категорию множеств.
- \\(C^\text{op}\\) — это категория, где морфизмы направлены в обратную сторону (контравариантность).
- \\(D\\) — это категория, где морфизмы направлены в обычную сторону (ковариантность).

Класс типов `Profunctor` формально определяется следующим образом:

- **Объекты**: Типы \\(A, B, C, D, \dots\\), которые являются объектами категорий \\(C\\) и \\(D\\).
- **Морфизмы**: Функции (или стрелки) \\(f: A \to B\\) и \\(g: C \to D\\), где:
    - \\(f\\) — контравариантный морфизм (преобразует входные данные).
    - \\(g\\) — ковариантный морфизм (преобразует выходные данные).
- **Операция `dimap`**: Для любых двух морфизмов \\(f: A \to B\\) и \\(g: C \to D\\) существует морфизм:
  \\(\text{dimap}(f, g): P\[B, C\] \to P\[A, D\]\\),
  который преобразует профунктор \\(P\[B, C\]\\) в профунктор \\(P\[A, D\]\\).

### Законы `Profunctor`

Класс типов `Profunctor` должен удовлетворять следующим законам:

- **Тождественность**:
  \\(\text{dimap}(\text{id}\_A, \text{id}\_B) = \text{id}_{P\[A, B\]}\\).
  Это означает, что применение `dimap` к тождественным морфизмам возвращает исходный профунктор.
- **Композиция**:
  \\(\text{dimap}(f \circ g, h \circ k) = \text{dimap}(g, h) \circ \text{dimap}(f, k)\\).
  Это означает, что `dimap` сохраняет композицию морфизмов.

## Определение в виде кода на Scala

```dotty
trait Profunctor[=>:[_, _]]:
  /** Contramap on `A`. */
  def mapfst[A, B, C](fab: A =>: B)(f: C => A): C =>: B

  /** Functor map on `B`. */
  def mapsnd[A, B, C](fab: A =>: B)(f: B => C): A =>: C

  /** Functor map on `A` and `B`. */
  def dimap[A, B, C, D](fab: A =>: B)(f: C => A)(g: B => D): C =>: D =
    mapsnd(mapfst(fab)(f))(g)
```

Здесь:

- `=>:[_, _]` — это тип, представляющий профунктор (например, функция `A => B`).
- Метод `dimap` принимает профунктор \\(P\[A, B\]\\) и две функции:
  - \\(f: C \to A\\) — контравариантное преобразование входных данных.
  - \\(g: B \to D\\) — ковариантное преобразование выходных данных.
- Результат — новый профунктор \\(P\[C, D\]\\).

### Связь с другими классами типов

`Profunctor` позволяет реализовать [InvariantFunctor](../monad/invariant-functor.md),
[Functor](../monad/functor.md)
и [ContravariantFunctor](../monad/contravariant-functor.md).


```dotty
trait Profunctor[=>:[_, _]]:
  ...

  protected[arrow] trait SndCovariant[C] extends Functor[[X] =>> =>:[C, X]]:
    extension [A](fa: C =>: A) override def map[B](f: A => B): C =>: B = mapsnd(fa)(f)

  def invariantFunctor: InvariantFunctor[[X] =>> X =>: X] =
    new InvariantFunctor[[X] =>> X =>: X]:
      extension [A](fa: A =>: A)
        override def imap[B](f: A => B, g: B => A): B =>: B = mapsnd(mapfst(fa)(g))(f)

  def covariantInstance[C]: Functor[[X] =>> =>:[C, X]] =
    new SndCovariant[C] {}

  def contravariantInstance[C]: ContravariantFunctor[[X] =>> =>:[X, C]] =
    new ContravariantFunctor[[X] =>> =>:[X, C]]:
      override def cmap[A, B](fa: B =>: C)(f: A => B): A =>: C = mapfst(fa)(f)
```

## Упражнения

### Упражнение 4.4.1: Реализация Profunctor для Function1 (обычные функции)

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

Реализуйте экземпляр `Profunctor` для `Function1`, определив:
- `mapfst` — преобразование входа через композицию слева
- `mapsnd` — преобразование выхода через композицию справа

```dotty
given Profunctor[Function1] with
  override def mapfst[A, B, C](fab: A => B)(f: C => A): C => B = ???
  override def mapsnd[A, B, C](fab: A => B)(f: B => C): A => C = ???
```

**Примеры:**

```dotty
import Profunctor.given

// Базовые функции
val len: String => Int = _.length        // String -> Int
val inc: Int => Int = _ + 1              // Int -> Int
val toString: Int => String = _.toString // Int -> String
val parseInt: String => Int = _.toInt    // String -> Int
val mul2: Int => Int = _ * 2             // Int -> Int

// mapfst — преобразование входа (контравариантное)
// len принимает String, хотим получить функцию Int => Int
val lenOfStringFromInt = mapfst(len)(toString)
lenOfStringFromInt(42)  // len(toString(42)) = len("42") = 2

val incAfterParse = mapfst(inc)(parseInt)
incAfterParse("10")  // inc(parseInt("10")) = inc(10) = 11

// mapsnd — преобразование выхода (ковариантное)
// inc возвращает Int, хотим получить String
val incAndToString = mapsnd(inc)(toString)
incAndToString(5)  // toString(inc(5)) = toString(6) = "6"

val lenAndMul2 = mapsnd(len)(mul2)
lenAndMul2("hello")  // mul2(len("hello")) = mul2(5) = 10
```

@:note(🔍 Показать решение)

Реализация `mapfst` использует композицию `f andThen fab`: 
сначала применяется `f: C => A`, преобразуя вход от `C` к `A`, затем исходная функция `fab: A => B`. 
Это контравариантное отображение, потому что направление стрелки входного типа переворачивается. 
Реализация `mapsnd` использует композицию `fab andThen f`: 
сначала применяется исходная функция `fab: A => B`, затем `f: B => C` преобразует выход. 
Это ковариантное отображение, потому что направление стрелки выходного типа сохраняется. 
Метод `dimap` комбинирует обе операции и уже реализован в трейте `Profunctor` через `mapfst` и `mapsnd`.

```dotty
given Profunctor[Function1] with
  override def mapfst[A, B, C](fab: A => B)(f: C => A): C => B = f andThen fab

  override def mapsnd[A, B, C](fab: A => B)(f: B => C): A => C = fab andThen f
```

@:@

### Упражнение 4.4.2: Реализация экземпляра Profunctor для Kleisli на основе Functor

В этом упражнении вам предстоит реализовать экземпляр класса типов `Profunctor` для типа `Kleisli[F, A, B]`, 
используя возможность отображения через `Functor[F]`.

```dotty
given kleisliProfunctor[F[_]: Functor]
    : Profunctor[[A, B] =>> Kleisli[F, A, B]]
with
  override def mapfst[A, B, C](fab: Kleisli[F, A, B])(f: C => A)
      : Kleisli[F, C, B] =
    ???

  override def mapsnd[A, B, C](fab: Kleisli[F, A, B])(f: B => C)
      : Kleisli[F, A, C] =
    ???
```

@:note(🔍 Показать решение)

Решение использует `Functor[F]` для получения доступа к `map`, который позволяет преобразовывать значения внутри контекста `F`. 
Для `mapfst` создается новая стрелка, которая сначала применяет функцию `f: C => A` к входному значению `c`, 
затем передает результат в исходную стрелку `fab`. 
Для `mapsnd` создается новая стрелка, которая применяет исходную стрелку `fab` к значению `a`,
получая `F[B]`, а затем с помощью `map` преобразует результат с помощью функции `f: B => C`.

```dotty
given kleisliProfunctor[F[_]: Functor]
    : Profunctor[[A, B] =>> Kleisli[F, A, B]]
with
  override def mapfst[A, B, C](fab: Kleisli[F, A, B])(f: C => A)
      : Kleisli[F, C, B] =
    Kleisli[F, C, B](c => fab.run(f(c)))

  override def mapsnd[A, B, C](fab: Kleisli[F, A, B])(f: B => C)
      : Kleisli[F, A, C] =
    Kleisli[F, A, C](a => fab.run(a).map(f))
```

@:@

### Упражнение 4.4.3: Реализация экземпляра Profunctor для Cokleisli на основе Functor

В этом упражнении вам предстоит реализовать экземпляр класса типов `Profunctor` для типа `Cokleisli[F, A, B]`, 
используя возможность отображения через `Functor[F]`.

```dotty
given cokleisliProfunctor[F[_]: Functor]
    : Profunctor[[A, B] =>> Cokleisli[F, A, B]] with
  override def mapfst[A, B, C](fab: Cokleisli[F, A, B])(f: C => A)
      : Cokleisli[F, C, B] =
    ???

  override def mapsnd[A, B, C](fab: Cokleisli[F, A, B])(f: B => C)
      : Cokleisli[F, A, C] =
    ???
```

@:note(🔍 Показать решение)

Решение использует `Functor[F]` для получения доступа к `map`, который позволяет преобразовывать значения внутри контекста `F`. 
Для `mapfst` создается новая стрелка, которая сначала применяет `map(f)` к входному контексту `F[C]`, преобразуя его в `F[A]`, 
а затем передает результат в исходную стрелку `fab`. 
Для `mapsnd` создается новая стрелка, которая применяет исходную стрелку `fab` к контексту `F[A]`, 
получая `B`, а затем применяет функцию `f: B => C` к результату.

```dotty
given cokleisliProfunctor[F[_]: Functor]
    : Profunctor[[A, B] =>> Cokleisli[F, A, B]] with
  override def mapfst[A, B, C](fab: Cokleisli[F, A, B])(f: C => A)
      : Cokleisli[F, C, B] =
    Cokleisli[F, C, B](fc => fab.run(fc.map(f)))

  override def mapsnd[A, B, C](fab: Cokleisli[F, A, B])(f: B => C)
      : Cokleisli[F, A, C] =
    Cokleisli[F, A, C](fa => f(fab.run(fa)))
```

@:@

## Схема

```mermaid
classDiagram
    class Profunctor~ =>: ~{
        +mapfst[A, B, C](fab: A =>: B)(f: C => A) C =>: B
        +mapsnd[A, B, C](fab: A =>: B)(f: B => C) A =>: C
        +dimap[A, B, C, D](fab: A =>: B)(f: C => A)(g: B => D) C =>: D
    }
```

## Реализация в библиотеках

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*

// ... Все операции родителей
```

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Cats](https://typelevel.org/cats/typeclasses/arrow.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Arrow.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Profunctor.html)
- The Dao of Functional Programming - Bartosz Milewski:
    - [Дао функционального программирования, глава 6.2](https://henrychern.wordpress.com/2022/09/23/milewski-%d0%b4%d0%b0%d0%be-%d1%84%d1%83%d0%bd%d0%ba%d1%86%d0%b8%d0%be%d0%bd%d0%b0%d0%bb%d1%8c%d0%bd%d0%be%d0%b3%d0%be-%d0%bf%d1%80%d0%be%d0%b3%d1%80%d0%b0%d0%bc%d0%bc%d0%b8%d1%80%d0%be%d0%b2%d0%b0/)
    - [The Dao of Functional Programming, 6.2](https://github.com/BartoszMilewski/Publications/blob/master/TheDaoOfFP/DaoFP.pdf)

[sample]: https://gitverse.ru/scalabook/typeclasses
