# Руководство пользователя

> Если не указано иное, под типом `Stream` в этом документе подразумевается `fs2.Stream`, а НЕ `scala.collection.immutable.Stream`.

## Обзор

Библиотека FS2 обладает двумя основными возможностями:

- Возможность **строить** сколь угодно сложные потоки (streams), возможно, с встроенными эффектами.
- Возможность **преобразовывать** один или несколько потоков с помощью небольшого, но мощного набора операций.

## Построение потоков (Building streams)

Тип `Stream[F, O]` представляет собой дискретный поток значений типа `O`, 
который может запрашивать выполнение эффектов типа `F`. 
Будем называть `F` — **типом эффекта**, а `O` — **типом выходных данных**. Рассмотрим несколько примеров:

```dotty
import fs2.Stream

val s0  = Stream.empty                // пустой поток
val s1  = Stream.emit(1)              // поток из одного элемента
val s1a = Stream(1, 2, 3)             // вариативный конструктор
val s1b = Stream.emits(List(1, 2, 3)) // принимает любую последовательность Seq
```

Поток `s1` имеет тип `Stream[Pure, Int]`. Его выходной тип — `Int`, а тип эффекта — `Pure`, 
что означает, что для получения выходных данных не требуется вычислять никакие эффекты. 
Потоки, не использующие эффекты, называются **чистыми (pure)**. 
Вы можете преобразовать чистый поток в `List` или `Vector` с помощью:

```dotty
s1.toList
s1.toVector
```

У потоков есть множество удобных функций, похожих на функции для списков. Вот лишь небольшой их набор:

```dotty
(Stream(1, 2, 3) ++ Stream(4, 5)).toList
// List(1, 2, 3, 4, 5)
Stream(1, 2, 3).map(_ + 1).toList
// List(2, 3, 4)
Stream(1, 2, 3).filter(_ % 2 != 0).toList
// List(1, 3)
Stream(1, 2, 3).fold(0)(_ + _).toList
// List(6)
Stream(None, Some(2), Some(3)).collect { case Some(i) => i }.toList
// List(2, 3)
Stream.range(0, 5).intersperse(42).toList
// List(0, 42, 1, 42, 2, 42, 3, 42, 4)
Stream(1, 2, 3).flatMap(i => Stream(i, i)).toList
// List(1, 1, 2, 2, 3, 3)
Stream(1, 2, 3).repeat.take(9).toList
// List(1, 2, 3, 1, 2, 3, 1, 2, 3)
Stream(1, 2, 3).repeatN(2).toList
// List(1, 2, 3, 1, 2, 3)
```

Из всех перечисленных операций только `flatMap` является примитивной; 
все остальные построены с помощью комбинаций других примитивов.

До сих пор мы рассматривали только чистые потоки. Потоки FS2 также могут включать вычисление эффектов:

```dotty
import cats.effect.IO

val eff = Stream.eval(IO { println("ВЫПОЛНЯЕТСЯ!!"); 1 + 1 })
```

`IO` — это тип эффекта, который мы будем часто видеть в примерах. Создание значения `IO` не имеет побочных эффектов, 
и `Stream.eval` тоже ничего не делает в момент создания — это всего лишь описание того, что должно произойти, 
когда поток будет в конечном счёте интерпретирован. Обратите внимание, что тип `eff` теперь — `Stream[IO, Int]`.

Функция `eval` работает для любого типа эффекта, а не только для `IO`. 
FS2 безразлично, какой тип эффекта вы используете для своих потоков. 
Вы можете использовать `IO` или привнести свой собственный тип эффекта, реализовав для него несколько интерфейсов 
(например, `cats.MonadError[?, Throwable]`, `cats.effect.Sync`, `cats.effect.Async`, `cats.effect.Concurrent`). 
Вот сигнатура `eval`:

```dotty
def eval[F[_], A](f: F[A]): Stream[F, A]
```

`eval` создаёт поток, который вычисляет переданный эффект, а затем выдает его результат (обратите внимание, что `F` ничем не ограничен). 
Любой поток, созданный с помощью `eval`, называется **эффектным (effectful)** 
и не может быть выполнен с помощью `toList` или `toVector`. 
Если мы попытаемся это сделать, получим ошибку компиляции:

```dotty
eff.toList
// value toList is not a member of fs2.Stream[cats.effect.IO, Int]
```

Вот полный пример выполнения эффектного потока:

```dotty
import cats.effect.unsafe.implicits.global

eff.compile.toVector.unsafeRunSync()
```

Первый вызов `.compile.toVector` — это один из нескольких методов, доступных для **компиляции** потока в единый эффект:

```dotty
val ra = eff.compile.toVector          // собрать все выходные данные в Vector
val rb = eff.compile.drain             // только для побочных эффектов (игнорируем выход)
val rc = eff.compile.fold(0)(_ + _)    // выполнить и накопить некоторый результат
```

Обратите внимание, что все они возвращают значение типа `IO` (или другого эффекта), 
но сам процесс компиляции на самом деле **не выполняет** никаких эффектов (ничего не печатается).

Если мы хотим выполнить эти эффекты «at the end of the universe», 
мы можем использовать один из методов `unsafe*` у `IO` 
(если вы используете свой собственный тип эффекта, способ выполнения может отличаться):

```dotty
ra.unsafeRunSync()
// ВЫПОЛНЯЕТСЯ!!
// Vector(2)
rb.unsafeRunSync()
// ВЫПОЛНЯЕТСЯ!!
rc.unsafeRunSync()
// ВЫПОЛНЯЕТСЯ!!
// 2
rc.unsafeRunSync()   
// повторный запуск выполнит вычисления заново:
// ВЫПОЛНЯЕТСЯ!!
// 2
```

