{%
showPrev = true
prevPage = ../stream-processing.html
showNext = true
nextPage = monad-pull.html
%}

# Создание и комбинирование Pull-операций

`Pull[O, R]` — это не просто контейнер, а **описание процесса**. 
Мы строим планы обработки данных, которые выполняются лениво. 
Это похоже на построение SQL-запроса: мы описываем _что_ нужно сделать, а не _как_ это делать немедленно.

## Базовые строительные блоки

### Пустой Pull (`done`)

```dotty
val done: Pull[Nothing, Unit] = Pull.Result(())
```

**Аналогии:**

- `Nil` в списках — пустая коллекция
- `None` в Option — отсутствие значения
- `Monoid.empty` в алгебраических структурах

**Особенности:**

- **Тип `Nothing`** — не может выводить никакие значения
- **Результат `Unit`** — завершается без значимого результата
- **Идентификатор** для операции `>>` (как `0` для сложения)

**Использование:**

```dotty
// Базовый case для рекурсии
def emptyIfNegative(n: Int): Pull[Int, Unit] =
  if n < 0 then Pull.done
  else Pull.Output(n)
```

### Создание значений (`Output`)

```dotty
val single: Pull[Int, Unit] = Pull.Output(1)
```

**Семантика:** "Выведи одно значение и завершись с результатом `Unit`"

**Аналогии:**

- `List(1)` — список с одним элементом
- `Iterator.single(1)` — итератор с одним элементом
- `Option(1)` — наличие значения

**Особенности:**

- **Ленивое создание** — значение не вычисляется до выполнения
- **Композиционность** — может быть частью更大的 пайплайна

---

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
- **Игнорирует результаты** — использует только выходы

### Преобразование в список (`toList`)

```dotty
val p: Pull[Int, Unit] = Pull.Output(1) >> Pull.Output(2)
val asList: List[Int] = p.toList  // List(1, 2)
```

**Предупреждение:** Не используйте для больших или бесконечных потоков!

**Альтернативы для больших данных:**

```dotty
// Обработка по частям
p.fold(0)(_ + _) // Сумма без загрузки в память
p.map(_ * 2).take(1000) // Ограничение размера
```

---

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

**Особенности:**

- **Не стекобезопасно** для очень длинных списков
- **Строгая оценка** — список должен быть полностью в памяти

**Стекобезопасная версия:**

```dotty
def fromListSafe[O](list: List[O]): Pull[O, Unit] = {
  def loop(remaining: List[O]): Pull[O, Unit] =
    remaining match
      case Nil => Pull.done
      case head :: tail => Pull.Output(head) >> loop(tail)

  loop(list)
}
```

### Из ленивого списка (`LazyList`)

```dotty
def fromLazyList[O](lazyList: LazyList[O]): Pull[O, Unit] =
  lazyList match
    case LazyList()    => Pull.done
    case head #:: tail => Pull.Output(head) >> fromLazyList(tail)
```

**Ключевые преимущества:**

- **Работает с бесконечными потоками** (с ограничениями)
- **Ленивое вычисление** — элементы вычисляются по требованию
- **Экономия памяти** — не загружает все данные сразу

**Пример с бесконечным потоком:**

```dotty
val naturals = LazyList.from(1) // 1, 2, 3, ...
fromLazyList(naturals.take(5)).toList // List(1, 2, 3, 4, 5)
```

**Ограничение:** `toList` на бесконечном потоке не завершится

---

## Обобщенное создание через `unfold`

### Принцип работы unfold

`unfold` — это обобщенный способ создания потоков из начального состояния.

**Сигнатура:**

```dotty
def unfold[O, R](init: R)(f: R => Either[R, (O, R)]): Pull[O, R]
```

**Логика:**

1. Начать с состояния `init`
2. Применить функцию `f`:
   - `Left(result)` → завершиться с `result`
   - `Right((output, nextState))` → вывести `output`, продолжить с `nextState`

### Пример: Числа Фибоначчи

```dotty
val fibs: Pull[Int, Unit] = unfold((0, 1)) {
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

### Практические применения unfold

**Генерация последовательностей:**

```dotty
// Арифметическая прогрессия
def arithmetic(first: Int, diff: Int): Pull[Int, Nothing] =
  unfold(first)(current => Right((current, current + diff)))

// Геометрическая прогрессия
def geometric(first: Int, ratio: Int): Pull[Int, Nothing] =
  unfold(first)(current => Right((current, current * ratio)))
