{%
showPrev = true
prevPage = local-effects.html
showNext = true
nextPage = stream-processing/monad-pull.html
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

### Обобщенное создание Pull (unfold)

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



### `continually`

Как мы это сделали для `LazyList`, мы можем создать конструктор `continually`:

```dotty
def continually[A](a: A): Pull[A, Nothing] =
  Output(a) >> continually(a)
```

Тип возврата `continually` интересен — тип результата — `Nothing`,
поскольку pull бесконечен и, следовательно, никогда не заканчивается значением.
Мы снова можем извлечь рекурсию в более общий комбинатор:

```dotty
enum Pull[+O, +R]:
  def repeat: Pull[O, R] =
    this >> repeat

object Pull:
  def continually[A](a: A): Pull[O, Nothing] =
    Output(a).repeat
```


### Частичная оценка

Пока что у нас нет способа частично оценить pull.
Частичная оценка особенно полезна для бесконечных pull,
таких как возвращаемые из `continually`, `repeat`, и `iterate`.
Давайте определим `take(n)` на `Pull`, который оценивает pull до тех пор,
пока не будет сгенерировано указанное количество выходных значений:

```dotty
def take(n: Int): Pull[O, Option[R]] =
  if n <= 0 then Result(None)
  else step match
    case Left(r) => Result(Some(r))
    case Right((hd, tl)) => Output(hd) >> tl.take(n - 1)
```

Когда `n` меньше или равно нулю, `take` возвращает `Result(None)`.
В противном случае мы выполняем шаг pull;
если мы достигли конца ввода, то возвращаем конечный результат источника, завернутый в `Some`.
В противном случае мы получаем выходное значение, которое мы выводим, и остаток pull,
который мы рекурсивно вызываем `take`, уменьшая количество элементов.

Однако эта реализация имеет одну тонкость: когда `n` положительно, мы с нетерпением шагаем по исходному pull.
Эта с нетерпением оценка может быть удивительной,
поскольку оценка в настоящее время происходит только при сворачивании pull
(или использовании операции, например `toList`, которая построена на `fold`),
поэтому давайте отложим эту оценку с помощью нового комбинатора на `Pull`:

```dotty
def uncons: Pull[Nothing, Either[R, (O, Pull[O, R])]] =
  Pull.done >> Result(step)

def take(n: Int): Pull[O, Option[R]] =
  if n <= 0 then Result(None)
  else uncons.flatMap:
    case Left(r) => Result(Some(r))
    case Right((hd, tl)) => Output(hd) >> tl.take(n - 1)
```

Операция `uncons` оборачивает `step` конструктор `Result` и откладывает его создание через `flatMap`.
В реализации `take` вместо сопоставления с результатом `step` мы теперь `flatMap` сопоставляем с результатом `uncons`.
С этим определением мы можем создавать частично оцененные бесконечные потоки:

```dotty
scala> val ints = Pull.iterate(0)(_ + 1)
val ints: Pull[Int, Nothing] = FlatMap(Output(0),...)

scala> val firstFive = ints.take(5).toList
val firstFive: List[Int] = List(0, 1, 2, 3, 4)
```


Мы можем построить множество интересных преобразований pull, комбинируя рекурсивные функции с `uncons`.
Вот как мы можем преобразовать выходные элементы pull:

```dotty
def mapOutput[O2](f: O => O2): Pull[O2, R] =
  uncons.flatMap:
    case Left(r) => Result(r)
    case Right((hd, tl)) => Output(f(hd)) >> tl.mapOutput(f)
```

Мы можем использовать тот же подход для добавления или удаления элементов pull.
Вот комбинатор, который фильтрует выходные элементы на основе предиката:

```dotty
def filter(p: O => Boolean): Pull[O, R] =
  uncons.flatMap:
    case Left(r) => Result(r)
    case Right((hd, tl)) =>
      (if p(hd) then Output(hd) else Pull.done) >> tl.filter(p)
```

Мы даже можем сохранять состояние в наших преобразованиях pull,
передавая состояние в качестве аргумента нашей рекурсивной функции:

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


Как мы уже много раз видели в этой книге, когда мы замечаем общие закономерности при определении ряда функций,
мы можем разложить эти закономерности на универсальные комбинаторы.
Функции `count`, `tally`, и `slidingMean` все имеют общую закономерность:
каждая имеет одну часть состояния, имеет функцию перехода состояния,
которая обновляет это состояние в ответ на ввод и производит один вывод.
Мы можем обобщить это до комбинатора, который мы назовем `mapAccumulate`:

```dotty
def mapAccumulate[S, O2](init: S)(f: (S, O) => (S, O2)): Pull[O2, (S, R)] =
  uncons.flatMap:
    case Left(r) => Result((init, r))
    case Right((hd, tl)) =>
      val (s, out) = f(init, hd)
      Output(out) >> tl.mapAccumulate(s)(f)
```


## Упражнения

### Упражнение 11.1.1: ???

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

**Задача:**

Реализуйте `fromListViaUnfold` и `fromLazyListViaUnfold` как методы на `Pull` сопутствующем объекте, используя `unfold`:

```dotty
def fromListViaUnfold[O](os: List[O]): Pull[O, Unit]
def fromLazyListViaUnfold[O](os: LazyList[O]): Pull[O, Unit]
```

@:note(🔍 Показать решение)

???

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

### Упражнение 11.1.2: ???

Определить `iterate` как метод на `Pull` сопутствующем объекте. 
Он должен возвращать бесконечный поток, который сначала выводит предоставленное начальное значение, 
затем выводит результат применения этого значения к предоставленной функции и т.д.:

```dotty
def iterate[O](initial: O)(f: O => O): Pull[O, Nothing]
```

@:note(🔍 Показать решение)

???

```dotty
def iterate[O](initial: O)(f: O => O): Pull[O, Nothing] =
  Output(initial) >> iterate(f(initial))(f)
```

@:@

### Упражнение 11.1.3: ???

Реализовать `drop`, `takeWhile`, и `dropWhile` на `Pull`:

```dotty
def drop(n: Int): Pull[O, R]
def takeWhile(p: O => Boolean): Pull[O, Pull[O, R]]
def dropWhile(p: O => Boolean): Pull[Nothing, Pull[O, R]]
```

@:note(🔍 Показать решение)

???

```dotty
def drop(n: Int): Pull[O, R] =
  if n <= 0 then this
  else uncons.flatMap:
    case Left(r) => Result(r)
    case Right((_, tl)) => tl.drop(n - 1)

def takeWhile(f: O => Boolean): Pull[O, Pull[O, R]] =
  uncons.flatMap:
    case Left(r) => Result(Result(r))
    case Right((hd, tl)) =>
      if f(hd) then Output(hd) >> tl.takeWhile(f)
      else Result(Output(hd) >> tl)

def dropWhile(f: O => Boolean): Pull[Nothing, Pull[O, R]] =
  uncons.flatMap:
    case Left(r) => Result(Result(r))
    case Right((hd, tl)) =>
      if f(hd) then tl.dropWhile(f)
      else Result(Output(hd) >> tl)
```

@:@

### Упражнение 11.1.4: ???

Реализуйте метод, `Pull` который выводит текущий счет всех входов, используя моноид для объединения элементов. 
Для каждого входного элемента объедините его с накопленным счетом, выведя результат:

```dotty
def tally[O2 >: O](используя m: Monoid[O2]): Pull[O2, R]

scala> значение t = ints.tally.take(10).toList
значение t: Список[Int] = Список(0, 0, 1, 3, 6, 10, 15, 21, 28, 36)
```

@:note(🔍 Показать решение)

???

```dotty
def tally[O2 >: O](using m: Monoid[O2]): Pull[O2, R] =
  def go(total: O2, p: Pull[O, R]): Pull[O2, R] =
    p.uncons.flatMap:
      case Left(r) => Result(r)
      case Right((hd, tl)) =>
        val newTotal = m.combine(total, hd)
        Output(newTotal) >> go(newTotal, tl)
  Output(m.empty) >> go(m.empty, this)
```

@:@

### Упражнение 11.1.5: ???

Реализуйте метод расширения для `Pull[Int, R]`, который выводит среднее значение последних `n` значений в виде `Pull[Double, R]`:

```dotty
extension [R](self: Pull[Int, R])
  def slidingMean(n: Int): Pull[Double, R]
```

@:note(🔍 Показать решение)

???

```dotty
extension [R](self: Pull[Int, R])
  def slidingMean(n: Int): Pull[Double, R] =
    def go(
      window: collection.immutable.Queue[Int],
      p: Pull[Int, R]
    ): Pull[Double, R] =
      p.uncons.flatMap:
        case Left(r) => Result(r)
        case Right((hd, tl)) =>
          val newWindow = if window.size < n then window :+ hd
                                             else window.tail :+ hd
          val meanOfNewWindow = newWindow.sum / newWindow.size.toDouble
          Output(meanOfNewWindow) >> go(newWindow, tl)
    go(collection.immutable.Queue.empty, self)
```

@:@

### Упражнение 11.1.6: ???

Переопределите `count`, `tally`, и `slidingMean`, используя `mapAccumulate` вместо явной рекурсии.

@:note(🔍 Показать решение)

???

```dotty
def countViaMapAccumulate: Pull[Int, R] =
  Output(0) >> mapAccumulate(0)((s, o) => (s + 1, s + 1)).map(_(1))

def tallyViaMapAccumulate[O2 >: O](using m: Monoid[O2]): Pull[O2, R] =
  Output(m.empty) >> mapAccumulate(m.empty): (s, o) =>
    val s2 = m.combine(s, o)
    (s2, s2)
  .map(_(1))

extension [R](self: Pull[Int, R])
  def slidingMeanViaMapAccumulate(n: Int): Pull[Double, R] =
    self.mapAccumulate(Queue.empty[Int]): (window, o) =>
      val newWindow = if window.size < n then window :+ o
                                         else window.tail :+ o
      val meanOfNewWindow = newWindow.sum / newWindow.size.toDouble
      (newWindow, meanOfNewWindow)
    .map(_(1))
```

@:@

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/artemkorsakov_scalabook/functional_programming_sample
