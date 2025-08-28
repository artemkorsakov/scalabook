{%
showPrev = true
prevPage = create-pull.html
%}

# Потоки

## Вспомним, что такое Pull

`Pull` - это алгебраический тип данных (ADT), который представляет собой потоковое вычисление. Давайте разберем его структуру:

```dotty
// Pull - это алгебраический тип данных, представляющий потоковое вычисление
enum Pull[+O, +R]:
  // Result - завершение вычисления с результатом типа R
  case Result[+R](result: R) extends Pull[Nothing, R]
  
  // Output - выдача выходного значения типа O
  case Output[+O](value: O)  extends Pull[O, Unit]
  
  // FlatMap - композиция вычислений (монадическое связывание)
  case FlatMap[X, +O, +R](
      source: Pull[O, X],
      f: X => Pull[O, R]
  ) extends Pull[O, R]
```

### Аналогия для понимания

Представьте `Pull[O, R]` как конвейерную ленту:

- `O` (Output) - это тип элементов, которые "выходят" наружу в процессе вычисления, как товары на конвейерной ленте
- `R` (Result) - это тип финального результата вычисления, как итоговый отчет о работе конвейера

## Монадическая природа Pull

[Монада](../../typeclass/monad/monad.md) - это паттерн в функциональном программировании, 
который позволяет последовательно комбинировать вычисления. 
Основные операции монады:

- `unit` (или `pure`) - завертывает значение в монадический контекст
- `flatMap` - последовательно связывает вычисления

### Стандартная монада для Pull

Для `Pull[O, A]` мы можем создать стандартную монаду:

```dotty
// Стандартный экземпляр монады для Pull[O, A]
given [O]: Monad[[x] =>> Pull[O, x]] with
  def unit[A](a: => A): Pull[O, A] = Result(a)  // Заворачиваем значение в Pull
  
  extension [A](pa: Pull[O, A])
    def flatMap[B](f: A => Pull[O, B]): Pull[O, B] =
      pa.flatMap(f)  // Используем встроенный flatMap Pull
```

**Как это работает на практике:**

```dotty
val computation: Pull[Int, String] =
  Result(5).flatMap: n =>          // Начинаем с числа 5
    Output(n).flatMap: _ =>        // Выводим число 5
      Result(s"Got number: $n")    // Завершаем с результатом
```

Это похоже на работу с `Option` или `List`, где мы последовательно связываем вычисления.

### Идея альтернативной монады

Что если нас интересуют только выходные значения, а не финальный результат?

Рассмотрим `Pull[A, Unit]`:

- `A` - тип выходных значений (то, что нас действительно интересует)
- `Unit` - финальный результат (ничего интересного, просто сигнал о завершении)

Мы можем создать альтернативный экземпляр монады, где:

- `unit` реализуется через `Output(a)`
- `flatMap` работает по-другому

### Реализация flatMapOutput

```dotty
extension [O](self: Pull[O, Unit])
  def flatMapOutput[O2](f: O => Pull[O2, Unit]): Pull[O2, Unit] =
    self.uncons.flatMap:
      case Left(()) => Result(())        // Конец потока - ничего не делаем
      case Right((hd, tl)) =>            // Есть элемент + остаток
        f(hd) >> tl.flatMapOutput(f)     // Применяем f к элементу и продолжаем
```

**Аналогия с List:**

```dotty
// Похоже на стандартный flatMap для списка:
def flatMapOutput[B](f: A => List[B]): List[B] = 
  this.flatMap(f)
```

**Пример использования:**

```dotty
val ints: Pull[Int, Unit] = Pull.fromList(List(0, 1, 2))
val doubled: Pull[Int, Unit] = ints.flatMapOutput(i => Pull.fromList(List(i, i)))
doubled.toList

// Результат: List(0, 0, 1, 1, 2, 2)
// Каждое число дублируется     
```

### Альтернативная монада

На основе `flatMapOutput` мы можем создать альтернативный экземпляр [монады](../../typeclass/monad/monad.md):

