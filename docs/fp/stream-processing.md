{%
showPrev = true
prevPage = local-effects.html
%}

# Потоковая обработка и инкрементный ввод-вывод

Функциональное программирование позволяет выражать любые программы, включая те, что взаимодействуют с внешним миром. 
Однако использование только типа `IO` для таких программ не всегда идеально, так как:

1. `IO` по сути встраивает императивный стиль в функциональный код
2. Теряется композиционность, характерная для чистого ФП
3. Управление ресурсами становится ответственностью программиста

Рассмотрим, как можно сделать лучше на примере задачи подсчета строк в файле.

## Проблема императивного подхода

### Пример на `IO`

```dotty
def linesGt40k(filename: String): IO[Boolean] = IO:
  // scala.io.Source имеет удобную функцию 
  // для чтения из внешних источников, таких как файлы.
  val src = io.Source.fromFile(filename) 
  try
    var count = 0
    // Получить итератор с отслеживанием состояния источника.
    val lines: Iterator[String] = src.getLines() 
    while count <= 40000 && lines.hasNext do
      // Имеет побочный эффект перехода к следующему элементу.
      lines.next 
      count += 1
    count > 40000
  finally src.close
```

**Плюсы:**

- Инкрементальность - не загружает весь файл в память
- Досрочное завершение - останавливается при достижении 40000 строк

**Минусы:**

1. **Утечки ресурсов** - если забыть `finally`, файл останется открытым
2. **Смешение логики** - алгоритм подсчета перемешан с управлением ресурсами
3. **Некомпозируемость** - трудно адаптировать для других задач

### Проблемы безопасности ресурсов

Ресурсы (файлы, сокеты, соединения БД) должны:

- Закрываться вовремя (не слишком рано, не слишком поздно)
- Не использоваться после закрытия
- Не "утекать" при выдаче исключений

В императивном коде это полностью наша ответственность.

## Функциональная альтернатива

Хотим писать код в таком стиле:

```dotty
lines.filter(_.nonEmpty).take(40000).exists(condition)
```

Но с реальными файлами, а не с готовыми коллекциями.

### Наивное решение: Ленивый IO

```dotty
def lines(filename: String): IO[LazyList[String]] = IO {
  val src = io.Source.fromFile(filename)
  src.getLines().to(LazyList).append {
    src.close()
    LazyList.empty
  }
}
```

**Проблемы:**

1. Ресурсы освобождаются только при полном чтении потока
2. Возможны утечки, если поток не дочитан до конца
3. Повторное чтение после закрытия файла приведет к ошибкам
4. Побочные эффекты при неожиданных моментах

### Принципы хорошего решения

Нужно решение, которое:

1. **Композиционно** - позволяет комбинировать простые операции
2. **Ресурсобезопасно** - автоматически управляет жизненным циклом ресурсов
3. **Инкрементально** - обрабатывает данные по мере необходимости
4. **Чисто** - без скрытых побочных эффектов

### Как этого достичь?

Идея: разделить описание потоковой обработки и ее исполнение.

1. Описываем потоковые операции как значения
2. Исполняем с гарантией правильного управления ресурсами

Примерный API:

```dotty
Stream.resource(openFile)(file =>
  Stream.fromIterator(file.lines)
    .filter(_.nonEmpty)
    .take(40000)
    .exists(_.startsWith("foo"))
).compile.toList
```

Где:

- `Stream` описывает операции
- `resource` гарантирует закрытие файла
- `compile.toList` выполняет весь пайплайн

### Выводы

