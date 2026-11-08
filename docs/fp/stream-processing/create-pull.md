# Создание и комбинирование Pull-операций

`Pull[O, R]` — это не просто контейнер, а **описание процесса обработки данных**.
Мы строим планы преобразования информации, которые выполняются только когда это явно требуется.
Это похоже на построение запроса к базе данных: мы описываем _что_ нужно сделать, а не выполняем операции немедленно.

## Базовые строительные блоки

### Пустой Pull (`done`)


Самый простой `Pull` - это процесс, который ничего не выводит и завершается с результатом `Unit`:

```dotty
val done: Pull[Nothing, Unit] = Pull.Result(())
```

**Концептуальные аналогии:**

- `Nil` в списках — пустая коллекция
- `None` в `Option` — отсутствие значения
- [Нейтральный элемент в моноидах](../../typeclass/monoid/monoid.md) — базовая точка отсчета

**Характеристики:**

- **Тип `Nothing`** — процесс не производит никаких выходных значений
- **Результат `Unit`** — завершается без значимого результата
- **Нейтральный элемент** для операции последовательной композиции

**Практическое применение:**

```dotty
// Базовый случай для рекурсивных процессов
def generateWhilePositive(n: Int): Pull[Int, Unit] =
  if n <= 0 then Pull.done
  else Pull.Output(n)
```

### Создание значений (`Output`)

Создание процессов, которые выводят конкретные значения:

```dotty
val single: Pull[Int, Unit] = Pull.Output(1)
```

**Семантика:** "Произведи одно значение и заверши работу с результатом `Unit`"

**Концептуальные аналогии:**

- `List(1)` — список с одним элементом
- `Iterator.single(1)` — итератор с одним элементом
- `Some(1)` — наличие конкретного значения

**Особенности:**

- **Отложенное создание** — значение не вычисляется до момента выполнения
- **Композиционность** — может быть компонентом сложного процесса обработки

## Комбинирование операций

### Последовательное соединение (`>>`)

**Синтаксис:**

```dotty
val combined: Pull[Int, Unit] = Pull.Output(1) >> Pull.Output(2)
```

**Семантика:** "Сделай A, затем сделай B"

**Механизм работы:**

1. Выполнить левый `Pull` (генерирует все его выходы)
2. Игнорировать его результат
3. Выполнить правый `Pull`

**Эквивалент через `flatMap`:**

```dotty
Pull.Output(1).flatMap(_ => Pull.Output(2))
```

**Особенности:**

- **Правая часть ленива** — вычисляется только когда нужно
- **Сохраняет все выходы** — объединяет выходные элементы
- **Игнорирует результаты** — использует только выходные значения

### Преобразование в список (`toList`)

`Pull` можно преобразовать в обычный список:

```dotty
val p: Pull[Int, Unit] = Pull.Output(1) >> Pull.Output(2)
val asList: List[Int] = p.toList  // List(1, 2)
```

**Предупреждение:** Не используйте для больших или бесконечных потоков!

**Альтернативы для больших данных:**

```dotty
// Обработка по частям
p.fold(0)(_ + _)        // Сумма без загрузки в память
p.map(_ * 2).take(1000) // Ограничение размера (take описывается ниже)
```

## Построение из коллекций

### Из списка (`List`)

```dotty
def fromList[O](list: List[O]): Pull[O, Unit] =
  list match
    case Nil          => Pull.done
    case head :: tail => Pull.Output(head) >> fromList(tail)
```

**Рекурсивная структура:**

```
Output(1) >> (Output(2) >> (Output(3) >> done))
```

Как это работает:

- Для пустого списка возвращаем `done`
- Для непустого - выводим первый элемент и рекурсивно обрабатываем хвост

Пример:

```dotty
fromList(List(1, 2, 3)).toList  // List(1, 2, 3)
```

### Из ленивого списка (`LazyList`)

```dotty
def fromLazyList[O](lazyList: LazyList[O]): Pull[O, Unit] =
  lazyList match
    case LazyList()    => Pull.done
    case head #:: tail => Pull.Output(head) >> fromLazyList(tail)
```

**Ключевые преимущества:**

- **Работает с бесконечными потоками** (ограничено только памятью)
- **Ленивое вычисление** — элементы вычисляются по требованию
- **Экономия памяти** — не загружает все данные сразу

**Пример с бесконечным потоком:**

