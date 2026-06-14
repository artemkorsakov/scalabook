{%
showPrev = true
prevPage = monad-transformer.html
showNext = true
nextPage = applicative-error.html
%}

# Свободная монада

Свободная монада — это способ структурировать вычисления, 
позволяющий отделить описание программы от её интерпретации. 
Она представляет собой абстракцию, инкапсулирующую последовательность операций в виде данных, 
которые можно интерпретировать различными способами.

Свободная монада обеспечивает:

- абстрактное синтаксическое дерево (AST - _abstract syntax tree_) для выражения монадических операций
- API для написания интерпретаторов, которые придают значение этому AST

**Основные идеи:**

- **Описание вычислений**: Свободная монада позволяет описать последовательность операций как данные, не выполняя их сразу.
- **Интерпретация**: Эти данные могут быть интерпретированы позже, в зависимости от контекста или требований.
- **Гибкость**: Свободная монада полезна, когда нужно поддерживать несколько интерпретаций для одного и того же набора операций.

**Где применяется:**

- **DSL (Domain-Specific Languages)**: Свободная монада часто используется для создания встроенных DSL, 
  где нужно отделить описание операций от их выполнения.
- **Тестирование**: В тестах можно использовать одну интерпретацию для реального выполнения операций, 
  а другую — для симуляции или мокирования.
- **Асинхронные вычисления**: Свободная монада может быть использована для описания асинхронных операций, 
  которые затем интерпретируются в зависимости от контекста (например, в разных средах выполнения).
- **Логирование и отладка**: Можно добавить интерпретацию, которая логирует каждую операцию, 
  не изменяя основную логику программы.

## Формальное определение

Свободная монада — это конструкция в теории категорий и функциональном программировании, 
позволяющая представить последовательность операций (вычислений) в виде структуры данных, 
которую можно интерпретировать различными способами. 
Формально, свободная монада строится на основе функтора и представляет собой монаду, 
которая "свободно" генерируется этим функтором.

Пусть \\(F\\) — это функтор (в смысле теории категорий). 
Тогда **свободная монада** \\(\text{Free}\[F\]\\), порождённая функтором \\(F\\), 
определяется как структура, которая удовлетворяет следующим свойствам:

- **Конструктор**:
    - \\(\text{Free}\[F\]\\) может быть представлен как сумма типов:
      \\(\text{Free}\[F, A\] = A + F(\text{Free}\[F, A\])\\).
      Это означает, что свободная монада либо возвращает значение типа \\(A\\) (завершённое вычисление), 
      либо продолжает вычисление, применяя функтор \\(F\\) к следующему шагу.
- **Единица (unit)**:
    - Существует естественное преобразование \\(\eta: A \to \text{Free}\[F, A\]\\), 
      которое вкладывает значение типа \\(A\\) в свободную монаду. Это соответствует завершённому вычислению.
- **Копроизведение (join)**:
    - Существует естественное преобразование \\(\mu: \text{Free}\[F, \text{Free}\[F, A\]\] \to \text{Free}\[F, A\]\\), 
      которое "сплющивает" вложенные свободные монады. Это позволяет комбинировать последовательные вычисления.
- **Универсальное свойство**:
    - Для любой монады \\(M\\) и естественного преобразования \\(\alpha: F \to M\\) существует 
      уникальный гомоморфизм монад \\(\text{Free}\[F\] \to M\\), который делает диаграмму коммутативной. 
      Это означает, что свободная монада является "наиболее общей" монадой, порождённой функтором \\(F\\).

Свободная монада позволяет представить вычисления как дерево, где каждый узел — это операция, 
определённая функтором \\(F\\), а листья — это значения типа \\(A\\). 
Это дерево можно интерпретировать различными способами, в зависимости от контекста.

Если \\(F\\) — это функтор, представляющий операцию "запроса к базе данных", 
то \\(\text{Free}\[F, A\]\\) — это последовательность таких запросов, 
которые можно интерпретировать как реальные запросы к базе данных, симуляции для тестирования или даже логирование.

## Код