Здесь мы наконец видим, как задачи выполняются. Как показано на примере `rc`, 
повторный запуск задачи приводит к повторному выполнению всего вычисления; ничего не кешируется автоматически.

## Чанки (Chunks)

Потоки FS2 внутренне разбиты на чанки для повышения производительности. 
Вы можете создать отдельный чанк с помощью `Stream.chunk`, который принимает `fs2.Chunk`. 
Многие функции библиотеки учитывают чанки и стараются сохранять их, где это возможно. 
`Chunk` — это строгая, конечная последовательность значений, поддерживающая эффективный индексный доступ к элементам.

```dotty
import fs2.Chunk

val s1c = Stream.chunk(Chunk.array(Array(1.0, 2.0, 3.0)))
```

## Базовые операции над потоками

У потоков есть небольшой, но мощный набор операций, некоторые из которых мы уже видели. 
Ключевые операции: `++`, `map`, `flatMap`, `handleErrorWith` и `bracket`:

```dotty
val appendEx1 = Stream(1, 2, 3) ++ Stream.emit(42)
val appendEx2 = Stream(1, 2, 3) ++ Stream.eval(IO.pure(4))

appendEx1.toVector
// Vector(1, 2, 3, 42)
appendEx2.compile.toVector.unsafeRunSync()
// Vector(1, 2, 3, 4)

appendEx1.map(_ + 1).toList
// List(2, 3, 4, 43)
```

Операция `flatMap` работает так же, как и для списков — она отображает каждый элемент, а затем конкатенирует результаты:

```dotty
appendEx1.flatMap(i => Stream.emits(List(i, i))).toList
// List(1, 1, 2, 2, 3, 3, 42, 42)
```

Независимо от того, как построен `Stream`, каждая операция выполняется за **константное время**. 
Так, `s ++ s2` выполняется за константное время, независимо от того, является ли `s` потоком `Stream.emit(1)` 
или огромным потоком с миллионами элементов и множеством встроенных эффектов. 
То же самое касается `s.flatMap(f)` и `handleErrorWith`, с которыми мы познакомимся чуть позже. 
Время выполнения этих операций не зависит от структуры `s`.

## Обработка ошибок

Поток может генерировать ошибки — либо явно, с помощью `Stream.raiseError`, 
либо неявно через исключение в чистом коде или внутри эффекта, переданного в `eval`:

```dotty
val err  = Stream.raiseError[IO](new Exception("ой-ой!"))
val err2 = Stream(1, 2, 3) ++ (throw new Exception("!@#$"))
val err3 = Stream.eval(IO(throw new Exception("ошибка в эффекте!!!")))
```

Все эти потоки завершатся ошибкой при выполнении:

```dotty
try err.compile.toList.unsafeRunSync() catch { case e: Exception => println(e) }
// java.lang.Exception: ой-ой!
// val res0: List[Nothing] | Unit = ()

try err2.toList catch { case e: Exception => println(e) }
// java.lang.Exception: !@#$
// val res1: List[Int] | Unit = ()

try err3.compile.drain.unsafeRunSync() catch { case e: Exception => println(e) }
// java.lang.Exception: ошибка в эффекте!!!
```

Метод `handleErrorWith` позволяет перехватывать любые из этих ошибок:

```dotty
err.handleErrorWith { e => Stream.emit(e.getMessage) }.compile.toList.unsafeRunSync()
// List(ой-ой!)
```

Обратите внимание, что даже при использовании `handleErrorWith` (или `attempt`) поток будет завершён после ошибки, 
и новые значения из него больше не будут извлекаться. В следующем примере число 4 никогда не будет прочитано из потока.

```dotty
val err4 = Stream(1, 2, 3).covary[IO] ++
  Stream.raiseError[IO](new Exception("плохие дела!")) ++
  Stream.eval(IO(4))

err4.handleErrorWith { _ => Stream(0) }.compile.toList.unsafeRunSync()
// List(1, 2, 3, 0)
```

> Не используйте `handleErrorWith` для освобождения ресурсов; 
> для этого предназначен `bracket`, о котором пойдёт речь в следующем разделе. 

## Приобретение ресурсов (Resource acquisition)

Если вам нужно захватить ресурс и вы хотите гарантировать, что при его захвате будет выполнено действие по очистке, 
используйте функцию `bracket`:

```dotty
val count   = new java.util.concurrent.atomic.AtomicLong(0)
val acquire = IO { println("увеличено: " + count.incrementAndGet()); () }
val release = IO { println("уменьшено: " + count.decrementAndGet()); () }

Stream.bracket(acquire)(_ => release).flatMap { _ =>
  Stream(1, 2, 3) ++ err
}.compile.drain.unsafeRunSync()
// увеличено: 1
// уменьшено: 0
// java.lang.Exception: ой-ой!
```

Внутренний поток завершается ошибкой, но обратите внимание, что действие `release` всё равно выполняется:

```dotty
count.get()
// 0
```

Независимо от того, как вы преобразуете поток FS2 и где возникают ошибки, библиотека гарантирует, 
что если ресурс был захвачен через `bracket`, связанное с ним действие освобождения будет выполнено. 
Вот сигнатура `bracket`:

```dotty
def bracket[F[_], R](acquire: F[R])(release: R => F[Unit]): Stream[F, R]
```

FS2 гарантирует семантику **один и только один раз** для действий очистки ресурсов, введённых функцией `Stream.bracket`.

## Упражнения по формированию потока

Попробуйте реализовать `repeat`, повторяющий поток бесконечно, 
`drain`, удаляющий все выходные данные из потока, 
`exec`, выполняющий эффект и игнорирующий его результат, 
и `attempt`, перехватывающий любые ошибки, возникшие в потоке:

```dotty
Stream(1, 0).repeat.take(6).toList
// List(1, 0, 1, 0, 1, 0)
Stream(1, 2, 3).drain.toList
// List()
Stream.exec(IO.println("!!")).compile.toVector.unsafeRunSync()
// !!
// Vector()
(Stream(1, 2) ++ Stream(3).map(_ => throw new Exception("неееет!!!"))).attempt.toList
// List(Right(1), Right(2), Left(java.lang.Exception: неееет!!!))
```

## Состояние-зависимое преобразование потоков (Statefully transforming streams)

Часто возникает необходимость преобразовывать один или несколько потоков с сохранением состояния, 
возможно, с вычислением эффектов по ходу дела. 
В качестве примера рассмотрим задачу взятия первых 5 элементов из потока `s: Stream[IO, Int]`. 
Чтобы создать `Stream[IO, Int]`, который берёт только первые 5 элементов из `s`, 
нам нужно многократно ожидать (или извлекать) значения из `s`, 
отслеживая количество уже просмотренных элементов и останавливаясь, как только будет достигнуто 5 элементов. 
В более сложных сценариях мы можем захотеть вычислять дополнительные эффекты по мере извлечения из одного или нескольких потоков.

Рассмотрим реализацию `take` с помощью комбинатора `scanChunksOpt`:

```dotty
import fs2.*

def tk[F[_], O](n: Long): Pipe[F, O, O] =
  in =>
    in.scanChunksOpt(n) { n =>
      if n <= 0 then None
      else
        Some((c: Chunk[O]) =>
          c.size match
            case m if m < n => (n - m, c)
            case _          => (0, c.take(n.toInt))
        )
    }

Stream(1, 2, 3, 4).through(tk(2)).toList
// List(1, 2)
```

Разберём этот код построчно.

```dotty
in => in.scanChunksOpt(n) { n =>
```

Здесь мы создаём анонимную функцию из `Stream[F, O]` в `Stream[F, O]` и вызываем `scanChunksOpt`, 
передавая начальное состояние `n` и функцию, которую определяем на следующих строках. 
Функция принимает текущее состояние в качестве аргумента, который мы намеренно назвали `n`, 
затеняя `n` из сигнатуры `tk`, чтобы случайно не ссылаться на него.

```dotty
if n <= 0 then None
```

Если текущее состояние равно 0 (или меньше), мы закончили, поэтому возвращаем `None`. 
Это указывает `scanChunksOpt`, что поток должен завершиться.

```dotty
else Some((c: Chunk[O]) =>
  c.size match
    case m if m < n => (n - m, c)
    case _          => (0, c.take(n.toInt))
)
```

В противном случае мы возвращаем функцию, которая обрабатывает следующий чанк в потоке. 
Функция сначала проверяет размер чанка. 
Если он меньше числа элементов, которые нужно взять, она возвращает чанк без изменений (он будет передан дальше по потоку), 
а также количество оставшихся элементов для взятия из последующих чанков (`n - m`). 
Если же размер чанка больше числа оставшихся элементов, из чанка берутся первые `n` элементов и выводятся, 
а также указывается, что больше элементов брать не нужно.

### Преобразование потоков с помощью Pull

Иногда `scanChunksOpt` недостаточно мощен, чтобы выразить нужное преобразование. 
Как бы сложна ни была задача, тип `fs2.Pull` обычно позволяет её выразить.