```dotty
val naturals = LazyList.from(1)       // 1, 2, 3, ...
fromLazyList(naturals.take(5)).toList // List(1, 2, 3, 4, 5)
```

**Ограничение:** `toList` на бесконечном потоке не завершится

## Обобщенное создание через `unfold`

### Принцип работы unfold

`unfold` — это обобщенный способ создания потоков из начального состояния.

**Сигнатура:**

```dotty
def unfold[O, R](init: R)(f: R => Either[R, (O, R)]): Pull[O, R] =
  f(init) match
    case Left(result) => Pull.Result(result)
    case Right((output, nextState)) =>
      Pull.Output(output) >> unfold(nextState)(f)
```

**Логика:**

1. Начать с состояния `init`
2. Применить функцию `f`:
   - Результат применения `Left(result)` → завершаемся с `result`
   - Результат применения `Right((output, nextState))` → выводим `output`, продолжаем с `nextState`

### Пример: Числа Фибоначчи

```dotty
val fibs: Pull[Int, (Int, Int)] = unfold((0, 1)) {
  case (a, b) => Right((a, (b, a + b)))
}
```

**Пошаговое выполнение:**

```
Состояние: (0, 1) → Output(0), новое состояние: (1, 1)
Состояние: (1, 1) → Output(1), новое состояние: (1, 2)
Состояние: (1, 2) → Output(1), новое состояние: (2, 3)
Состояние: (2, 3) → Output(2), новое состояние: (3, 5)
```

### Особенности реализации

1. **Ленивость**: `>>` не вычисляет второй `Pull` сразу, что позволяет работать с бесконечными потоками
2. **Безопасность стека**: рекурсия в `unfold` хвостовая и оптимизирована
3. **Композиционность**: `Pull` можно свободно комбинировать

### Практические применения unfold

**Генерация последовательностей:**

```dotty
// Арифметическая прогрессия
def arithmetic(first: Int, diff: Int): Pull[Int, Int] =
  unfold(first)(current => Right((current, current + diff)))

// Геометрическая прогрессия
def geometric(first: Int, ratio: Int): Pull[Int, Int] =
  unfold(first)(current => Right((current, current * ratio)))
```

**Чтение с состоянием:**

```dotty
def readChunks(file: File, chunkSize: Int): Pull[Array[Byte], Unit] =
  unfold(openFile(file)): channel =>
    val buffer = ByteBuffer.allocate(chunkSize)
    channel.read(buffer) match
      case -1 => Left(()) // EOF
      case n  => Right((buffer.array().take(n), channel))
```

## Практическое применение

Вышеописанные строительные блоки и операции позволяют:

- Преобразовывать существующие коллекции в потоки
- Создавать собственные генераторы данных
- Комбинировать несколько потоков вместе
- Обрабатывать данные по мере поступления без загрузки в память

### Упражнение 11.2.1: Реализация fromListViaUnfold и fromLazyListViaUnfold

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

**Задача:**

Реализуйте `fromListViaUnfold` и `fromLazyListViaUnfold` как методы сопутствующего объекта `Pull`, используя `unfold`:

```dotty
def fromListViaUnfold[O](os: List[O]): Pull[O, Unit] = ???
def fromLazyListViaUnfold[O](os: LazyList[O]): Pull[O, Unit] = ???
```

@:note(🔍 Показать решение)

Эти функции преобразуют `List[O]` и `LazyList[O]` в поток `Pull[O, Unit]` с помощью `unfold`:
для `List` используется паттерн-матчинг с `Nil` и `hd :: tl`, а для `LazyList` — с `LazyList()` и `hd #:: tl`,
где каждый элемент последовательности эмитится в поток, а хвост становится новым состоянием,
пока коллекция не будет исчерпана (возврат `Left`), после чего результат игнорируется через `.map(_ => ())`.

```dotty
def fromListViaUnfold[O](os: List[O]): Pull[O, Unit] =
  unfold(os):
    case Nil      => Left(Nil)
    case hd :: tl => Right((hd, tl))
  .map(_ => ())

def fromLazyListViaUnfold[O](os: LazyList[O]): Pull[O, Unit] =
  unfold(os):
    case LazyList() => Left(LazyList())
    case hd #:: tl  => Right((hd, tl))
  .map(_ => ())
```

@:@

## Бесконечные потоки

### Базовая реализация `continually`

Метод `continually` создает бесконечный поток, повторяющий одно значение:

```dotty
def continually[A](a: A): Pull[A, Nothing] =
  Output(a) >> continually(a)
```

**Семантика типа `Nothing`:**

- `Nothing` — это тип, не имеющий значений. Показывает, что поток **никогда не завершится** естественным образом
- Можно прервать только внешними операциями (`take`, `limit` - описываются ниже)
- Использует рекурсию для бесконечного повторения
- Ленивое выполнение благодаря оператору `>>`

**Проблема:** Не стекобезопасно из-за рекурсии

Пример использования:

```dotty
val ones: Pull[Int, Nothing] = Pull.continually(1)
```

### Улучшенная версия с `repeat`

Мы можем выделить общий паттерн повторения:

```dotty
enum Pull[+O, +R]:
  def repeat: Pull[O, R] =
    this >> repeat

object Pull:
  def continually[A](a: A): Pull[A, Unit] =
    Output(a).repeat
```

**Преимущества:**

- **Ленивая рекурсия** — правая часть `>>` вычисляется только при необходимости
- **Переиспользование кода** — общий паттерн для повторения
- **Читаемость** — явная семантика "повторяй"

Пример использования:

```dotty
val ones: Pull[Int, Unit] = Pull.continually(1)
```

### Упражнение 11.2.2: Создание бесконечного потока с iterate

Определите `iterate` как метод сопутствующего объекта `Pull`.
Он должен возвращать бесконечный поток, который сначала выводит предоставленное начальное значение,
затем выводит результат применения этого значения к предоставленной функции и т.д.:

```dotty
def iterate[O](initial: O)(f: O => O): Pull[O, Nothing] = ???
```

**Пояснение:**

- Метод `iterate` создает бесконечную последовательность (поток) значений
- Первым значением будет `initial`
- Каждое следующее значение получается применением функции `f` к предыдущему значению

**Пример использования:**

```dotty
// Создает поток: 1, 2, 4, 8, 16...
val powersOfTwo = Pull.iterate(1)(_ * 2)
```

@:note(🔍 Показать решение)

Это рекурсивная реализация метода `iterate`, который создаёт бесконечный поток значений, начиная с `initial`,
где каждое следующее значение вычисляется применением функции `f` к предыдущему.
Сначала выводится начальное значение через `Output`, затем рекурсивно генерируется следующий элемент (`f(initial)`),
что обеспечивает последовательное "развёртывание" потока.
Оператор `>>` связывает шаги, гарантируя корректную ленивую обработку.

```dotty
def iterate[O](initial: O)(f: O => O): Pull[O, Nothing] =
  Output(initial) >> iterate(f(initial))(f)
```

@:@

## Частичное выполнение потоков

### Операция `take`

Позволяет ограничить поток N элементами:

```dotty
def take(n: Int): Pull[O, Option[R]] =
  if n <= 0 then Result(None)
  else
    step match
      case Left(r)         => Result(Some(r))
      case Right((hd, tl)) => Output(hd) >> tl.take(n - 1)
```

**Стратегия выполнения:**

1. **Базовый случай**: n ≤ 0 — завершаемся с `None`
2. **Завершение источника**: источник завершился — возвращаем его результат в `Some`
3. **Есть данные**: выдаем элемент, уменьшаем счетчик, продолжаем

**Пример:**

```dotty
Pull.continually(1).take(3).toList  // List(1, 1, 1)
```

### Базовый блок `uncons`

Однако предыдущая реализация `take` имеет одну особенность:
когда `n` положительно, немедленно вычисляется каждый шаг исходного `pull`.

Поэтому сформулируем базовый строительный блок для частичного выполнения:

```dotty
def uncons: Pull[Nothing, Either[R, (O, Pull[O, R])]] =
  Pull.done >> Result(step)
```

**Назначение:** Отложенное получение следующего шага

**Преимущества перед прямым `step`:**

- **Ленивость** — вычисление отложено до момента использования
- **Безопасность** — можно использовать в композициях
- **Единообразие** — работает через механизм `Pull`

### Улучшенный `take` через `uncons`

```dotty
def take(n: Int): Pull[O, Option[R]] =
  if n <= 0 then Result(None)
  else
    uncons.flatMap:
      case Left(r)         => Result(Some(r))
      case Right((hd, tl)) => Output(hd) >> tl.take(n - 1)
```