Функциональные потоковые библиотеки (например, [Functional Streams for Scala (FS2)](https://fs2.io)) позволяют:

- Сохранять композиционность чистого кода
- Автоматически управлять ресурсами
- Эффективно обрабатывать большие/бесконечные потоки
- Избегать скрытых побочных эффектов

Это дает преимущества ФП даже для IO-интенсивных задач.

## Простые потоковые преобразования

Давайте разберем концепцию потоковых преобразований.

### Что такое потоковый процессор?

Потоковый процессор - это преобразователь данных, который:

- Принимает поток (последовательность) входных данных
- Обрабатывает их по мере поступления
- Возвращает новый поток выходных данных

Примеры потоков:

- Строки из файла
- HTTP-запросы к серверу
- Координаты кликов мыши
- Показания датчиков

### Тип данных `Pull`

```dotty
enum Pull[+O, +R]:
  case Result[+R](result: R) extends Pull[Nothing, R]
  case Output[+O](value: O)  extends Pull[O, Unit]
  case FlatMap[X, +O, +R](
      source: Pull[O, X],
      f: X => Pull[O, R]
  ) extends Pull[O, R]

  def step: Either[R, (O, Pull[O, R])] = this match
    case Result(r) => Left(r)
    case Output(o) => Right(o, Pull.Result(()))
    case FlatMap(source, f) =>
      source match
        case FlatMap(s2, g) =>
          s2.flatMap(x => g(x).flatMap(y => f(y))).step
        case other => other.step match
            case Left(r)         => f(r).step
            case Right((hd, tl)) => Right((hd, tl.flatMap(f)))

  @annotation.tailrec
  final def fold[A](init: A)(f: (A, O) => A): (R, A) =
    step match
      case Left(r)         => (r, init)
      case Right((hd, tl)) => tl.fold(f(init, hd))(f)

  def toList: List[O] =
    fold(List.newBuilder[O])((bldr, o) => bldr += o)(1).result

  def flatMap[O2 >: O, R2](
      f: R => Pull[O2, R2]
  ): Pull[O2, R2] =
    Pull.FlatMap(this, f)

  def >>[O2 >: O, R2](next: => Pull[O2, R2]): Pull[O2, R2] =
    flatMap(_ => next)

  def map[R2](f: R => R2): Pull[O, R2] =
    flatMap(r => Result(f(r)))
end Pull
```

`Pull[O, R]` - это основа для построения потоковых преобразований, где:

- `O` (Output) - тип выходных элементов
- `R` (Result) - тип конечного результата

`Pull[O, R]` выводит любое количество значений типа `O` и затем завершается одним значением типа `R`.

### Основные компоненты Pull

1. **Result(r: R)** - конечный результат обработки
   ```dotty
   Pull.Result(42) // Завершается с результатом 42
   ```

2. **Output(value: O)** - выдает одно значение в поток
   ```dotty
   Pull.Output("Hello") // Выдает строку "Hello" в поток
   ```

3. **FlatMap** - комбинирует Pull-операции последовательно
   ```dotty
   Pull.Output(1).flatMap(x => Pull.Output(x + 1)) // 1 → 2
   ```

### Как работает Pull?

1. **Пошаговое выполнение (step)**:
    - `Pull` выполняется по шагам
    - Каждый шаг либо:
        - Возвращает результат (`Left(r)`)
        - Или выдает значение и продолжение (`Right(o, nextPull)`)

   Пример:
   ```dotty
   val p = Pull.Output(1) >> Pull.Output(2)
   p.step // Right((1, Pull.Output(2)))
   ```

2. **Накопление результатов (fold)**:
    - Проходит по всем элементам
    - Применяет функцию к аккумулятору
    - Возвращает конечный результат и аккумулятор

   Пример:
   ```dotty
   val p = Pull.Output(1) >> Pull.Output(2)
   p.fold(0)(_ + _) // ((), 3) - () потому что результат Unit
   ```

3. **Преобразование в список (toList)**:
   ```dotty
   Pull.Output(1) >> Pull.Output(2).toList // List(1, 2)
   ```

### Комбинаторы Pull

1. **flatMap** - последовательное выполнение:
   ```dotty
   Pull.Output(1).flatMap: x => 
     Pull.Output(x * 2)
   // Выдает 2
   ```

2. **>> (then)** - выполнить следующее действие:
   ```dotty
   Pull.Output(1) >> Pull.Output(2) // 1, затем 2
   ```

3. **map** - преобразовать результат:
   ```dotty
   Pull.Result(1).map(_ * 2) // Result(2)
   ```

### Почему это полезно?

1. **Ленивость** - данные обрабатываются по мере необходимости
2. **Композиционность** - можно комбинировать простые операции
3. **Безопасность** - контроль над выполнением

Пример обработки файла:

```dotty
def fileLines(file: File): Pull[String, Unit] = 
  readLine(file).flatMap {
    case None => Pull.Result(())
    case Some(line) => Pull.Output(line) >> fileLines(file)
  }
```

`Pull` дает нам:

- Простой способ описывать потоковые преобразования
- Возможность комбинировать операции
- Контроль над выполнением без лишних эффектов

Это фундамент для построения более сложных потоковых API в функциональном стиле.

## Создание Pull-операций

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
- Ленивая выполнение благодаря оператору `>>`

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

### Упражнение 11.1.1: Реализация fromListViaUnfold и fromLazyListViaUnfold

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

### Упражнение 11.1.2: Создание бесконечного потока с iterate

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

### Упражнение 11.1.3: Реализация операторов drop и while для Pull

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

### Упражнение 11.1.4: Реализация countViaMapAccumulate

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