```dotty
val outputMonad: Monad[[x] =>> Pull[x, Unit]] = new:
  def unit[A](a: => A): Pull[A, Unit] = Output(a)
  extension [A](pa: Pull[A, Unit])
    def flatMap[B](f: A => Pull[B, Unit]): Pull[B, Unit] =
      pa.flatMapOutput(f)
```

### Когда использовать какую монаду?

1. **Стандартная монада** (`Pull[O, A]`) - когда важен и процесс (выходные значения), и результат
   - Пример: чтение файла, где выходные значения - строки, а результат - статистика

2. **Альтернативная монада** (`Pull[A, Unit]`) - когда важны только выходные значения
   - Пример: трансформация потока данных, где мы только преобразуем элементы

**Примеры в коде**

```dotty
// Результат - это Pull
Pull.pullMonad.unit(Pull.Output(1))
// val res1: Pull[Nothing, Pull[Int, Unit]] = Result(Output(1))

// Преобразование элементов
Pull.outputMonad.unit(Pull.Output(1))
// val res2: Pull[Pull[Int, Unit], Unit] = Output(Output(1))
```

**Заключение**

`Pull`-монады предоставляют мощный инструмент для работы с потоковыми вычислениями:

1. **Стандартная монада** фокусируется на последовательной композиции вычислений с сохранением финального результата
2. **Альтернативная монада** фокусируется на преобразовании выходных значений, игнорируя финальный результат

Выбор между ними зависит от того, что важно в конкретном случае: 
только процесс обработки данных или также и финальный результат этой обработки.

## Идея создания Stream как специализированного типа

Вместо работы напрямую с `Pull[O, Unit]`, мы создаем специализированный тип `Stream`, который:

- Является непрозрачным псевдонимом для `Pull[O, Unit]`
- Фиксирует тип результата как `Unit` (фокусируясь только на выходных значениях)
- Предоставляет более удобный API для работы с потоками данных

```dotty
// Определяем Stream как непрозрачный тип
opaque type Stream[+O] = Pull[O, Unit]

object Stream:
  // Создание Stream из последовательности значений
  def apply[O](os: O*): Stream[O] =
    Pull.fromList(os.toList).toStream

  // Методы расширения для Stream
  extension [O](self: Stream[O])
    // Конвертация обратно в Pull
    def toPull: Pull[O, Unit] = self

    // Свертка элементов потока
    def fold[A](init: A)(f: (A, O) => A): A =
      self.fold(init)(f)(1)

    // Преобразование в список
    def toList: List[O] =
      self.toList

    // Взятие первых n элементов
    def take(n: Int): Stream[O] =
      self.take(n).map(_ => ())

    // Конкатенация потоков
    def ++(that: => Stream[O]): Stream[O] =
      self >> that

  // Монадический экземпляр для Stream
  given Monad[Stream] with
    def unit[A](a: => A): Stream[A] = Pull.Output(a)
    extension [A](sa: Stream[A])
      def flatMap[B](f: A => Stream[B]): Stream[B] =
        sa.flatMapOutput(f)

object Pull:
  // Конвертация Pull в Stream
  extension [O](self: Pull[O, Unit])
    def toStream: Stream[O] = self
```

### Непрозрачный тип (Opaque Type)

```dotty
opaque type Stream[+O] = Pull[O, Unit]
```

**Что это значит:**

- `Stream[O]` внешне выглядит как отдельный тип
- Внутри он реализован как `Pull[O, Unit]`
- Это обеспечивает типобезопасность и чистый API

**Преимущества:**

- Сокрытие внутренней реализации
- Предотвращение случайного неправильного использования API
- Возможность предоставить специализированные методы

### Создание Stream

```dotty
def apply[O](os: O*): Stream[O] =
  Pull.fromList(os.toList).toStream
```

**Как работает:**

- Принимает переменное количество элементов типа `O`
- Преобразует в список
- Создает `Pull` из списка
- Конвертирует в `Stream`

**Пример:**