```

**Чтение с состоянием:**

```dotty
def readChunks(file: File, chunkSize: Int): Pull[Array[Byte], Unit] =
  unfold(openFile(file)) { channel =>
    val buffer = ByteBuffer.allocate(chunkSize)
    channel.read(buffer) match {
      case -1 => Left(()) // EOF
      case n => Right((buffer.array().take(n), channel))
    }
  }
```

---

## Бесконечные потоки

### Базовая реализация `continually`

```dotty
def continually[A](a: A): Pull[A, Nothing] =
  Output(a) >> continually(a)
```

**Семантика типа `Nothing`:**

- `Nothing` — это тип, не имеющий значений
- Показывает, что поток **никогда не завершится** естественным образом
- Можно прервать только внешними операциями (`take`, `limit`)

**Проблема:** Не стекобезопасно из-за рекурсии

### Улучшенная версия с `repeat`

```dotty
enum Pull[+O, +R]:
  def repeat: Pull[O, R] =
    this >> repeat

object Pull:
  def continually[A](a: A): Pull[A, Nothing] =
    Output(a).repeat
```

**Преимущества:**

- **Ленивая рекурсия** — правая часть `>>` вычисляется только при необходимости
- **Переиспользование кода** — общий паттерн для повторения
- **Читаемость** — явная семантика "повторяй"

---

## Частичное выполнение

### Операция `take`

```dotty
def take(n: Int): Pull[O, Option[R]] =
  if n <= 0 then Result(None)
  else step match
    case Left(r) => Result(Some(r))
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

**Ключевое отличие:** Использует `flatMap` вместо непосредственного вызова `step`

---

## Трансформации потоков

### Преобразование элементов (`mapOutput`)

```dotty
def mapOutput[O2](f: O => O2): Pull[O2, R] =
  uncons.flatMap:
    case Left(r)         => Result(r)
    case Right((hd, tl)) => Output(f(hd)) >> tl.mapOutput(f)
```

**Семантика:** "Применить f к каждому элементу потока"

**Пример:**

```dotty
Pull.fromList(List(1, 2, 3)).mapOutput(_ * 2).toList
// List(2, 4, 6)
```

**Особенности:**

- **Ленивое выполнение** — преобразования применяются по мере необходимости
- **Сохранение структуры** — результат имеет тот же тип завершения `R`

### Фильтрация (`filter`)

```dotty
def filter(p: O => Boolean): Pull[O, R] =
  uncons.flatMap:
    case Left(r) => Result(r)
    case Right((hd, tl)) =>
      (if p(hd) then Output(hd) else Pull.done) >> tl.filter(p)
```

**Стратегия:** Пропустить элемент если предикат false, иначе вывести

**Пример:**

```dotty
Pull.fromList(List(1, 2, 3, 4)).filter(_ % 2 == 0).toList
// List(2, 4)
```

---

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

**Альтернатива — только итог:**

```dotty
def totalCount: Pull[Nothing, Int] =
  fold(0)((count, _) => count + 1).map(_._1)
```

### Обобщенный `mapAccumulate`

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

---

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





### Базовые строительные блоки Pull

#### Пустой Pull

Самый простой `Pull` - это `Pull`, который ничего не выводит и завершается с результатом `Unit`:

```dotty
val done: Pull[Nothing, Unit] = Pull.Result(())
```

Это аналог:

- `Nil` для списков
- `None` для `Option`
- Пустого потока в других системах

#### Создание Pull с выходными значениями

Мы можем создавать `Pull`, которые выводят конкретные значения:

```dotty
val single: Pull[Int, Unit] = Pull.Output(1)
```

Это аналогично:

- `List(1)` для списков
- Потоку с одним элементом

### Комбинирование Pull-операций

#### Последовательное соединение (>>)

Оператор `>>` соединяет два `Pull` последовательно:

```dotty
val combined: Pull[Int, Unit] = Pull.Output(1) >> Pull.Output(2)
```

Это работает как:

1. Выполнить первый `Pull` (выводит 1)
2. Затем выполнить второй `Pull` (выводит 2)

Эквивалент через `flatMap`:

```dotty
val combined = Pull.Output(1).flatMap(_ => Pull.Output(2))
```

#### Преобразование в список

`Pull` можно преобразовать в обычный список:

```dotty
val p: Pull[Int, Unit] = Pull.Output(1) >> Pull.Output(2)
val asList: List[Int] = p.toList  // List(1, 2)
```

### Построение Pull из коллекций

#### Из обычного списка (List)

```dotty
def fromList[O](list: List[O]): Pull[O, Unit] =
  list match
    case Nil          => Pull.done
    case head :: tail => Pull.Output(head) >> fromList(tail)
```

Как это работает:

- Для пустого списка возвращаем `done`
- Для непустого - выводим первый элемент и рекурсивно обрабатываем хвост

Пример:

```dotty
fromList(List(1, 2, 3)).toList  // List(1, 2, 3)
```

#### Из ленивого списка (LazyList)

```dotty
def fromLazyList[O](lazyList: LazyList[O]): Pull[O, Unit] =
  lazyList match
    case LazyList()    => Pull.done
    case head #:: tail => Pull.Output(head) >> fromLazyList(tail)
```

Особенности:

- Обрабатывает бесконечные потоки (ограничено только памятью)
- Элементы вычисляются лениво

Пример:

```dotty
val infinite = LazyList.from(1)
fromLazyList(infinite.take(3)).toList // List(1, 2, 3)
```

## Обобщенное создание Pull (unfold)

Шаблон `unfold` - это мощный способ создания `Pull`:

```dotty
def unfold[O, R](init: R)(f: R => Either[R, (O, R)]): Pull[O, R] =
  f(init) match
    case Left(result) => Pull.Result(result)
    case Right((output, nextState)) =>
      Pull.Output(output) >> unfold(nextState)(f)
```

Как это работает:

1. Начинаем с начального состояния `init`
2. Применяем функцию `f`:
    - Если возвращает `Left(result)` - завершаем с этим результатом
    - Если `Right((output, nextState))` - выводим `output` и продолжаем с `nextState`

Пример (генерация чисел Фибоначчи):

```dotty
val fibs: Pull[Int, Unit] = unfold((0, 1)) { case (a, b) =>
  Right((a, (b, a + b)))
}

fibs.take(5).toList  // List(0, 1, 1, 2, 3)
```

### Особенности реализации

1. **Ленивость**: `>>` не вычисляет второй `Pull` сразу, что позволяет работать с бесконечными потоками
2. **Безопасность стека**: рекурсия в `unfold` хвостовая и оптимизирована
3. **Композиционность**: `Pull` можно свободно комбинировать

### Практическое применение

Эти строительные блоки позволяют:

- Преобразовывать существующие коллекции в потоки
- Создавать собственные генераторы данных
- Комбинировать несколько потоков вместе
- Обрабатывать данные по мере поступления без загрузки в память

## Бесконечные потоки с `continually`

### Базовая реализация

Метод `continually` создает бесконечный поток, повторяющий одно значение:

```dotty
def continually[A](a: A): Pull[A, Nothing] =
  Output(a) >> continually(a)
```

Ключевые особенности:

- Тип результата `Nothing` показывает, что поток никогда не завершается
- Использует рекурсию для бесконечного повторения
- Ленивое выполнение благодаря оператору `>>`

Пример использования:

```dotty
val ones: Pull[Int, Nothing] = Pull.continually(1)
```

### Улучшенная версия через `repeat`

Мы можем выделить общий паттерн повторения:

```dotty
enum Pull[+O, +R]:
  def repeat: Pull[O, R] =
    this >> repeat

object Pull:
  def continually[A](a: A): Pull[A, Nothing] =
    Output(a).repeat
```

Преимущества:

- Более ясная семантика
- Повторное использование кода
- Та же ленивая природа

## Частичное выполнение потоков

### Операция `take`

Позволяет ограничить поток N элементами:

```dotty
def take(n: Int): Pull[O, Option[R]] =
  if n <= 0 then Result(None)
  else step match
    case Left(r) => Result(Some(r))
    case Right((hd, tl)) => Output(hd) >> tl.take(n - 1)
```

Как это работает:

1. Если n ≤ 0 - сразу завершаем в виде `Result(None)`
2. Иначе разбираем поток на заглавный элемент и остаток
3. Для каждого элемента уменьшаем счетчик
4. Сохраняем конечный результат, завернутый в `Some`, если поток завершился

Пример:

```dotty
Pull.continually(1).take(3).toList  
// List(1, 1, 1)
```

### Операция `uncons`