Операция `uncons` оборачивает `step` конструктор `Result` и откладывает его создание через `flatMap`.
В реализации `take` вместо сопоставления с результатом `step` мы теперь сопоставляем с результатом `uncons.flatMap`.

Особенности:

- Откладывает вычисление через `>>`
- Возвращает либо результат, либо пару (элемент, продолжение)
- Позволяет безопасно работать с бесконечными потоками

С этим определением теперь можно создавать частично выполняемые бесконечные потоки:

```dotty
val ints      = Pull.fromLazyList(LazyList.from(0))
val firstFive = ints.take(5).toList
// List(0, 1, 2, 3, 4)
```

### Упражнение 11.2.3: Реализация drop для Pull

Реализуйте метод `drop`:

```dotty
def drop(n: Int): Pull[O, R] = ???
```

**Пояснение:**

- `drop(n)` - пропускает первые `n` элементов потока
- Метод работает через рекурсивное разворачивание потока (`uncons`)

**Примеры использования:**

```dotty
val nums = Pull.fromList(List(1, 2, 3, 4, 5))

nums.drop(2).toList          // List(3, 4, 5)
```

@:note(🔍 Показать решение)

`drop` пропускает первые `n` элементов через рекурсивное разворачивание потока (`uncons`), возвращая оставшуюся часть.

```dotty
def drop(n: Int): Pull[O, R] =
  if n <= 0 then this
  else
    uncons.flatMap:
      // Конец потока
      case Left(r) => Result(r)
      // Пропускаем текущий элемент
      case Right((_, tl)) => tl.drop(n - 1)
```

@:@

### Упражнение 11.2.4: Реализация takeWhile для Pull

Реализуйте метод `takeWhile`:

```dotty
def takeWhile(p: O => Boolean): Pull[O, Pull[O, R]] = ???
```

**Пояснение:**

- `takeWhile(p)` - возвращает элементы пока предикат `p` истинен, затем останавливается
- Метод работает через рекурсивное разворачивание потока (`uncons`)

**Примеры использования:**

```dotty
val nums = Pull.fromList(List(1, 2, 3, 4, 5))

nums.takeWhile(_ < 4).toList // List(1, 2, 3)
```

@:note(🔍 Показать решение)

`takeWhile` последовательно выводит элементы, удовлетворяющие предикату `f`, а затем возвращает остаток потока.

```dotty
def takeWhile(f: O => Boolean): Pull[O, Pull[O, R]] =
  uncons.flatMap:
    // Конец потока
    case Left(r)         => Result(Result(r))
    case Right((hd, tl)) =>
      // Выводим подходящий элемент
      if f(hd) then
        Output(hd) >> tl.takeWhile(f)
      // Возвращаем оставшийся поток
      else Result(Output(hd) >> tl)
```

@:@

### Упражнение 11.2.5: Реализация dropWhile для Pull

Реализуйте метод `dropWhile`:

```dotty
def dropWhile(f: O => Boolean): Pull[O, R] = ???
```

**Пояснение:**

- `dropWhile(p)` - пропускает элементы пока предикат `p` истинен, затем возвращает оставшиеся
- Метод работает через рекурсивное разворачивание потока (`uncons`)

**Примеры использования:**

```dotty
val nums = Pull.fromList(List(1, 2, 3, 4, 5))

nums.dropWhile(_ < 3).toList // List(3, 4, 5)
```

@:note(🔍 Показать решение)

`dropWhile` пропускает элементы, пока они удовлетворяют `f`, а затем возвращает оставшиеся.

```dotty
def dropWhile(f: O => Boolean): Pull[O, R] =
  uncons.flatMap:
    // Конец потока
    case Left(r)         => Result(r)
    case Right((hd, tl)) =>
      // Пропускаем подходящий элемент
      if f(hd) then tl.dropWhile(f)
      // Возвращаем оставшийся поток
      else Output(hd) >> tl  
```

@:@

## Трансформации потоков

### Преобразование элементов (`mapOutput`)

Преобразует каждый элемент потока:

```dotty
def mapOutput[O2](f: O => O2): Pull[O2, R] =
  uncons.flatMap:
    case Left(r)         => Result(r)
    case Right((hd, tl)) => Output(f(hd)) >> tl.mapOutput(f)
```

**Семантика:** "Применить `f` к каждому элементу потока"

**Пример:**

```dotty
Pull.fromList(List(1, 2, 3)).mapOutput(_ * 2).toList
// List(2, 4, 6)
```