Тип `Pull[F[_], O, R]` представляет программу, которая может извлекать значения из одного или нескольких потоков, 
записывать **выходные** данные типа `O` и возвращать **результат** типа `R`. 
Он образует [монаду](../../typeclass/monad/monad.md) по `R` и снабжён множеством других полезных операций. 
Полный набор операций `Pull` см. в [классе `Pull`](https://github.com/functional-streams-for-scala/fs2/blob/main/core/shared/src/main/scala/fs2/Pull.scala).

`Pull`, который записывает одно выходное значение типа `Int`, можно построить с помощью `Pull.output1`.

```dotty
import fs2.*

val p1 = Pull.output1(1)
// Output(Chunk(1))
```

Его можно напрямую преобразовать в поток, эквивалентный `Stream(1)`.

```dotty
val s1 = p1.stream
// val s1: Stream[Nothing, Int] = Stream(..)
```

`Pull` образуют монаду по своему результату `R` и могут быть скомбинированы с помощью монадных операций. 
Следующий код создаёт `Pull`, соответствующий `Stream(1, 2)`.

```dotty
p1 >> Pull.output1(2)
// val res0: Pull[[x] =>> Nothing, Int, Unit] = <function1>
```

`Pull` может быть создан из потока с помощью различных операций, доступных через функцию `pull`. 
Например, `echo` преобразует поток в соответствующее ему представление `Pull`.

```dotty
s1.pull.echo
// InScope(Output(Chunk(1)),false)
```

Более полезный `Pull` создаётся с помощью `uncons`. Он создаёт `Pull`, который извлекает следующий чанк из потока.

```dotty
s1.pull.uncons
// val res2: Pull[Nothing, Nothing, Option[(Chunk[Int], Stream[Nothing, Int])]] = <function1>
```

Давайте рассмотрим его тип результата:

- `Option` не пуст, если в потоке есть чанк для извлечения. Если поток завершился и чанков больше нет, результат — `None`.
- `Chunk[Int]` представляет извлечённый чанк.
- `Stream[Nothing, Int]` представляет хвост потока.

Обратите внимание, что тип выходных данных — `Nothing`, потому что данный `Pull` не выводит никаких элементов.

Рассмотрим реализацию `take` с использованием `uncons`:

```dotty
import fs2.*

def tk[F[_], O](n: Long): Pipe[F, O, O] =
  def go(s: Stream[F, O], n: Long): Pull[F, O, Unit] =
    s.pull.uncons.flatMap {
      case Some((hd, tl)) =>
        hd.size match
          case m if m <= n => Pull.output(hd) >> go(tl, n - m)
          case _           => Pull.output(hd.take(n.toInt))
      case None => Pull.done
    }
  in => go(in, n).stream
// def tk[F[_$1], O](n: Long): Pipe[F, O, O]  

Stream(1, 2, 3, 4).through(tk(2)).toList
// List(1, 2)
```

Разберём построчно:

```dotty
def go(s: Stream[F, O], n: Long): Pull[F, O, Unit] = 
```

Мы реализуем это с помощью рекурсивной функции, возвращающей `Pull`. 
При каждом вызове мы передаём `Stream[F, O]` и количество оставшихся элементов `n`.

```dotty
s.pull.uncons.flatMap {
```

Вызов `s.pull` даёт нам множество методов, преобразующих поток в `Pull`. 
Мы используем `uncons`, чтобы извлечь следующий чанк из потока, 
что даёт нам `Pull[F, Nothing, Option[(Chunk[O], Stream[F, O])]]`. 
Затем мы применяем `flatMap` к этому `Pull`, чтобы получить доступ к `Option`.

```dotty
case Some((hd, tl)) =>
  hd.size match
    case m if m <= n => Pull.output(hd) >> go(tl, n - m)
    case _           => Pull.output(hd.take(n.toInt))
```

Если мы получаем `Some`, мы деструктурируем кортеж как `hd: Chunk[O]` и `tl: Stream[F, O]`. 
Затем проверяем размер головного чанка. 
Если размер чанка меньше или равен оставшемуся числу элементов, чанк выводится через `Pull.output`, 
и мы рекурсивно вызываем `go` для хвоста потока `tl`, передавая оставшееся число элементов. 
В противном случае мы выводим первые `n` элементов головы.

```dotty
case None => Pull.done
```

`None` получается, когда в потоке `in` больше нет элементов. 
Мы завершаем извлечение с помощью `Pull.done` — `Pull`, который ничего не делает.

```dotty
in => go(in, n).stream
```

Наконец, мы создаём анонимную функцию из `Stream[F, O]` в `Stream[F, O]` и вызываем `go` с начальным значением `n`. 
Мы получаем `Pull[F, O, Unit]`, который преобразуем обратно в `Stream[F, O]` с помощью метода `.stream`.

```dotty
val s2 = Stream(1, 2, 3, 4).through(tk(2))
s2.toList
// List(1, 2)
```

FS2 гарантирует, что все ресурсы, захваченные `Pull`, будут освобождены при завершении потока. 
Обратите внимание, что **ничего не происходит**, когда мы вызываем `.stream` у `Pull`, — это просто преобразование обратно в API `Stream`.

На практике явная рекурсия требуется редко — методы внутри `pull` обычно рекурсируют за нас. 
Явную рекурсию в `tk` можно было бы убрать, используя `pull.take`.

```dotty
import fs2.*

def tk[F[_], O](n: Long): Pipe[F, O, O] =
  in => in.pull.take(n).void.stream

Stream(1, 2, 3, 4).through(tk(2)).toList
// List(1, 2)
```

В [`Stream`](https://github.com/functional-streams-for-scala/fs2/blob/main/core/shared/src/main/scala/fs2/Stream.scala) 
существует множество полезных функций преобразования, построенных на основе типа `Pull`.

## Упражнения: преобразование потоков

Попробуйте реализовать `takeWhile`, `intersperse` и `scan`:

```dotty
Stream.range(0, 100).takeWhile(_ < 7).toList
// List(0, 1, 2, 3, 4, 5, 6)
Stream("Alice", "Bob", "Carol").intersperse("|").toList
// List(Alice, |, Bob, |, Carol)
Stream.range(1, 10).scan(0)(_ + _).toList // накопленная сумма
// List(0, 1, 3, 6, 10, 15, 21, 28, 36, 45)
```

## Параллелизм (Concurrency)

FS2 поставляется с большим количеством операций для параллельной работы. 
Функция `merge` запускает два потока параллельно, объединяя их выходные данные. 
Она останавливается, когда оба входных потока завершились:

```dotty
import cats.effect.IO
import cats.effect.unsafe.implicits.global

Stream(1, 2, 3).merge(Stream.eval(IO {
  Thread.sleep(200); 4
})).compile.toVector.unsafeRunSync()
// Vector(1, 2, 3, 4)
```

Функция `merge` поддерживает параллелизм. В FS2 есть множество других полезных функций параллелизма, 
таких как `concurrently` (запускает другой поток параллельно и игнорирует его выход), 
`interruptWhen` (останавливается, если левая ветвь выдаёт `true`), `either` (как `merge`, но возвращает `Either`) и другие.

В зависимости от того, как вы хотите остановить результирующий поток, 
вы можете использовать один из трёх вариантов метода `merge`: `mergeHaltR`, `mergeHaltL` и `mergeHaltBoth`. 
Результирующий поток завершится, когда завершится правый поток, левый поток или любой из потоков соответственно:

```dotty
val finite   = Stream('a', 'b', 'c', 'd', 'e').covary[IO]
val infinite = Stream.iterate(0)(_ + 1).covary[IO]

// Левый --------- Правый
finite.mergeHaltL(infinite) // Завершится
infinite.mergeHaltL(finite) // Не завершится

finite.mergeHaltR(infinite) // Не завершится
infinite.mergeHaltR(finite) // Завершится

finite.mergeHaltBoth(infinite) // Завершится
infinite.mergeHaltBoth(finite) // Тоже завершится
```

Поскольку довольно часто требуется завершить один поток, как только завершится другой (как в среде производитель-потребитель), 
существуют оптимизации по сравнению с вариантами `merge`. 
`concurrently` — одна из них: она завершает результирующий поток, когда завершается левый поток. 
Правый поток также завершится в этот момент, отбрасывая свои значения (похоже на `finite.mergeHaltL(infinite.drain)`):

```dotty
import cats.effect.std.{Queue, Random}
import scala.concurrent.duration.*

def producer(queue: Queue[IO, Option[Int]])(using
    rnd: Random[IO]
): Stream[IO, Option[Int]] =
  Stream
    .repeatEval(Random[IO].betweenInt(100, 800))
    .evalTap(n => IO.println(s"Произведено: $n"))
    .flatMap(t =>
      Stream.sleep[IO](t.milliseconds) >>
        Stream.emit(if t >= 750 then None else Some(t))
    )
    .evalTap(queue.offer)

def consumer(queue: Queue[IO, Option[Int]]): Stream[IO, Unit] =
  Stream.fromQueueNoneTerminated(queue, 10).evalMap(n =>
    IO.println(s"Потреблено: $n")
  )

val concurrentlyDemo =
  Stream.eval(Queue.bounded[IO, Option[Int]](20)).flatMap { queue =>
    Stream.eval(Random.scalaUtilRandom[IO]).flatMap { implicit rnd =>
      consumer(queue).concurrently(producer(queue))
    }
  }

concurrentlyDemo.compile.drain.unsafeRunSync()
// Произведено: 398
// Произведено: 752
// Потреблено: 398
// Произведено: 528
```

В примере выше поток `consumer` завершится, если элемент, произведённый `producer`, обрабатывается более 750 миллисекунд.

Функция `parEvalMap` позволяет вычислять эффекты параллельно и выдавать результаты по порядку на количестве волокон (fibers) 
до `maxConcurrent` одновременно, аналогично методу `parTraverseN`, который обычно используется в коллекциях стандартной библиотеки:

```dotty
// Это вычислит 5 эффектов параллельно
Stream(1, 2, 3, 4, 5).parEvalMap(5)(n => IO.pure(n * 2)).compile.toVector.unsafeRunSync()
// Vector(2, 4, 6, 8, 10)
```

Однако её использование с чистыми операциями встречается редко; 
чаще она применяется с функциями или комбинаторами, которые могут иметь побочные эффекты:

```dotty
import fs2.io.file.{Path, Files}
import fs2.io.readInputStream

import java.net.{URI, URL}
import java.io.InputStream

def getConnectionStream(url: URL): IO[InputStream] =
  IO(url.openConnection().getInputStream)

// Приключения Тома Сойера, Марк Твен
val bookParts = Stream(
  "7193/pg7193.txt", // Часть 1
  "7194/pg7194.txt", // Часть 2
  "7195/pg7195.txt", // Часть 3
  "7196/pg7196.txt"  // Часть 4
).map(part => new URI(s"https://www.gutenberg.org/cache/epub/$part").toURL)

bookParts
  .covary[IO]
  .parEvalMap(4)(url =>
    IO.println(s"Получение соединения с $url") >> getConnectionStream(url)
  )
  .flatMap(inps => readInputStream[IO](IO(inps), 4096))
  .through(Files[IO].writeAll(Path("testdata/tom_sawyer.txt")))
  .compile
  .drain
  .unsafeRunSync()
```

Хотя в большинстве случаев порядок потока не важен. Это может быть связано с разными причинами, 
например, если результирующие значения не имеют значения, 
если функция, которую вы передаёте, может занимать существенно разное время в зависимости от входных данных и т.д. 
Для таких случаев существует метод `parEvalMapUnordered`. 
Например, если вы просто хотите логировать эффекты, как только они завершатся:

```dotty
def slowFibo(n: Int): Int = // Просто имитация дорогостоящих вычислений
  if n <= 0 then n
  else if n == 1 then 1
  else slowFibo(n - 1) + slowFibo(n - 2)

Stream.eval(Random.scalaUtilRandom[IO]).flatMap { rnd =>
  Stream.repeatEval[IO, Int](rnd.nextIntBounded(40))
    .parEvalMapUnordered(2)(n =>
      IO.println(s"Выдано значение для $n с результатом: ${slowFibo(n)}")
    )
}
  .interruptAfter(3.seconds)
  .compile
  .drain
  .unsafeRunSync()
```

Обратите внимание, что если вам нужна неограниченная параллельность, 
существуют также версии `parEvalMapUnbounded` и `parEvalMapUnorderedUnbounded`, которые не принимают аргумент `maxConcurrent`.

Функция `parJoin` запускает несколько потоков параллельно; 
она очень полезна, когда нужно запустить несколько потоков как независимые процессы, 
а не делать их зависящими друг от друга. Сигнатура:

```dotty
// обратите внимание на ограничение Concurrent[F]
import cats.effect.Concurrent
def parJoin[F[_]: Concurrent, O](maxOpen: Int)(outer: Stream[F, Stream[F, O]]): Stream[F, O]
```

Она разворачивает вложенный поток, позволяя одновременно выполняться до `maxOpen` внутренним потокам. 
Как и `parEvalMapUnbounded`, существует метод `parJoinUnbounded` для неограниченной параллельности.

Пример запуска трёх различных процессов одновременно с использованием `parJoin`:

```dotty
val processA =
  Stream.awakeEvery[IO](1.second).map(_ => 1).evalTap(x =>
    IO.println(s"Процесс A: $x")
  )

val processB =
  Stream.iterate[IO, Int](0)(_ + 1).metered(400.milliseconds).evalTap(x =>
    IO.println(s"Процесс B: $x")
  )

val processC = Stream(1, 2, 3, 4, 5)
  .flatMap(t => Stream.sleep[IO]((t * 200).milliseconds) >> Stream.emit(t))
  .repeat
  .evalTap(x => IO.println(s"Процесс C: $x"))

Stream(processA, processB, processC).parJoin(3)
  .interruptAfter(5.seconds)
  .compile
  .drain
  .unsafeRunSync()
```

Ограничение `Concurrent` на `F` требуется везде, где в библиотеке используется параллелизм. 
Как упоминалось ранее, пользователи могут привносить свои собственные типы эффектов при условии, 
что они также предоставляют экземпляр `Concurrent` в неявной области видимости.

Кроме того, существует ряд других примитивов параллелизма — асинхронные очереди, сигналы и семафоры. 
См. раздел [Примитивы параллелизма](concurrency-primitives.md) для получения дополнительных примеров. 

## Упражнения: параллелизм

Не глядя на реализацию, попробуйте реализовать `mergeHaltBoth`:

```dotty
type Pipe2[F[_], -I, -I2, +O] = (Stream[F, I], Stream[F, I2]) => Stream[F, O]

/** Как `merge`, но останавливается, как только _любая_ из ветвей останавливается. */
def mergeHaltBoth[F[_]: Concurrent, O]: Pipe2[F, O, O, O] = (s1, s2) => ???
```

## Прерывание (Interruption)

Иногда некоторые задачи должны выполняться только при выполнении определённых условий или до тех пор, пока не завершится другая задача. 
К счастью, `Stream` определяет несколько очень полезных методов, которые позволяют нам это сделать. 
В следующем примере мы увидим, как `interruptWhen` помогает нам описать такие случаи. 
Мы опишем программу, состоящую из двух параллельных потоков: 
первый будет выводить текущее время на консоль каждую секунду, а второй — останавливать первый.

Сначала нам нужно настроить окружение с несколькими импортами и объявить некоторые неявные значения.

```dotty
import fs2.Stream
import cats.effect.{Deferred, IO}
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration.*
```

Пример выглядит так:

```dotty
val program =
  Stream.eval(Deferred[IO, Unit]).flatMap { switch =>
    val switcher =
      Stream.eval(switch.complete(())).delayBy(5.seconds)

    val program =
      Stream.repeatEval(IO(println(java.time.LocalTime.now))).metered(1.second)

    program
      .interruptWhen(switch.get.attempt)
      .concurrently(switcher)
  }

program.compile.drain.unsafeRunSync()
```

Теперь разберём этот код построчно, чтобы понять, что происходит.

```dotty
val program =
  Stream.eval(Deferred[IO, Unit]).flatMap { switch =>
```

Здесь мы создаём `Stream[IO, Deferred[IO, Unit]]`. 
[`Deferred`](../../cats-effect/typeclasses/concurrent/deferred.md) — это примитив параллелизма, 
представляющий условие, которое ещё не выполнено. 
Мы будем использовать полученный экземпляр `Deferred[IO, Unit]` как механизм для сигнализации о завершении задачи. 
Исходя из этой цели, мы называем этот экземпляр `switch` (переключатель).

```dotty
val switcher =
  Stream.eval(switch.complete(())).delayBy(5.seconds)
```

`switcher` — это поток, который через 5 секунд «переключит» `switch`, вызвав на нём `complete`. 
`delayBy` добавляет задержку перед выполнением потока, эффективно откладывая его вычисление на указанное время.

```dotty
val program =
  Stream.repeatEval(IO(println(java.time.LocalTime.now))).metered(1.second)
```

Это программа, которую мы хотим прервать. `repeatEval` — это эффектная версия `repeat`. 
`metered`, в свою очередь, заставляет наш поток выдавать значения с заданной частотой (в данном случае — одно значение в секунду).

```dotty
program
  .interruptWhen(switch.get.attempt)
  .concurrently(switcher)
```

В этой строке мы вызываем `interruptWhen` для потока, получая поток, который прекратит вычисления, 
как только «переключатель будет переключён»; затем, благодаря `concurrently`, мы указываем, что хотим, 
чтобы `switcher` выполнялся в **фоновом режиме**, игнорируя его выход. 
Это возвращает нам программу, описанную в начале главы.

Это способ создать программу, которая выполняется в течение заданного времени, в данном примере — 5 секунд. 
Прерывание по таймеру — настолько частый случай, что FS2 определяет метод `interruptAfter`. 
Вооружившись этими знаниями, мы можем переписать наш пример так:

```dotty
val program1 =
  Stream
    .repeatEval(IO(println(java.time.LocalTime.now)))
    .metered(1.second)
    .interruptAfter(5.seconds)

program1.compile.drain.unsafeRunSync()
```

## Взаимодействие с внешним миром

При взаимодействии с внешним миром вы можете столкнуться с несколькими различными ситуациями:

- Функции, которые выполняют побочные эффекты **синхронно**. Это самый простой случай.
- Функции, которые выполняют эффекты **асинхронно** и вызывают обратный вызов (callback) **один раз** по завершении. 
  Пример: чтение 4 МБ из файла на диске может быть функцией, которая принимает обратный вызов для вызова, когда байты становятся доступны.
- Функции, которые выполняют эффекты асинхронно и вызывают обратный вызов **один или несколько раз** по мере поступления результатов. 
  Пример: API базы данных, который асинхронно передаёт результаты запроса по мере их поступления.

Рассмотрим каждый из этих случаев по порядку.

### Синхронные эффекты

С ними проще всего работать. Просто оберните такие эффекты в `Stream.eval`:

```dotty
def destroyUniverse(): Unit = println("БУУУУМ!!!"); // заглушка

val s = Stream.exec(IO { destroyUniverse() }) ++ Stream("...двигаемся дальше")
s.compile.toVector.unsafeRunSync()
// БУУУУМ!!!
// val res0: Vector[String] = Vector(...двигаемся дальше)
```

Способ, которым вы вводите синхронные эффекты в ваш тип эффекта, может отличаться. 
`Sync.delay` можно использовать для этого в общем случае, не привязываясь к конкретному эффекту:

```dotty
import cats.effect.Sync

val T  = Sync[IO]
val s2 = Stream.exec(T.delay { destroyUniverse() }) ++
  Stream("...двигаемся дальше")
s2.compile.toVector.unsafeRunSync()
```

При использовании этого подхода убедитесь, что выражение, переданное в `delay`, не выбрасывает исключений.

### Асинхронные эффекты (однократный вызов колбэка)

Очень часто вы будете иметь дело с API, подобным этому:

```dotty
trait Connection:
  def readBytes(
      onSuccess: Array[Byte] => Unit,
      onFailure: Throwable => Unit
  ): Unit

  // или, возможно:
  def readBytesE(onComplete: Either[Throwable, Array[Byte]] => Unit): Unit =
    readBytes(bs => onComplete(Right(bs)), e => onComplete(Left(e)))

  override def toString = "<connection>"
```

То есть мы предоставляем `Connection` с двумя обратными вызовами (или одним, принимающим `Either`), 
и в какой-то момент позже этот обратный вызов будет вызван **один раз**. 
Трейт `cats.effect.Async` предоставляет удобную функцию в таких ситуациях:

```dotty
trait Async[F[_]] extends Sync[F] with Temporal[F]:
  ...

  /** Создаёт `F[A]` из асинхронного вычисления, которое имеет форму функции, с
    * помощью которой мы можем зарегистрировать обратный вызов. Это можно
    * использовать для перехода от API на основе колбэков к простой монадической
    * версии.
    */
  def async_[A](register: (Either[Throwable, A] => Unit) => Unit): F[A]
```

Вот полный пример:

```dotty
val c = new Connection:
  def readBytes(
      onSuccess: Array[Byte] => Unit,
      onFailure: Throwable => Unit
  ): Unit =
    Thread.sleep(200)
    onSuccess(Array(0, 1, 2))

val bytes = IO.async_[Array[Byte]] { cb => c.readBytesE(cb) }

Stream.eval(bytes).map(_.toList).compile.toVector.unsafeRunSync()
```

Обязательно ознакомьтесь с пакетом [`fs2.io`](https://github.com/functional-streams-for-scala/fs2/tree/main/io/), 
в котором есть отличные привязки FS2 к библиотекам Java NIO, использующие именно этот подход.

### Асинхронные эффекты (многократный вызов колбэка)

Прелесть API с колбэками, которые вызываются один раз, в том, 
что регулировку пропускной способности (throttling / back-pressure) можно организовать внутри самой FS2. 
Если вам не нужно больше значений — просто не читайте их, и они не будут производиться! 
Но иногда вы имеете дело с API, который вызывает переданные вами колбэки **более одного раза**. 
Возможно, это какой-то потоковый API, и он вызывает ваш колбэк при появлении новых данных. 
В таких случаях вы можете использовать асинхронную очередь для организации взаимодействия между 
миром потоковой обработки FS2 и внешним API, 
а также использовать любой подходящий механизм для регулирования производителя, предоставляемый этим API.

> Некоторые из таких API не предоставляют никаких средств для регулирования производителя, 
> и в этом случае вам придётся либо смириться с потенциально неограниченным потреблением памяти 
> (если производитель и потребитель работают с сильно разными скоростями), 
> либо использовать блокирующие примитивы параллелизма, такие как `cats.effect.std.Queue.bounded` или примитивы из `java.util.concurrent`.

Рассмотрим полный пример:

```dotty
import fs2.*
import cats.effect.Async
import cats.effect.std.{Dispatcher, Queue}

type Row        = List[String]
type RowOrError = Either[Throwable, Row]

trait CSVHandle:
  def withRows(cb: RowOrError => Unit): Unit

def rows[F[_]: Async as F](h: CSVHandle): Stream[F, Row] =
  for
    dispatcher <- Stream.resource(Dispatcher.sequential[F])
    q          <- Stream.eval(Queue.unbounded[F, Option[RowOrError]])
    _          <- Stream.eval {
      F.delay {
        def enqueue(v: Option[RowOrError]): Unit =
          dispatcher.unsafeRunAndForget(q.offer(v))

        // Заполняем данные - withRows блокируется во время чтения файла,
        // асинхронно вызывая переданный колбэк для каждой строки
        h.withRows(e => enqueue(Some(e)))
        // После возврата из withRows сигнализируем, что поток завершён.
        enqueue(None)
      }
    }
    // Благодаря fromQueueNoneTerminated поток завершится при встрече None
    row <- Stream.fromQueueNoneTerminated(q).rethrow
  yield row
```

См. [`Queue`](../../cats-effect/std/queue.md) для получения информации о других полезных методах. 
Большинство конкурентных очередей в Cats Effect поддерживают отслеживание своего размера, 
что удобно для реализации ограничения производителя на основе размера.

## Reactive Streams

Инициатива [reactive streams](http://www.reactive-streams.org/) сложна, мутабельна и небезопасна — 
она нежелательна для использования поверх fs2. 
Но бывают случаи, когда нам нужно использовать fs2 вместе с другой потоковой библиотекой, 
и здесь reactive streams проявляет себя.

Любая система reactive streams может взаимодействовать с любой другой системой reactive streams, 
предоставляя `org.reactivestreams.Publisher` или `org.reactivestreams.Subscriber`.

Библиотека `reactive-streams` предоставляет экземпляры издателей и подписчиков, 
соответствующих спецификации reactive streams, для упрощения взаимодействия с другими потоковыми библиотеками.

#### Использование

Вам могут потребоваться следующие импорты:

```dotty
import fs2.*
import fs2.interop.reactivestreams.*
import cats.effect.{IO, Resource}
```

Чтобы преобразовать `Stream` в нисходящий (downstream) одноадресный `org.reactivestreams.Publisher`:

```dotty
val stream = Stream(1, 2, 3).covary[IO]
stream.toUnicastPublisher
```

Чтобы преобразовать восходящий (upstream) `org.reactivestreams.Publisher` в `Stream`:

```dotty
val publisher: Resource[IO, StreamUnicastPublisher[IO, Int]] =
  Stream(1, 2, 3).covary[IO].toUnicastPublisher
publisher.use { p =>
  p.toStreamBuffered[IO](10).compile.toList
}
```

Одноадресный издатель должен иметь только одного подписчика.

## Приложения

###Как работает прерывание потоков

В FS2 поток может завершиться одним из трёх способов:

1. **Нормальное исчерпание входных данных.** Например, поток `Stream(1, 2, 3)` завершается после того, 
   как выдан единственный чанк (содержащий значения `1, 2, 3`).
2. **Необработанное исключение.** Например, поток `Stream(1, 2, 3) ++ (throw Err)` завершается с ошибкой `Err` после выдачи единственного чанка.
3. **Прерывание потребителем потока.** Прерывание может быть **синхронным**, как в `(Stream(1) ++ (throw Err)) take 1`, 
   которое детерминированно остановит поток до выполнения `++`, или **асинхронным**, как в `s1 merge s2 take 3`.

Касательно пункта 3:

- Поток никогда не будет прерван во время **захвата** ресурса (через `bracket`) или во время **освобождения** ресурса. 
  Функция `bracket` гарантирует, что если FS2 начинает захват ресурса, соответствующее действие освобождения будет выполнено.
- В остальном потоки могут быть прерваны между любыми двумя «шагами» потока. Сами шаги атомарны с точки зрения FS2. 
  `Stream.eval(eff)` — это один шаг, `Stream.emit(1)` — один шаг, `Stream(1, 2, 3)` — один шаг (выдача чанка), 
  а все остальные операции (например, `handleErrorWith`, `++`, `flatMap`) — это несколько шагов и могут быть прерваны.
- **Всегда используйте `bracket` или функцию на его основе, например `onFinalize`, для предоставления логики очистки ресурсов 
  или любой другой логики, которую вы хотите выполнить независимо от того, как завершился поток. 
  Не используйте для этой цели `handleErrorWith` или `++`.**

Рассмотрим несколько примеров того, как это работает, начиная со случая синхронного прерывания:

```dotty
import fs2.*
import cats.effect.IO
import cats.effect.unsafe.implicits.global

case object Err extends Throwable

(Stream(1) ++ Stream(2).map(_ => throw Err)).take(1).toList
(Stream(1) ++ Stream.raiseError[IO](Err)).take(1).compile.toList.unsafeRunSync()
```

`take 1` использует `Pull`, но не исследует весь поток, и ни один из этих примеров никогда не выбросит ошибку. 
Это логично. Немного более тонкий момент: следующий код тоже **никогда не выбросит ошибку**:

```dotty
(Stream(1) ++ Stream.raiseError[IO](Err)).take(1).compile.toList.unsafeRunSync()
```

Причина проста: потребитель (`take(1)`) завершается, как только получает элемент. 
Как только он его получил, он заканчивает потребление потока и не запускает дальнейшие шаги, 
поэтому поток никогда не завершается нормально — он был прерван до того, как это могло произойти. 
В данном случае мы можем видеть, что после выданной `1` ничего нет, но FS2 не знает об этом, пока не выполнит следующий шаг потока.

Если вместо этого мы используем `onFinalize`, код гарантированно выполнится, независимо от того, прерывает ли `take`:

```dotty
Stream(1)
  .covary[IO]
  .onFinalize(IO { println("финализировано!") })
  .take(1).compile.toVector.unsafeRunSync()
```

Это охватывает синхронные прерывания. Теперь рассмотрим асинхронные прерывания. 
Посмотрим, каким может быть результат `merged` в следующем примере:

```dotty
val s1 = (Stream(1) ++ Stream(2)).covary[IO]
val s2 = (Stream.empty ++ Stream.raiseError[IO](Err)).handleErrorWith { e =>
  println(e); Stream.raiseError[IO](e)
}
val merged = s1 merge s2 take 1
```

Результат в высокой степени **недетерминирован**. Вот несколько возможных сценариев:

- `s1` может завершиться до того, как ошибка в `s2` будет обнаружена, тогда ничего не будет напечатано и ошибки не возникнет.
- `s2` может обнаружить ошибку до того, как `s1` выдаст что-либо. Когда ошибка будет проброшена дальше `s2`, 
  это приведёт к завершению `merge` и асинхронному прерыванию `s1`, и `take` завершится с той же ошибкой.
- `s2` может обнаружить ошибку до того, как `s1` выдаст что-либо, но в тот период, когда ошибка перехватывается `handleErrorWith`, 
  `s1` может выдать значение, и `take(1)` может завершиться, инициируя прерывание как `s1`, 
  так и `s2`, до того, как ошибка будет проброшена дальше, но **после** того, как исключение будет напечатано! 
  В этом случае поток всё равно завершится без ошибки.

Корректность вашей программы не должна зависеть от того, как чередуются разные потоки, и ещё раз: 
не используйте `handleErrorWith` или другие прерываемые функции для очистки ресурсов. 
Используйте для этой цели `bracket` или `onFinalize`.

---

**Ссылки:**

- [Fs2 home page][home]

[home]: https://fs2.io/#/