```dotty
trait Natural[F[_], G[_]]:
  def transform[A](a: F[A]): G[A]
  
sealed trait Free[F[_], A]

final case class UnitF[F[_], A](a: A) extends Free[F, A]

final case class LiftF[F[_], A](fn: F[A]) extends Free[F, A]

final case class FlatMap[F[_], A, B](a: Free[F, A], fx: A => Free[F, B])
    extends Free[F, B]

object Free:
  given freeFunctor[F[_]]: Functor[[X] =>> Free[F, X]] with
    extension [A](as: Free[F, A])
      override def map[B](f: A => B): Free[F, B] =
        FlatMap(as, a => UnitF(f(a)))

  given freeMonad[F[_]]: Monad[[X] =>> Free[F, X]] with
    override def unit[A](a: => A): Free[F, A] =
      UnitF(a)

    extension [A](fa: Free[F, A])
      override def flatMap[B](f: A => Free[F, B]): Free[F, B] =
        FlatMap(fa, f)

  def liftF[F[_], A](a: F[A]): Free[F, A] = LiftF[F, A](a)

  def foldF[F[_], M[_]: Monad as monad, A](
      fa: Free[F, A]
  )(
      trans: Natural[F, M]
  ): M[A] =
    fa match
      case UnitF(a)             => monad.unit(a)
      case LiftF(fn)            => trans.transform(fn)
      case FlatMap(UnitF(a), f) => foldF(f(a))(trans)
      case FlatMap(LiftF(a), f) =>
        monad.flatMap(trans.transform(a)) { aa => foldF(f(aa))(trans) }
      case FlatMap(FlatMap(a, f), g) =>
        foldF(FlatMap(a, aa => FlatMap(f(aa), g)))(trans)
end Free  
```

### Алгебраический тип Free

```dotty
sealed trait Free[F[_], A]
```

`Free[F, A]` может быть в одном из трёх состояний:

#### `UnitF` — чистое значение

```dotty
final case class UnitF[F[_], A](a: A) extends Free[F, A]
```

- Представляет завершённое вычисление, возвращающее значение `a`
- Аналог `pure` / `unit` в монадах
- Не содержит эффектов `F`

#### `LiftF` — поднятый эффект

```dotty
final case class LiftF[F[_], A](fn: F[A]) extends Free[F, A]
```

- Поднимает эффект `F[A]` в свободную монаду
- Представляет "сырую" операцию из функтора `F`
- Например, если `F` — это `Option`, то `LiftF(Some(42))` или `LiftF(None)`

#### `FlatMap` — последовательная композиция

```dotty
final case class FlatMap[F[_], A, B](
    a: Free[F, A],
    fx: A => Free[F, B]
) extends Free[F, B]
```

- Представляет последовательное выполнение двух вычислений
- Сначала выполняется `a` (получая `A`), затем применяется `fx` к результату
- Это строительный блок для `flatMap` / `bind`

Это представление кодирует **монадические законы как данные**:

- `UnitF` — это `pure`
- `FlatMap` — это `flatMap`
- `LiftF` — это поднятие эффекта из исходного функтора

### Реализация Functor

```dotty
given freeFunctor[F[_]]: Functor[[X] =>> Free[F, X]] with
  extension [A](as: Free[F, A])
    override def map[B](f: A => B): Free[F, B] =
      FlatMap(as, a => UnitF(f(a)))
```

**Как это работает:**

1. `map` преобразует значение внутри `Free[F, A]`
2. Использует `FlatMap` для последовательного выполнения:
    - Сначала выполняем `as`
    - Затем применяем функцию, которая заворачивает `f(a)` в `UnitF`
3. Это стандартная реализация `map` через `flatMap` и `unit`

### Реализация Monad

```dotty
given freeMonad[F[_]]: Monad[[X] =>> Free[F, X]] with
  override def unit[A](a: => A): Free[F, A] = UnitF(a)

  extension [A](fa: Free[F, A])
    override def flatMap[B](f: A => Free[F, B]): Free[F, B] =
      FlatMap(fa, f)
```

**Обратите внимание:**

- `unit` просто создаёт `UnitF(a)`
- `flatMap` буквально создаёт `FlatMap(fa, f)` **без выполнения**!

Это ключевое отличие: в данном случае `flatMap` **не вычисляет** результат, а только **строит структуру данных**.

### Подъём эффекта в Free

```dotty
def liftF[F[_], A](a: F[A]): Free[F, A] = LiftF[F, A](a)
```

- Превращает любой эффект `F[A]` в `Free[F, A]`
- Позволяет использовать операции из `F` внутри свободной монады

## Сворачивание Free-монады (интерпретатор)

Самое важное — `foldF` — интерпретатор, который превращает структуру данных `Free[F, M]` в реальные вычисления в монаде `M`.

```dotty
def foldF[F[_], M[_]: Monad as monad, A](
    fa: Free[F, A]
)(
    trans: Natural[F, M]
): M[A] = fa match { ... }
```

**`Natural[F, M]`** — это естественное преобразование:

```dotty
trait Natural[F[_], G[_]]:
  def transform[A](a: F[A]): G[A]
```

Оно умеет превращать `F[A]` в `M[A]` для любого типа `A`.

### Разбор случаев

#### Случай 1: UnitF (чистое значение)

