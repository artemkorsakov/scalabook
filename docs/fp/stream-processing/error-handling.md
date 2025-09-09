{%
showPrev = true
prevPage = expandable.html
showNext = true
nextPage = resource.html
%}

# Обработка ошибок

Некоторые типы эффектов предоставляют возможность обработки ошибок, возникающих во время оценки.
Код главы 13 определяет `Task[A]` тип как непрозрачный тип вокруг `IO[Try[A]]`.
`Task` позволяет нам игнорировать наличие потенциальных ошибок, пока мы не захотим их обработать:

```dotty
val a: Task[Int] = Task("asdf".toInt)
val b: Task[Int] = a.map(_ + 1)
val c: Task[Try[Int]] = b.attempt
val d: Try[Int] = c.unsafeRunSync(pool)
```

Когда задача, назначенная для `a` оценивается, преобразование из строки в целое число завершается ошибкой с `NumberFormatException`.
`Task` обрабатывает это исключение и сохраняет его внутри, обернутым в `Failure`
(напомним, что `Failure` это конструктор данных `Try`).
В определении `b` вызов `map` оказывается пустой операцией.
В определении `c` вызов `attempt` раскрывает базовый `Try[Int]`.

`Task` добавляет канал ошибок в `IO` — канал для передачи ошибок, которые произошли в ходе вычислений,
во многом таким же образом, как `Try` и `Either` обеспечивает неэффективные вычисления.
Мы можем определить `Monad[Task]` экземпляр, который работает так же, как экземпляры монад для `Try` и `Either`,
замыкаясь при столкновении с `Failure`.
`Task` также предоставляет `handleErrorWith` комбинатор, полученный из `attempt` и `flatMap`:

```dotty
def handleErrorWith(h: Throwable => Task[A]): Task[A] =
  attempt.flatMap:
    case Failure(t) => h(t)
    case Success(a) => Task(a)

val a: Task[Int] = Task("asdf".toInt)
val b: Task[Int] = a.map(_ + 1)
val c: Task[Int] = b.handleErrorWith(_ => Task(0))
```

`handleErrorWith` чем-то похож на `flatMap` в том, что он обеспечивает последовательность,
но канала ошибок вместо канала результатов.
Что происходит, когда мы используем `Task` with `Stream`?
Мы можем использовать `eval` так же, как мы делали с `IO`:

```dotty
val s: Stream[Task, Int] = Stream.eval(Task("asdf".toInt))
```

Когда мы преобразуем этот поток в задачу через один из элиминаторов потока (например, `toList` или `run`),
мы получим ошибку, которая оборачивает `NumberFormatException`.
Полезно иметь возможность обрабатывать такие ошибки как часть API `Stream` и `Pull`,
поэтому давайте добавим `handleErrorWith` к каждому:

```dotty
enum Pull[+F[_], +O, +R]:
  case Handle(
    source: Pull[F, O, R],
    handler: Throwable => Pull[F, O, R]
  ) extends Pull[F, O, R]

  def handleErrorWith[F2[x] >: F[x], O2 >: O, R2 >: R](
    handler: Throwable => Pull[F2, O2, R2]): Pull[F2, O2, R2] =
    Pull.Handle(this, handler)

extension [F[_], O](self: Stream[F, O])
  def handleErrorWith(handler: Throwable => Stream[F, O]): Stream[F, O] =
    Pull.Handle(self, handler)
```

Мы добавили новый конструктор в `Pull`, который захватывает исходный pull и обработчик ошибок.
Затем нам нужно расширить определение `step` для интерпретации этого `Handle` конструктора.
При выполнении шага и обнаружении `Handle`, нам нужно выполнить шаг внутреннего pull и обработать любые ошибки,
которые возникли при этом.
Но `step` определено для работы с произвольным эффектом `F[_]`, имеющим экземпляр монады,
а монада не предоставляет возможности обработки ошибок. Чтобы решить эту проблему,
давайте введем новый класс типов, который добавляет возможности обработки ошибок к `Monad`:

```dotty
trait MonadThrow[F[_]] extends Monad[F]:
  extension [A](fa: F[A])
    def attempt: F[Try[A]]
    def handleErrorWith(h: Throwable => F[A]): F[A] =
      attempt.flatMap:
        case Failure(t) => h(t)
        case Success(a) => unit(a)

  def raiseError[A](t: Throwable): F[A]
```

`MonadThrow` расширяет `Monad` и добавляет возможность выявлять ошибки и впоследствии обрабатывать их.

### Упражнение 15.12

Определите законы для `MonadThrow`. Рассмотрите, как `attempt` следует взаимодействовать с `raiseError` и `unit`.

Теперь мы можем обновить определение слова `step` «взять» пример `MonadThrow`:

```dotty
def step[F2[x] >: F[x], O2 >: O, R2 >: R](
  using F: MonadThrow[F2]
): F2[Either[R2, (O2, Pull[F2, O2, R2])]] =
  this match
    ...
    case Handle(source, f) =>
      source match
        case Handle(s2, g) =>
          s2.handleErrorWith(x =>
            g(x).handleErrorWith(y => f(y))).step #1
        case other =>
          other.step
            .map:
              case Right((hd, tl)) => Right((hd, Handle(tl, f)))
              case Left(r) => Left(r)
            .handleErrorWith(t => f(t).step) #2
```