**Особенности:**

- **Ленивое выполнение** — преобразования применяются по мере необходимости
- **Сохранение структуры** — результат имеет тот же тип завершения `R`

### Фильтрация (`filter`)

Фильтрует элементы по условию:

```dotty
def filter(p: O => Boolean): Pull[O, R] =
  uncons.flatMap:
    case Left(r) => Result(r)
    case Right((hd, tl)) =>
      (if p(hd) then Output(hd) else Pull.done) >> tl.filter(p)
```

**Стратегия:** Пропустить элемент если предикат `false`, иначе вывести

**Пример:**

```dotty
Pull.fromList(List(1, 2, 3, 4)).filter(_ % 2 == 0).toList
// List(2, 4)
```

## Работа с состоянием

### Подсчет элементов (`count`)

```dotty
def count: Pull[Int, R] =
  def go(total: Int, p: Pull[O, R]): Pull[Int, R] =
    p.uncons.flatMap:
      case Left(r) => Result(r)
      case Right((_, tl)) =>
        val newTotal = total + 1
        Output(newTotal) >> go(newTotal, tl)

  Output(0) >> go(0, this)
```

**Вывод после каждого элемента:** 0, 1, 2, 3, ...

Как работает:

1. Начинает с 0
2. Для каждого элемента увеличивает счетчик
3. Выдает текущее количество после каждого элемента

**Пример:**

```dotty
Pull.fromList(List("a", "b", "c")).count.toList
// List(0, 1, 2, 3)
```

**Альтернатива — только итог:**

```dotty
def totalCount: Pull[Nothing, Int] =
  fold(0)((count, _) => count + 1).map(_._1)
```

### Обобщенный `mapAccumulate`

Комбинирует `map` и `fold` с сохранением состояния:

```dotty
def mapAccumulate[S, O2](init: S)(f: (S, O) => (S, O2)): Pull[O2, (S, R)] =
  uncons.flatMap:
    case Left(r) => Result((init, r))
    case Right((hd, tl)) =>
      val (s, out) = f(init, hd)
      Output(out) >> tl.mapAccumulate(s)(f)
```

**Пример — накопление суммы:**

```dotty
Pull.fromList(List(1, 2, 3)).mapAccumulate(0)((sum, x) =>
  (sum + x, s"Element: $x, Running total: ${sum + x}")
).toList
// List("Element: 1, Running total: 1",
//      "Element: 2, Running total: 3",
//      "Element: 3, Running total: 6")
```

**Применения:**

- Накопление статистики
- Нумерация элементов
- Скользящие средние
- Состоятельные преобразования

### Упражнение 11.2.6: Реализация countViaMapAccumulate

Переопределите `count`, используя `mapAccumulate` вместо явной рекурсии.

```dotty
def countViaMapAccumulate: Pull[Int, R] = ???
```

**Примеры использования:**

```dotty
Pull.fromList(List("a", "b", "c")).countViaMapAccumulate.toList
// List(0, 1, 2, 3)
```

@:note(🔍 Показать решение)

Это решение реализует подсчёт элементов в потоке через `mapAccumulate`, начиная с 0:
сначала эмитится начальное значение 0 (`Output(0)`),
затем для каждого элемента потока аккумулируется состояние-счётчик (увеличивается на 1),
при этом эмитится текущее значение счётчика (`s + 1`),
а `map(_(1))` извлекает только эмитируемые значения (второй элемент кортежа), игнорируя промежуточные состояния.
Результат — поток чисел от 1 до N (где N — количество элементов), предваренный нулём.

```dotty
def countViaMapAccumulate: Pull[Int, R] =
  Output(0) >> mapAccumulate(0)((s, o) => (s + 1, s + 1)).map(_(1))
```

@:@

## Заключение: Паттерны проектирования

1. **Recursive Descent** — рекурсивное построение через `>>`
2. **Unfold Pattern** — генерация из состояния
3. **Transformer Pattern** — преобразования через `mapOutput`, `filter`
4. **Accumulator Pattern** — накопление состояния через `mapAccumulate`

**Преимущества подхода:**

- **Композиционность** — маленькие блоки собираются в сложные пайплайны
- **Ленивость** — вычисления только по требованию
- **Безопасность** — контроль над выполнением
- **Выразительность** — ясная семантика операций

Этот DSL позволяет строить сложные потоковые обработки из простых, хорошо комбинируемых компонентов.

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