Однако предыдущая реализация `take` имеет одну особенность:
когда `n` положительно, немедленно вычисляется каждый шаг исходного `pull`.

Поэтому сформулируем базовый строительный блок для частичного выполнения:

```dotty
def uncons: Pull[Nothing, Either[R, (O, Pull[O, R])]] =
  Pull.done >> Result(step)
  
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

## Трансформации потоков

### Преобразование элементов потока

Преобразует каждый элемент потока:

```dotty
def mapOutput[O2](f: O => O2): Pull[O2, R] =
  uncons.flatMap:
    case Left(r)         => Result(r)
    case Right((hd, tl)) => Output(f(hd)) >> tl.mapOutput(f)
```

Пример:

```dotty
Pull.fromList(List(1, 2, 3)).mapOutput(_ * 2).toList 
// List(2, 4, 6)
```

### Фильтрация элементов потока

Фильтрует элементы по условию:

```dotty
def filter(p: O => Boolean): Pull[O, R] =
  uncons.flatMap:
    case Left(r) => Result(r)
    case Right((hd, tl)) =>
      (if p(hd) then Output(hd) else Pull.done) >> tl.filter(p)
```

Особенности:

- Пропускает элементы, не удовлетворяющие предикату
- Сохраняет структуру потока
- Ленивое выполнение

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

Как работает:

1. Начинает с 0
2. Для каждого элемента увеличивает счетчик
3. Выдает текущее количество после каждого элемента

Пример:

```dotty
Pull.fromList(List("a", "b", "c")).count.toList
// List(0, 1, 2, 3)
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

Пример (сумма элементов):

```dotty
Pull.fromList(List(1, 2, 3)).mapAccumulate(0)((sum, x) => (sum + x, sum + x)).toList
// List(1, 3, 6)
```

> Вышеперечисленные примитивы образуют мощный DSL для потоковой обработки данных в функциональном стиле.

## Упражнения

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
Оператор `>>` (flatMap) связывает шаги, гарантируя корректную ленивую обработку.
Тип `Nothing` указывает, что поток не завершается ошибкой.

```dotty
def iterate[O](initial: O)(f: O => O): Pull[O, Nothing] =
  Output(initial) >> iterate(f(initial))(f)
```

@:@

### Упражнение 11.2.3: Реализация операторов drop и while для Pull

Реализуйте методы `drop`, `takeWhile`, и `dropWhile`:

```dotty
def drop(n: Int): Pull[O, R] = ???
def takeWhile(p: O => Boolean): Pull[O, Pull[O, R]] = ???
def dropWhile(f: O => Boolean): Pull[O, R] = ???
```

**Пояснение:**

- `drop(n)` - пропускает первые `n` элементов потока
- `takeWhile(p)` - возвращает элементы пока предикат `p` истинен, затем останавливается
- `dropWhile(p)` - пропускает элементы пока предикат `p` истинен, затем возвращает оставшиеся
- Все методы работают через рекурсивное разворачивание потока (`uncons`)

**Примеры использования:**

```dotty
val nums = Pull.fromList(List(1, 2, 3, 4, 5))

nums.drop(2).toList          // List(3, 4, 5)
nums.takeWhile(_ < 4).toList // List(1, 2, 3)
nums.dropWhile(_ < 3).toList // List(3, 4, 5)
```

@:note(🔍 Показать решение)

Эти методы реализуют базовые операции над ленивыми потоками (`Pull`):
`drop` пропускает первые `n` элементов через рекурсивное разворачивание потока (`uncons`), возвращая оставшуюся часть;
`takeWhile` последовательно выводит элементы, удовлетворяющие предикату `f`, а затем возвращает остаток потока;
`dropWhile` пропускает элементы, пока они удовлетворяют `f`, а затем возвращает оставшиеся.
Все операции работают лениво через `flatMap` и рекурсию, сохраняя потоковую природу `Pull`,
где `Result` завершает обработку, а `Output` эмитит значения.
Типы результатов (`Pull[O, R]` или вложенные `Pull`) обеспечивают корректную композицию.

```dotty
def drop(n: Int): Pull[O, R] =
  if n <= 0 then this
  else
    uncons.flatMap:
      // Конец потока
      case Left(r) => Result(r)
      // Пропускаем текущий элемент
      case Right((_, tl)) => tl.drop(n - 1)

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

### Упражнение 11.2.4: Реализация countViaMapAccumulate

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


---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