```dotty
case UnitF(a) => monad.unit(a)
```

Просто возвращаем `a` в монаде `M`.

#### Случай 2: LiftF (поднятый эффект)

```dotty
case LiftF(fn) => trans.transform(fn)
```

Применяем естественное преобразование к `fn: F[A]`, получая `M[A]`.

#### Случай 3: FlatMap с UnitF слева

```dotty
case FlatMap(UnitF(a), f) => foldF(f(a))(trans)
```

Если первое вычисление — чистое значение `a`, просто продолжаем с `f(a)`.

#### Случай 4: FlatMap с LiftF слева — выполнение эффекта

```dotty
case FlatMap(LiftF(a), f) =>
  monad.flatMap(trans.transform(a)) { aa => foldF(f(aa))(trans) }
```

Этот случай — **точка выполнения эффекта** в свободной монаде. 
Когда мы встречаем конструкцию вида `LiftF(a) >>= f` (эффект `a`, за которым следует продолжение `f`), 
мы должны реально выполнить этот эффект в целевой монаде `M`.
Без этого правила `foldF` никогда бы не выполнил ни одного эффекта — 
он просто рекурсивно обходил бы структуру данных, не производя никаких вычислений.

**Один шаг выполнения:**

1. **Преобразуем эффект**:

   ```dotty
   trans.transform(a)   // F[A] => M[A]
   ```

   Используем естественное преобразование, чтобы перевести эффект из исходного функтора `F` в целевую монаду `M`.

2. **Выполняем один шаг монадического связывания**:
   ```dotty
   monad.flatMap(...) { aa => foldF(f(aa))(trans) }
   ```

    - `monad.flatMap` выполняет эффект `M[A]` (первый шаг)
    - Полученное значение `aa` подставляется в продолжение `f(aa)`
    - Затем рекурсивно сворачивается результат `f(aa)`

**Почему это безопасно для стека?**

```dotty
// Визуализация выполнения:

Исходное:     FlatMap(LiftF(a), f)
                ↓
Шаг 1:        monad.flatMap(trans.transform(a)) { aa => foldF(f(aa)) }
                ↓
Шаг 2:        Выполняется один эффект в M
                ↓
Шаг 3:        foldF рекурсивно обрабатывает продолжение f(aa)
```

**Ключевая особенность** — здесь **нет рекурсивного вызова `foldF` внутри `foldF` до выполнения эффекта**:

- Сначала выполняется `trans.transform(a)` (один шаг)
- Затем `monad.flatMap` вызывает переданную функцию **после** получения результата
- Внутри этой функции снова вызывается `foldF`, но уже на сокращённой структуре

**Почему это важно для безопасности стека:**

1. **Один эффект за раз**: Мы выполняем ровно один эффект `F[A]` за один рекурсивный шаг
2. **Нет вложенных flatMap**: После выполнения эффекта мы вызываем `foldF` на **следующем** куске программы
3. **Хвостовая рекурсия**: Структура вызовов не углубляется, так как каждый вызов `foldF` происходит **после** выполнения текущего шага

**Стек вызовов в правильной реализации:**

```
foldF(FlatMap(LiftF(a1), f1))
  → trans.transform(a1)        // выполняем эффект 1
  → foldF(f1(aa1))             // рекурсия на следующем шаге
    → ...                      // стек не растёт!
```

Если бы мы попытались выполнить все эффекты за один раз (например, через `traverse` или цикл), то:

- Либо потеряли бы ленивость (вычисления выполнялись бы до того, как мы готовы)
- Либо получили бы глубокую рекурсию (каждый `flatMap` добавлял бы фрейм в стек)

#### Случай 5: FlatMap с FlatMap слева (ассоциативность) — критический для безопасности стека!

```dotty
case FlatMap(FlatMap(a, f), g) =>
  foldF(FlatMap(a, aa => FlatMap(f(aa), g)))(trans)
```

**Проблема, которую решает этот случай:**

Без этого правила рекурсивный `foldF` мог бы создать глубоко вложенную структуру вызовов вида:

```
FlatMap(FlatMap(FlatMap(..., f1), f2), f3)
```

Наивная рекурсивная обработка привела бы к стековому переполнению для длинных цепочек вычислений.

**Решение — один шаг переписывания (как в [прыжках на батуте](../../fp/external-effects/stack_overflow_error.md)):**

1. **Исходное выражение**: `((a >>= f) >>= g)`
    - Сначала выполняется `a`, затем `f`, затем `g`

2. **Один шаг трансформации**:

   ```dotty
   FlatMap(a, aa => FlatMap(f(aa), g))
   //    ↑                      ↑
   //    |                      └── применение g после f(aa)
   //    └── исходное вычисление
   ```

   Теперь: `(a >>= (aa => f(aa) >>= g))`