```dotty
val numbers: Stream[Int] = Stream(1, 2, 3, 4, 5)
```

### Методы расширения Stream

#### `toPull` - обратная конвертация

```dotty
def toPull: Pull[O, Unit] = self
```

**Зачем нужно:**

- Для низкоуровневых операций с `Pull`
- Когда нужен доступ к полной мощности `Pull` API

#### `fold` - агрегация элементов

```dotty
def fold[A](init: A)(f: (A, O) => A): A
```

**Аналогия с коллекциями:**

```dotty
// Похоже на List.foldLeft
List(1, 2, 3).fold(0)(_ + _)  // = 6
```

#### `toList` - материализация потока

```dotty
def toList: List[O]
```

**Особенности:**

- Полностью потребляет поток
- Возвращает все элементы как обычный список

#### `take` - ограничение потока

```dotty
def take(n: Int): Stream[O] =
  self.take(n).map(_ => ())
```

**Что делает `.map(_ => ())`:**

- Преобразует `Pull[O, Option[Unit]]` в `Pull[O, Unit]`
- Отбрасывает служебную информацию о результате

#### `++` - конкатенация

```dotty
def ++(that: => Stream[O]): Stream[O] =
  self >> that
```

**Как работает:**

- `>>` (then) оператор для `Pull`
- Выполняет первый поток, затем второй
- Ленивая оценка `that` (по имени)

### Монада для Stream

```dotty
given Monad[Stream] with
  def unit[A](a: => A): Stream[A] = Pull.Output(a)
  extension [A](sa: Stream[A])
    def flatMap[B](f: A => Stream[B]): Stream[B] =
      sa.flatMapOutput(f)
```

**Особенности этой монады:**

- `unit` создает поток с одним элементом
- `flatMap` работает как `flatMapOutput` для `Pull`
- Позволяет использовать for-comprehensions

**Пример:**

```dotty
val result: Stream[Int] =
  for
    a <- Stream(1, 2)
    b <- Stream(10, 20)
  yield a + b
// Результат: 11, 21, 12, 22
```

### Конвертация между Pull и Stream

```dotty
// В Pull object
extension [O](self: Pull[O, Unit])
  def toStream: Stream[O] = self

// В Stream object
extension [O](self: Stream[O])
  def toPull: Pull[O, Unit] = self
```

**Двусторонняя конвертация:**

- `Stream → Pull`: для низкоуровневых операций
- `Pull → Stream`: для высокоуровневых операций

## Зачем нужны оба типа: Pull и Stream?

### Pull - для построения вычислений

**Сильные стороны `Pull`:**

- Более мощный и гибкий
- Позволяет определять кастомные потоковые вычисления
- Подходит для низкоуровневых операций
- Сохраняет информацию о результате вычисления

**Использование:**

```dotty
// Создание кастомных потоковых операций
def customProcessor[O]: Pull[O, Statistics] = ...
```

### Stream - для работы с данными

**Сильные стороны `Stream`:**

- Упрощенный API, похожий на коллекции
- Фокус на выходных значениях
- Удобен для трансформации данных
- Поддерживает монадические операции

**Использование:**
```dotty
// Работа как с коллекцией
val processed: Stream[Int] = numbers
  .map(_ * 2)
  .filter(_ > 5)
  .take(10)
```

### High-level vs Low-level API

1. **Stream API** (высокоуровневый):
   - Для обычных операций с данными
   - Похож на работу с `List`/`LazyList`
   - Безопасный и удобный

2. **Pull API** (низкоуровневый):
   - Для создания новых операций
   - Когда нужен контроль над выполнением
   - Для кастомной логики обработки

### Пример архитектуры

```dotty
// Низкоуровневая операция (использует Pull)
def customTake[O](n: Int): Pull[O, Unit] => Pull[O, Unit] = {
  // Сложная логика на Pull
}

// Высокоуровневое использование (использует Stream)
val result: Stream[Int] = numbers
  .customTake(5)  // Метод расширения, использующий Pull внутри
  .map(transform)
```

### Заключение