Эта реализация сначала проверяет наличие левовложенных обработчиков ошибок
и переписывает такое вложение в правовложенное, как мы делаем для левовложенных вызовов `flatMap`.
В противном случае она выполняет исходное извлечение и обрабатывает любые возникшие ошибки
путем делегирования методу `handleErrorWith` для целевой эффект `F`.
Если ошибок не происходит, то мы передаем обработчик ошибок на оставшийся pull.

В результате `step` принятия `MonadThrow`,
все различные элиминаторы `MonadThrow` также должны принимать: `fold`, `toList`, и `run`.
Мы можем аналогичным образом добавить поддержку явного вызова ошибок в pull и stream, введя `raiseError` конструктор для каждого.
Смотрите код главы для получения полной информации.

Теперь, когда у нас есть `handleErrorWith` и `raiseError` для потока, мы можем определить `onComplete`:

```dotty
extension [F[_], O](self: Stream[F, O])
  def onComplete(that: => Stream[F, O]): Stream[F, O] =
    self.handleErrorWith(t => that ++ raiseError(t)) ++ that
```

Метод `onComplete` позволяет нам оценивать поток после завершения исходного потока, независимо от того,
завершился ли исходный поток успешно или завершился с ошибкой. Это особенно эффективно,
когда переданный поток `onComplete` оценивает эффект.

Позволяет ли `onComplete` нам безопасно получать и освобождать ресурсы, такие как дескрипторы файлов?
Давайте посмотрим:

```dotty
def acquire(path: String): Task[Source] =
  Task(Source.fromFile(path))

def use(source: Source): Stream[Task, Unit] =
  Stream.eval(Task(source.getLines))
    .flatMap(itr => Stream.fromIterator(itr))
    .mapEval(line => Task(println(line)))

def release(source: Source): Task[Unit] =
  Task(source.close()))

val printLines: Stream[Task, Unit] =
  Stream.eval(acquire("path/to/file")).flatMap(resource =>
    use(resource).onComplete(Stream.eval(release(resource))))
```

Сначала мы оцениваем `acquire` задачу открытия файла, а затем получаем `flatMap` результат,
создавая поток, который использует открытый файл и освобождает его через `onComplete`.
Затем мы выводим каждую строку файла на консоль.
Если мы запустим это, возможно, с некоторым дополнительным протоколированием получения
и завершения ресурсов, мы увидим, что это делает именно то, что нам нужно.

К сожалению, этот подход имеет серьезное ограничение: он некомпозируемый.
Давайте рассмотрим, что произойдет, если мы немного рефакторим все, извлекая вывод каждой строки до вызова `onComplete`:

```dotty
val printLines: Stream[Task, Unit] =
  Stream.eval(acquire("path/to/file"))
    .flatMap: resource =>
      Stream.eval(source => Task(source.getLines))
        .flatMap(itr => Stream.fromIterator(itr))
        .onComplete(Stream.eval(release(resource)))
    .mapEval(line => Task(println(line)))
```

Эта программа должна вести себя так же, как и оригинал, и на самом деле так и происходит, когда нет ошибок.
Однако, если финал `mapEval` завершается с исключением, то обработчик ошибок, зарегистрированный с помощью `onComplete`,
не вызывается, общее вычисление завершается неудачей, и ресурс никогда не финализируется.

Другим проблемным случаем является раннее завершение в результате взятия только некоторых элементов исходного потока.
Представьте, что мы изменили `use` так, чтобы взять первые 10 строк файла.
Если мы поместим `take(10)` в тело `use`, то весь поток будет вести себя правильно.
Однако если мы перейдем `take(10)` к вызову `onComplete`, то первые 10 строк будут возвращены,
и ресурс никогда не будет завершен.

В обоих этих случаях мы ожидаем, что ресурс будет завершен независимо от того, что еще происходит в потоке.
Опираться только на `flatMap` и `onComplete` не дает нам достаточно власти,
чтобы предоставить эту гарантию — мы вернемся к этой гарантии в ближайшее время.

Добавление поддержки обработки ошибок `Stream` приводит к более сильному ограничению на формы его устранения.
Следовательно, больше невозможно преобразовать a `Stream[IO, X]` в an `IO[List[X]]`
или любое другое `IO` значение, поскольку `IO` не имеет экземпляр `MonadThrow`.
Правильный ли это компромисс? На практике большинство типов монадических эффектов поддерживают обработку ошибок,
поэтому это ограничение не такое уж ограничивающее, как кажется на первый взгляд,
но стоит поэкспериментировать с другими вариантами API.

А как насчет неэффективных потоков?
Ранее мы оценивали их в `TailRec` монаде, которая также не имеет `MonadThrow` экземпляра.
Это решается проще — мы можем добавить обработку ошибок в `TailRec` во многом таким же образом,
как мы добавили ее в `IO`, встроив `Try` внутрь него:

```dotty
opaque type SyncTask[A] = TailRec[Try[A]]
```

Мы можем определить `MoandThrow[SyncTask]` экземпляр и изменить неэффективные элиминаторы для оценки потоков,
используя `SyncTask` вместо `TailRec`. В общем, каждый раз,
когда мы добавляем дополнительную функциональность к потокам и извлечениям,
мы должны быть осторожны с тем, как эта функциональность изменяет требования к элиминаторам.


---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