3. **Почему это безопасно?**
    - Мы выполняем **ровно один шаг** переписывания
    - Левый `FlatMap` "поднимается" наверх, вложенность уменьшается
    - Затем `foldF` рекурсивно продолжает обрабатывать уже упрощённую структуру
    - Глубина рекурсии ограничена, переполнения стека не происходит

**Визуализация трансформации:**

```
До:                    После:
                            FlatMap
    FlatMap                  /   \
     /   \                  a    λ(aa)
  FlatMap  g                     |
   /   \                       FlatMap
  a     f                        /   \
                               f(aa)  g

Старая структура:       Новая структура:
((a >>= f) >>= g)  →   (a >>= (aa => f(aa) >>= g))
```

**Ключевая идея:**

Мы не сворачиваем всё выражение целиком, а делаем **один шаг** — выносим левый `FlatMap` наружу. 
Это позволяет `foldF` работать как **трамплин**: на каждой итерации мы немного упрощаем структуру, не углубляя стек вызовов.

## Пример использования

[Исходный код](https://gitverse.ru/scalabook/typeclasses/content/master/src/main/scala/ru/scalabook/typeclasses/monad/FreeExample.scala)

```dotty
import cats.effect.{IO, IOApp, Ref}
import ru.scalabook.typeclasses.monad.Free
import ru.scalabook.typeclasses.monad.Free.*
import ru.scalabook.typeclasses.monad.Monad

object FreeExample extends IOApp.Simple:
  enum CounterOp[A]:
    case Increment extends CounterOp[Unit]
    case Get       extends CounterOp[Int]

  import CounterOp.*

  given Monad[IO] with
    override def unit[A](a: => A): IO[A] =
      IO.delay(a)

    extension [A](fa: IO[A])
      override def flatMap[B](f: A => IO[B]): IO[B] =
        fa.flatMap(f)

  def run: IO[Unit] =
    Ref.of[IO, Int](0).flatMap: ref =>
      // Естественное преобразование в IO
      given counterToIO: Natural[CounterOp, IO] with
        def transform[A](op: CounterOp[A]): IO[A] =
          op match
            case Increment =>
              ref.update(i => i + 1)
            case Get =>
              ref.get

      // Строим программу
      val program: Free[CounterOp, Int] =
        for
          _ <- liftF[CounterOp, Unit](Increment)
          _ <- liftF[CounterOp, Unit](Increment)
          x <- liftF[CounterOp, Int](Get)
        yield x

      // Выполняем программу
      val result: IO[Int] = Free.foldF(program)(counterToIO)

      result.flatMap(i => IO.println(s"i = $i")).void
end FreeExample
// i = 2
```

### Почему это полезно?

1. **Разделение описания и выполнения**: Программа описывается как данные, а интерпретатор определяет, как её выполнять
2. **Тестирование**: Можно написать несколько интерпретаторов (например, тестовый, production, для логирования)
3. **Композиция**: Можно комбинировать разные эффекты через свободные монады
4. **Рефакторинг**: Легко изменять логику выполнения без изменения описания программы

### Визуализация структуры

```
Free[F, A] =
    | UnitF(a)          // Чистое значение
    | LiftF(fa)         // Эффект из F
    | FlatMap(fa, f)    // Последовательность: сначала fa, затем f

Пример: liftF(Inc) >>= (_ => liftF(Get))
Становится: FlatMap(LiftF(Inc), _ => LiftF(Get))
```

Это мощная абстракция, лежащая в основе многих систем эффектов (Cats Effect, ZIO, Monix).

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Deriving the Free Monad](https://noelwelsh.com/posts/deriving-the-free-monad/)
- [Free Monads Are Simple](https://underscore.io/blog/posts/2015/04/14/free-monads-are-simple.html)
- [Free Monad in Scala](https://www.youtube.com/watch?v=lzlCjgRWPDU&list=PLmtsMNDRU0Bzj7INIrLugi3a_WClwQuiS)
- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Herding Cats](http://eed3si9n.com/herding-cats/Free-monads.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Free+Monad.html)
- [Monadic IO: Laziness Makes You Free](https://noelwelsh.com/posts/monadic-io-laziness-makes-you-free/)
- [Stackless Scala](../../translations/trampolining.md)
- [Stackless Scala With Free Monads - Runar Oli Bjarnason](https://blog.higher-order.com/assets/trampolines.pdf)
- [Why free monads matter](https://www.haskellforall.com/2012/06/you-could-have-invented-free-monads.html)

[sample]: https://gitverse.ru/scalabook/typeclasses