Разделение на `Pull` и `Stream` предоставляет:

1. **Гибкость** `Pull` для создания сложных вычислений
2. **Удобство** `Stream` для работы с данными
3. **Безопасность** типов через непрозрачные типы
4. **Интероперабельность** через конвертацию между типами

Это разделение позволяет использовать правильный инструмент для каждой задачи, 
сочетая мощность низкоуровневого API с удобством высокоуровневого.

## Составление потоковых преобразований

Тип `Pipe` - это фундаментальная абстракция для создания модульных и композируемых потоковых преобразований.

```dotty
type Pipe[-I, +O] = Stream[I] => Stream[O]
```

### Что такое Pipe?

**Pipe\[I, O\]** - это функция, которая:

- Принимает на вход поток элементов типа `I`
- Возвращает поток элементов типа `O`
- Преобразует один поток в другой

**Простая аналогия:** Представьте себе водопроводную трубу (pipe), которая:

- Вход: вода одного качества
- Выход: вода другого качества (очищенная, обогащенная и т.д.)

### Почему это полезно?

1. **Модульность** - каждое преобразование независимо
2. **Переиспользуемость** - один `Pipe` можно применять к разным потокам
3. **Композируемость** - `Pipe` можно комбинировать как Lego
4. **Тестируемость** - каждый `Pipe` можно тестировать отдельно
5. Побуждает пользователей потоковой библиотеки мыслить в терминах независимых потоковых преобразований

### Базовые преобразования

```dotty
// Удаление пустых строк
val nonEmpty: Pipe[String, String] =
  (input: Stream[String]) => input.filter(_.nonEmpty)

// Приведение к нижнему регистру
val lowerCase: Pipe[String, String] =
  (input: Stream[String]) => input.map(_.toLowerCase)
```

**Как это работает:**

- `nonEmpty` принимает `Stream[String]`, возвращает `Stream[String]` без пустых строк
- `lowerCase` принимает `Stream[String]`, возвращает `Stream[String]` в нижнем регистре

### Композиция Pipe

```dotty
// Композиция через andThen
val normalize: Pipe[String, String] =
  nonEmpty andThen lowerCase

// Эквивалентная композиция через compose
val normalizeAlt: Pipe[String, String] =
  lowerCase compose nonEmpty
```

**Что происходит при композиции:**

1. Сначала применяется `nonEmpty` - удаляются пустые строки
2. Затем применяется `lowerCase` - оставшиеся строки приводятся к нижнему регистру

**Визуализация:**

```
Исходный поток: ["Hello", "", "World"] 
  → nonEmpty:      ["Hello", "World"] 
  → lowerCase:     ["hello", "world"]
```

### Способы применения Pipe

```dotty
val lines: Stream[String] = Stream("Hello", "", "World!")

// Способ 1: Прямой вызов функции
val normalized1: Stream[String] = normalize(lines)

// Способ 2: Использование pipe оператора
import scala.util.chaining.scalaUtilChainingOps
val normalized2: Stream[String] = lines.pipe(normalize)

// Способ 3: Последовательное применение
val normalized3: Stream[String] = lines.pipe(nonEmpty).pipe(lowerCase)
```

**Разница между способами:**

- Все три способа функционально эквивалентны
- Выбор зависит от стиля и читаемости кода
- `pipe` оператор часто читается более естественно

## Упражнения

### Упражнение 11.3.1: Реализация exists. Стратегия 1: Только конечный результат (остановка при нахождении)

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

**Задача:**

Реализовать `exists[I](f: I => Boolean): Pipe[I, Boolean]`,
останавливающийся при нахождении первого результата.

```dotty
exists1[I](f: I => Boolean): Pipe[I, Boolean] = ???
```

**Поведение:** `exists1(_ % 2 == 0)(Stream(1, 3, 5, 6, 7)) → Stream(true)`

**Пример использования:**

```dotty
// Проверка наличия четных чисел в потоке
val hasEvenNumbers: Pipe[Int, Boolean] = exists1(_ % 2 == 0)

// Применение к разным потокам
val stream1: Stream[Int] = Stream(1, 3, 5, 7, 9)
val stream2: Stream[Int] = Stream(1, 3, 5, 6, 7)

val result1: Stream[Boolean] = hasEvenNumbers(stream1) // Stream(false)
val result2: Stream[Boolean] = hasEvenNumbers(stream2) // Stream(true)
```

@:note(🔍 Показать решение)

```dotty
def exists1[I](f: I => Boolean): Pipe[I, Boolean] =
  (input: Stream[I]) =>
    input
      .mapOutput(f)        // Преобразуем каждый элемент в Boolean
      .dropWhile(i => !i)  // Пропускаем все false до первого true
      .takeWhile(identity) // Берем все true (но обычно только первый)
      .take(1)             // Гарантируем, что возьмем только один элемент
      .uncons.flatMap:
        // Если достигли результата, то в исходном стриме нет элемента, удовлетворяющего предикату
        case Left(_)  => Pull.Output(false)
        // В противном случае такой элемент есть
        case Right(_) => Pull.Output(true)       
```

@:@

### Упражнение 11.3.2: Реализация exists. Стратегия 2: Все промежуточные результаты (остановка при нахождении)

**Задача:**

Реализовать `exists[I](f: I => Boolean): Pipe[I, Boolean]` - остановка и выдача всех промежуточных результатов.

```dotty
exists[I](f: I => Boolean): Pipe[I, Boolean] = ???
```

**Поведение:** `exists2(_ % 2 == 0)(Stream(1, 3, 5, 6, 7)) → Stream(false, false, false, true)`

**Пример использования:**

```dotty
// Поиск первого четного числа с выводом промежуточных результатов
val trackEvenSearch: Pipe[Int, Boolean] = exists2(_ % 2 == 0)

// Применение к потоку чисел
val numbers: Stream[Int] = Stream(1, 3, 5, 6, 7, 8, 9)
val searchProcess: Stream[Boolean] = trackEvenSearch(numbers)

// Результат: false (1), false (3), false (5), true (6) → остановка
```

**Преимущества этой стратегии:**

- ✅ Показывает весь процесс поиска
- ✅ Останавливается при первом успехе
- ✅ Эффективно использует память (ленивая обработка)
- ✅ Подходит для отладки и мониторинга

**Применение в реальных сценариях:**

- Отслеживание процесса поиска в больших данных
- Мониторинг выполнения условий в реальном времени
- Отладочная информация для сложных pipeline
- Поиск первого подходящего элемента с историей проверок

@:note(🔍 Показать решение)

```dotty
def exists2[I](f: I => Boolean): Pipe[I, Boolean] =
  (input: Stream[I]) =>
    // Вспомогательная рекурсивная функция
    def go(p: Stream[Boolean]): Stream[Boolean] =
      p.uncons.flatMap:
        // Конец потока - завершаем
        case Left(r)              => Pull.done
        // Нашли true - выводим и завершаем
        case Right((hd, _)) if hd => Pull.Output(true) >> Pull.done
        // Нашли false - выводим и продолжаем
        case Right((hd, tl))      => Pull.Output(hd) >> go(tl)

    // Преобразуем входной поток и обрабатываем
    go(input.mapOutput(f))
```

**Пошаговое объяснение решения:**

1. **`input.mapOutput(f)`** - преобразуем каждый элемент в `Boolean`
2. **`go`** - рекурсивно обрабатывает поток `Boolean` значений:
   - **`case Left(())`** - поток закончился, завершаем
   - **`case Right((true, _))`** - нашли `true`, выводим и останавливаемся
   - **`case Right((false, tail))`** - нашли `false`, выводим и продолжаем
@:@

### Упражнение 11.3.3: Реализация exists. Стратегия 3: Все результаты (без остановки)

**Задача:**

Реализовать `exists[I](f: I => Boolean): Pipe[I, Boolean]` - не остановка и выдача всех промежуточных результатов.

```dotty
exists[I](f: I => Boolean): Pipe[I, Boolean] = ???
```

**Поведение:** `exists3(_ % 2 == 0)(Stream(1, 3, 5, 6, 7)) → Stream(false, false, false, true, false)`

**Пример использования:**

```dotty
// Поиск четных чисел с полной историей проверок
val trackAllEvens: Pipe[Int, Boolean] = exists3(_ % 2 == 0)

// Применение к потоку чисел
val numbers: Stream[Int] = Stream(1, 3, 5, 6, 7, 8, 9)
val allResults: Stream[Boolean] = trackAllEvens(numbers)

// Результат: false (1), false (3), false (5), true (6), false (7), true (8), false (9)
```

**Преимущества этой стратегии:**

- ✅ **Полная история** - все промежуточные результаты сохраняются
- ✅ **Непрерывная обработка** - не прерывается при нахождении элемента
- ✅ **Детерминированность** - обрабатывает все элементы потока
- ✅ **Подходит для агрегации** - можно строить полную статистику

**Практические применения:**

- Мониторинг качества данных в реальном времени
- Построение полной истории выполнения условий
- Агрегация статистики по всему потоку
- Feature monitoring в ML pipelines
- Аудит и логирование процессов проверки

@:note(🔍 Показать решение)

```dotty
def exists3[I](f: I => Boolean): Pipe[I, Boolean] =
  (input: Stream[I]) => input.mapOutput(f)
```

@:@

### Выбор стратегии exist

**Критерии выбора:**

1. **Эффективность** - нужно ли обрабатывать весь поток?
2. **Ленивость** - можно ли остановиться досрочно?
3. **Информативность** - нужны ли промежуточные результаты?

**Отличия стратегий:**

| Стратегия | Остановка          | Результаты            | Использование                       |
|-----------|--------------------|-----------------------|-------------------------------------|
| **1**     | При первом `true`  | Только конечный       | Когда важен только факт наличия     |
| **2**     | При первом `true`  | Все до первого `true` | Для отладки процесса поиска         |
| **3**     | Не останавливается | Все результаты        | Для полной статистики и мониторинга |

**Для задачи подсчета строк:**

Теперь мы можем выразить основное преобразование для задачи подсчета строк как `count andThen exists(_ > 40000)`,
и можно легко присоединить фильтры и другие преобразования к конвейеру.

```dotty
val count: Pipe[String, Int] = // преобразование подсчета
val thresholdCheck: Pipe[Int, Boolean] = exists(_ > 40000)

// Композиция pipeline
val pipeline: Pipe[String, Boolean] = count andThen thresholdCheck
```

## Практическое применение в реальных задачах

### Пример: Анализ лог-файлов

```dotty
// Разные этапы обработки
val readLines: Pipe[String, String] = identity // чтение файла
val filterErrors: Pipe[String, String] = _.filter(_.contains("ERROR"))
val extractTimestamps: Pipe[String, String] = _.map(_.take(19)) // извлечение времени
val countByHour: Pipe[String, (String, Int)] = // группировка по часу

// Композиция pipeline
val errorAnalysis: Pipe[String, (String, Int)] =
  readLines andThen
  filterErrors andThen
  extractTimestamps andThen
  countByHour
```

### Преимущества подхода

1. **Гибкость** - легко добавлять/удалять этапы обработки
2. **Тестируемость** - каждый `Pipe` тестируется отдельно
3. **Читаемость** - код отражает бизнес-логику
4. **Переиспользуемость** - `Pipe` можно использовать в разных контекстах

## Заключение

Концепция `Pipe` предоставляет мощный инструмент для построения сложных потоковых обработчиков данных:

1. **Абстракция** - скрывает сложность внутренней реализации
2. **Композиция** - позволяет строить сложные преобразования из простых
3. **Гибкость** - поддерживает разные стратегии обработки
4. **Практичность** - применима к реальным задачам обработки данных

Такой подход превращает обработку потоков данных из императивного кода в декларативное описание преобразований, 
что делает код более чистым, понятным и поддерживаемым.

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp

