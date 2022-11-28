# Проблема производитель-потребитель — параллелизм и волокна

Модель производитель-потребитель часто встречается в параллельных запусках. 
Здесь один или несколько производителей вставляют данные в общую структуру данных, такую как очередь, 
в то время как один или несколько потребителей извлекают данные из нее. 
Читатели и писатели работают одновременно. 
Если очередь пуста, то читатели будут блокироваться до тех пор, пока данные не будут доступны, 
если очередь заполнена, то писатели будут ждать освобождения некоторого «сегмента». 
Только один модуль записи может одновременно добавлять данные в очередь, чтобы предотвратить повреждение данных. 
Кроме того, только один считыватель может извлекать данные из очереди, 
поэтому никакие два считывателя не могут получить один и тот же элемент данных.

Существуют вариации этой проблемы в зависимости от того, имеется ли более одного потребителя/производителя, 
или структура данных, находящаяся между ними, ограничена по размеру или нет. 
Решения, обсуждаемые здесь, подходят для настроек с несколькими потребителями и несколькими читателями. 
Сначала мы будем предполагать неограниченную структуру данных, а позже предложим решение для ограниченной.

Но прежде чем мы приступим к решению этой проблемы, мы должны определить волокна (_fibers_), 
которые являются основным строительным блоком параллелизма на основе cats-effect.

## Введение в волокна

Волокно содержит `F` - действие для выполнения (обычно `IO` экземпляр). 
Волокна похожи на «легкие» потоки, то есть их можно использовать так же, как и потоки, для создания параллельного кода. 
Однако они не являются потоками. 
Порождение новых волокон не гарантирует, что действие, описанное в связанном с ним `F`, 
будет выполнено в случае нехватки потоков. 
Внутренне cats-effect использует пулы потоков для запуска волокон при работе на JVM. 
Таким образом, если в пуле нет доступного потока, выполнение волокна будет «ждать», 
пока какой-либо поток снова не освободится. 
С другой стороны, когда выполнение некоторого волокна заблокировано, 
например поскольку он должен ждать освобождения семафора, поток, выполняющий волокно, 
повторно используется с помощью cats-effect, поэтому он доступен для других волокон. 
Когда выполнение волокна может быть возобновлено, cats-effect будет искать свободный поток для продолжения выполнения. 
Термин «семантически заблокированный» иногда используется для обозначения того, 
что блокирование волокна не влечет за собой остановку какого-либо потока. 
Cats-effect также перерабатывает волокна из готовых и погашенных волокон. 
Но имейте в виду, что, напротив, если волокно действительно заблокировано каким-то внешним действием, 
например, ожиданием некоторого ввода из TCP-сокета, 
то у Cats-effect нет возможности восстановить этот поток, пока действие не завершится. 
Такие вызовы должны быть обернуты `IO.blocking`, чтобы сигнализировать о том, что обернутый код заблокирует поток. 
Cats-effect использует эту информацию как подсказку для оптимизации `IO` планирования.

Еще одно отличие потоков заключается в том, что волокна — очень дешевые объекты. 
Мы можем легко создать миллионы из них, не влияя на производительность.

Стоит отметить, что вам не нужно явно отключать волокна. 
Если вы создадите волокно и оно закончит активную работу `IO`, то оно будет очищено сборщиком мусора, 
если только на него не будет какой-либо другой активной ссылки в памяти.
Таким образом, вы можете рассматривать волокно как любой другой обычный объект, 
за исключением того, что, когда волокно работает, сама среда выполнения с cats-effect поддерживает волокно в живых.

Это также имеет некоторые интересные последствия. 
Например, если вы создаете узел `IO.async` и регистрируете обратный вызов с чем-то, 
и находитесь в волокне, у которого нет сильных ссылок на объекты где-либо еще 
(т.е. вы сделали что-то вроде действия «запустил и забыл»), 
тогда сам обратный вызов является единственной сильной ссылкой на волокно. 
Это означает, что если регистрация завершится ошибкой или система, в которой вы зарегистрировались, выбросит ее, 
волокно просто изящно исчезнет.

Cats-effect реализует некоторые примитивы параллелизма для координации параллельных волокон: 
[Deferred](https://typelevel.org/cats-effect/docs/std/deferred), [Ref](https://typelevel.org/cats-effect/docs/std/ref), 
[Semaphore](https://typelevel.org/cats-effect/docs/std/semaphore) ...

Более подробную информацию о параллелизме в cats-effect можно найти 
в этом учебнике [«Параллелизм в Scala с cats-effect»](https://github.com/slouc/concurrency-in-scala-with-ce).

Хорошо, теперь, после краткого рассмотрения волокон, можно начать работать над проблемой производителя-потребителя.

## Первая (и неэффективная) реализация

Нам нужна промежуточная структура, в которую производитель(-и) может вставлять данные, а потребитель(-и) извлекать. 
Предположим, что это простая очередь. Первоначально будет только один производитель и один потребитель. 
Производитель сгенерирует последовательность целых чисел (`1`, `2`, `3`...), 
а потребитель просто прочитает эту последовательность. 
Наша общая очередь будет экземпляром неизменного объекта `Queue[Int]`.

Доступ к очереди может быть одновременным (и будет!), поэтому нам нужен какой-то способ защитить очередь, 
чтобы только одно волокно одновременно обрабатывало ее. 
Лучший способ обеспечить упорядоченный доступ к некоторым общим данным — [`Ref`](https://typelevel.org/cats-effect/docs/std/ref). 
Экземпляр `Ref` упаковывает некоторые заданные данные и реализует методы для безопасного управления этими данными. 
Когда какое-то волокно запускает один из этих методов, 
любой другой вызов любого метода `Ref` экземпляра будет заблокирован.

Обертка `Ref` для нашей очереди будет `Ref[F, Queue[Int]]` (для некоторых `F[_]`).

Метод `producer` будет таким:

```scala
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import collection.immutable.Queue

def producer[F[_]: Sync: Console](queueR: Ref[F, Queue[Int]], counter: Int): F[Unit] =
  for
    _ <- if counter % 10000 == 0 then Console[F].println(s"Produced $counter items") else Sync[F].unit
    _ <- queueR.getAndUpdate(_.enqueue(counter + 1))
    _ <- producer(queueR, counter + 1)
  yield ()
```

Первая строка просто печатает какое-то сообщение журнала для каждого 10000-го элемента, поэтому мы знаем, «живой» ли он. 
Он использует `type class Console[_]`, который дает возможность печатать 
и читать строки (`IO.println` просто использует внутри `Console[IO].println`).

Затем код вызывает `queueR.getAndUpdate` - добавление данных в очередь. 
Обратите внимание, что `.getAndUpdate` предоставляется текущая очередь, 
затем используем ее `.enqueue` для вставки следующего значения `counter + 1`. 
Этот вызов возвращает новую очередь с добавленным значением, которое хранится в экземпляре `ref`. 
Если какое-то другое волокно получает доступ к `queueR`, то волокно (семантически) блокируется.

Метод `consumer` немного другой. 
Он попытается прочитать данные из очереди, но должен знать, что очередь может быть пустой:

```scala
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import collection.immutable.Queue

def consumer[F[_]: Sync: Console](queueR: Ref[F, Queue[Int]]): F[Unit] =
  for
    iO <- queueR.modify { queue =>
            queue.dequeueOption.fold((queue, Option.empty[Int])) { (i, queue) => (queue, Option(i)) }
          }
    _  <- if iO.exists(_ % 10000 == 0) then Console[F].println(s"Consumed ${iO.get} items") else Sync[F].unit
    _  <- consumer(queueR)
  yield ()
```

Вызов `queueR.modify` позволяет изменить упакованные данные (нашу очередь) и вернуть значение, вычисленное из этих данных. 
В нашем случае он возвращает `Option[Int]`, что выдало бы `None`, если бы очередь была пустой. 
Следующая строка используется для регистрации сообщения в консоли о каждом 10000-ом прочитанном элементе. 
Наконец `consumer` вызывается рекурсивно, чтобы начать снова.

Теперь мы можем создать программу, которая создает экземпляр `queueR` и запускает `producer` и `consumer` параллельно:

```scala
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*

import scala.collection.immutable.Queue

object InefficientProducerConsumer extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for
      queueR <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
      res <- (consumer(queueR), producer(queueR, 0))
        .parMapN((_, _) => ExitCode.Success) // Запуск producer и consumer в параллели до окончания выполнения (до отмены пользователем по CTRL-C)
        .handleErrorWith { t =>
          Console[IO].errorln(s"Error caught: ${t.getMessage}").as(ExitCode.Error)
        }
    yield res

  private def producer[F[_]: Sync](queueR: Ref[F, Queue[Int]], counter: Int): F[Unit] = ??? // определено выше
  private def consumer[F[_]: Sync](queueR: Ref[F, Queue[Int]]): F[Unit] = ???               // определено выше
```

Полная реализация этого примитивного производителя-потребителя доступна [здесь](https://github.com/lrodero/cats-effect-tutorial/blob/series/3.x/src/main/scala/catseffecttutorial/producerconsumer/InefficientProducerConsumer.scala).

Наша функция `run` создает экземпляр общей очереди, обернутой в `Ref`, 
и параллельно загружает производителя и потребителя. 
Для этого используется `parMapN`, который создает и запускает волокна, запускающие `IO`-ы, переданный в качестве параметра. 
Затем он берет выходные данные каждого волокна и применяет к ним заданную функцию. 
В нашем случае и производитель, и потребитель будут работать "вечно", 
пока пользователь не нажмет CTRL-C, что вызовет отмену.

В качестве альтернативы мы могли бы использовать метод `start` для явного создания новых экземпляров `Fiber`, 
которые будут запускать производителя и потребителя, а затем использовать `join` для ожидания их завершения, 
например:

```scala
import cats.effect.*
import collection.immutable.Queue

object InefficientProducerConsumer extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for
      queueR        <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
      producerFiber <- producer(queueR, 0).start
      consumerFiber <- consumer(queueR).start
      _             <- producerFiber.join
      _             <- consumerFiber.join
    yield ExitCode.Error

  private def producer[F[_]: Sync](queueR: Ref[F, Queue[Int]], counter: Int): F[Unit] = ??? // определено выше
  private def consumer[F[_]: Sync](queueR: Ref[F, Queue[Int]]): F[Unit] = ???               // определено выше
```

Однако в большинстве случаев не рекомендуется обрабатывать волокна вручную, так как с ними не так просто работать. 
Например, если в волокне есть ошибка, вызов `join` этого волокна не вызовет ее, он вернется в обычном режиме, 
и вы должны явно проверить экземпляр `Outcome`, возвращенный вызовом `.join`, чтобы увидеть, не возникла ли ошибка. 
Кроме того, другие волокна будут продолжать работать, не зная о том, что произошло.

Cats Effect предоставляет дополнительные методы `joinWith` или `joinWithNever`, чтобы убедиться, что ошибка вызвана, 
по крайней мере, обычной семантикой `MonadError` (например, "короткое замыкание"). 
Теперь, когда мы вызываем ошибку, нам также нужно отменить другие работающие волокна. 
Мы можем легко попасть в ловушку путаницы волокон, чтобы следить за ней. 
Кроме того, ошибка, вызванная волокном, не продвигается до тех пор, 
пока не будет достигнут вызов `joinWith` или `joinWithNever`.

Таким образом, в нашем примере выше, если в `consumerFiber` возникает ошибка, 
то у нас нет возможности наблюдать это, пока волокно `producer` не завершится. 
Обратите внимание, что в нашем примере `producer` никогда не завершает работу, 
и поэтому ошибка никогда не возникает! 
И даже если бы волокно `producer` закончилось, оно бы потребляло ресурсы впустую.

В отличие от этого, `parMapN` передает вызывающей стороне любую обнаруженную ошибку 
и заботится об отмене других запущенных волокон. 
В результате `parMapN` проще в использовании, более лаконичен и о нем легче рассуждать. 
_Из-за этого, если у вас нет особых и необычных требований, 
вы должны предпочесть использовать команды высшего уровня, 
такие как `parMapN` или `parSequence` для работы с волокнами._

Хорошо, мы придерживаемся нашей реализации, основанной на `.parMapN`. 
Все? Это работает? Что ж, это работает... но далеко от идеала. 
Если мы запустим его, то обнаружим, что `producer` работает быстрее, чем `consumer`, поэтому очередь постоянно растет. 
И даже если бы это было не так, мы должны понимать, что consumer будет работать постоянно, 
независимо от наличия элементов в очереди, что далеко не идеально. 
Мы постараемся улучшить его в следующем разделе, используя [Deferred](https://typelevel.org/cats-effect/docs/std/deferred). 
Также мы будем использовать несколько потребителей и производителей, 
чтобы сбалансировать скорость производства и потребления.

## Более надежная реализация проблемы производителя/потребителя

В нашем коде производителя/потребителя мы уже защищаем доступ к очереди (нашему общему ресурсу) с помощью `Ref`. 
Теперь, вместо того, чтобы использовать `Option` для представления элементов, извлеченных из возможно пустой очереди, 
мы должны вместо этого каким-то образом заблокировать волокно вызывающей стороны, если очередь пуста, 
до тех пор, пока какой-либо элемент не может быть возвращен. 
Это будет сделано путем создания и хранения экземпляров `Deferred`. 
Экземпляр `Deferred[F, A]` может содержать один единственный элемент некоторого типа `A`. 
`Deferred` экземпляры создаются пустыми и могут быть заполнены только один раз. 
Если какое-то волокно попытается прочитать элемент из пустого `Deferred`, 
то оно будет семантически заблокировано до тех пор, пока какое-то другое волокно не заполнит (завершит) его.

Таким образом, наряду с очередью произведенных, но еще не потребленных элементов, 
мы должны отслеживать экземпляры `Deferred`, созданные, когда очередь была пуста и ожидающие доступности элементов. 
Эти экземпляры будут храниться в новой очереди `takers`. 
Мы сохраним обе очереди в новом типе `State`:

```scala
import cats.effect.Deferred
import scala.collection.immutable.Queue
case class State[F[_], A](queue: Queue[A], takers: Queue[Deferred[F, A]])
```

И производитель, и потребитель будут иметь доступ к одному и тому же общему экземпляру состояния, 
который будет переноситься и безопасно модифицироваться экземпляром `Ref`. 

Потребитель работает следующим образом:

- Если `queue` не пустой, он извлечет и вернет свой заглавный элемент. 
  Новое состояние сохранит остаток очереди, изменения `takers` не потребуются.
- Если `queue` пусто, он будет использовать новый экземпляр `Deferred` в качестве нового `taker`, 
  добавит его в очередь `takers` и «заблокирует» вызывающую сторону, вызвав `taker.get`

Если предположить, что в нашей настройке мы производим и потребляем `Ints` (как и раньше), 
тогда новый потребительский код будет таким:

```scala
import cats.effect.{Deferred, Ref, Async}
import cats.effect.std.Console
import cats.syntax.all._
import scala.collection.immutable.Queue

case class State[F[_], A](queue: Queue[A], takers: Queue[Deferred[F, A]])

def consumer[F[_]: Async: Console](id: Int, stateR: Ref[F, State[F, Int]]): F[Unit] =
  val take: F[Int] =
    Deferred[F, Int].flatMap { taker =>
      stateR.modify {
        case State(queue, takers) if queue.nonEmpty =>
          val (i, rest) = queue.dequeue
          // Получен элемента из очереди, мы можем просто вернуть его
          State(rest, takers) -> Async[F].pure(i)
        case State(queue, takers)                   =>
          // Нет элемента в очереди, должны заблокировать вызов пока что-то не получим
          State(queue, takers.enqueue(taker)) -> taker.get
      }.flatten
    }

  for {
    i <- take
    _ <- if (i % 10000 == 0) Console[F].println(s"Consumer $id has reached $i items") else Async[F].unit
    _ <- consumer(id, stateR)
  } yield ()
end consumer
```

Параметр `id` используется только для идентификации потребителя в логах консоли 
(напомним, теперь у нас будет несколько производителей и потребителей, работающих параллельно). 
Экземпляр `take` реализует проверку и обновление состояния в `stateR`. 
Обратите внимание, как он будет блокироваться на `taker.get`, когда очередь пуста.

Производитель, со своей стороны, будет:

- Если есть ожидающие `takers`, он возьмет первый в очереди и предложит ему вновь созданный элемент (`taker.complete`).
- Если нет `takers`, он просто поставит в очередь созданный элемент.

Таким образом производитель будет выглядеть так:

```scala
import cats.effect.{Deferred, Ref, Sync}
import cats.effect.std.Console
import cats.syntax.all.*
import scala.collection.immutable.Queue

case class State[F[_], A](queue: Queue[A], takers: Queue[Deferred[F, A]])

def producer[F[_]: Sync: Console](id: Int, counterR: Ref[F, Int], stateR: Ref[F, State[F, Int]]): F[Unit] =
  def offer(i: Int): F[Unit] =
    stateR.modify {
      case State(queue, takers) if takers.nonEmpty =>
        val (taker, rest) = takers.dequeue
        State(queue, rest) -> taker.complete(i).void
      case State(queue, takers)                    =>
        State(queue.enqueue(i), takers) -> Sync[F].unit
    }.flatten

  for {
    i <- counterR.getAndUpdate(_ + 1)
    _ <- offer(i)
    _ <- if i % 10000 == 0 then Console[F].println(s"Producer $id has reached $i items") else Sync[F].unit
    _ <- producer(id, counterR, stateR)
  } yield ()
```

Наконец, мы модифицируем нашу основную программу, чтобы она создавала счетчик и состояние `Refs`. 
Также она создаст несколько потребителей и производителей, по 10 штук, и запустит их всех параллельно:

```scala
import cats.effect.*
import cats.effect.std.Console
import cats.instances.list.*
import cats.syntax.all.*
import scala.collection.immutable.Queue

object ProducerConsumer extends IOApp:

  case class State[F[_], A](queue: Queue[A], takers: Queue[Deferred[F, A]])

  object State {
    def empty[F[_], A]: State[F, A] = State(Queue.empty, Queue.empty)
  }

  override def run(args: List[String]): IO[ExitCode] =
    for {
      stateR   <- Ref.of[IO, State[IO, Int]](State.empty[IO, Int])
      counterR <- Ref.of[IO, Int](1)
      producers = List.range(1, 11).map(producer(_, counterR, stateR)) // 10 производителей
      consumers = List.range(1, 11).map(consumer(_, stateR))           // 10 потребителей
      res      <- (producers ++ consumers).parSequence
                    .as(
                      ExitCode.Success
                    ) // Запуск producer и consumer в параллели до окончания выполнения (до отмены пользователем по CTRL-C)
                    .handleErrorWith { t =>
                      Console[IO].errorln(s"Error caught: ${t.getMessage}").as(ExitCode.Error)
                    }
    } yield res

  private def producer[F[_]: Sync](id: Int, counterR: Ref[F, Int], stateR: Ref[F, State[F, Int]]): F[Unit] =
    ??? // определено выше

  private def consumer[F[_]: Async](id: Int, stateR: Ref[F, State[F, Int]]): F[Unit] = ??? // определено выше
```

Полная реализация этого производителя-потребителя с неограниченной очередью доступна [здесь](https://github.com/lrodero/cats-effect-tutorial/blob/series/3.x/src/main/scala/catseffecttutorial/producerconsumer/ProducerConsumer.scala).

Производители и потребители создаются в виде двух списков `IO` экземпляров. 
Все они запускаются в своем собственном волокне вызовом `parSequence`, который будет ждать завершения всех из них, 
а затем вернет значение, переданное в качестве параметра. 
Как и в предыдущем примере, эта программа будет работать вечно, пока пользователь не нажмет CTRL-C.

Наличие нескольких потребителей и производителей улучшает баланс между потребителями и производителями... 
но, тем не менее, в долгосрочной перспективе очередь имеет тенденцию увеличиваться в размерах. 
Чтобы исправить это, мы обеспечим ограничение размера очереди, поэтому всякий раз, 
когда этот максимальный размер будет достигнут, производители будут блокироваться, 
как это делают потребители, когда очередь пуста.

## Производитель-потребитель с ограниченной очередью

Наличие ограниченной очереди подразумевает, что производители, когда очередь заполнена, 
будут ждать (быть «семантически заблокированными»), пока не появится какая-то пустая корзина, доступная для заполнения. 
Таким образом, реализация должна отслеживать этих ожидающих производителей. 
Для этого мы добавим новую очередь `offerers`, которая будет добавлена в `State` рядом с `takers`. 
Для каждого ожидающего производителя в очереди `offerers` будет храниться `Deferred[F, Unit]`, 
который будет использоваться для блокировки производителя до тех пор, 
пока предлагаемый им элемент не будет добавлен в `queue` или напрямую передан какому-либо потребителю (`taker`). 
Наряду с экземпляром `Deferred` нам также необходимо сохранить в очереди `offerers` 
фактический элемент, предлагаемый производителем. 
Таким образом класс `State` теперь становится:

```scala
import cats.effect.Deferred
import scala.collection.immutable.Queue

case class State[F[_], A](
    queue: Queue[A],
    capacity: Int,
    takers: Queue[Deferred[F, A]],
    offerers: Queue[(A, Deferred[F, Unit])]
)
```

Конечно, и потребитель, и производитель должны быть изменены для обработки этой новой очереди `offerers`. 
У потребителя может быть четыре сценария, в зависимости от того, является ли каждый из `queue` и `offerers` пустым или нет. 
Для каждого сценария потребитель должен:

- Если `queue` не пусто:
  - Если `offerers` пустой, то он извлечет и вернет `queue` головной элемент.
  - Если `offerers` не пустой (есть какой-то производитель), тогда все сложнее. 
    Головной элемент `queue` будет возвращен потребителю. Теперь у нас есть свободная корзина в формате `queue`. 
    Таким образом, первый ожидающий может использовать эту корзину, чтобы добавить предлагаемый им элемент. 
    Этот элемент будет добавлен в `queue`, и экземпляр `Deferred` будет завершен, 
    поэтому производитель будет освобожден (разблокирован).
- Если `queue` пусто:
  - Если `offerers` пустой, то мы ничего не можем дать вызывающей стороне, поэтому создается `taker` 
    и добавляется в `takers`, пока вызывающая сторона заблокирована с помощью `taker.get`.
  - Если `offerers` не пустой, то извлекается первый в очереди `offerer`, его `Deferred` экземпляр освобождается, 
    в то время как предлагаемый элемент возвращается вызывающему.

Итак, потребительский код выглядит так:

```scala
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import scala.collection.immutable.Queue

case class State[F[_], A](
    queue: Queue[A],
    capacity: Int,
    takers: Queue[Deferred[F, A]],
    offerers: Queue[(A, Deferred[F, Unit])]
)

def consumer[F[_]: Async: Console](id: Int, stateR: Ref[F, State[F, Int]]): F[Unit] =
  val take: F[Int] =
    Deferred[F, Int].flatMap { taker =>
      stateR.modify {
        case State(queue, capacity, takers, offerers) if queue.nonEmpty && offerers.isEmpty =>
          val (i, rest) = queue.dequeue
          State(rest, capacity, takers, offerers) -> Async[F].pure(i)
        case State(queue, capacity, takers, offerers) if queue.nonEmpty                     =>
          val (i, rest)               = queue.dequeue
          val ((move, release), tail) = offerers.dequeue
          State(rest.enqueue(move), capacity, takers, tail) -> release.complete(()).as(i)
        case State(queue, capacity, takers, offerers) if offerers.nonEmpty                  =>
          val ((i, release), rest) = offerers.dequeue
          State(queue, capacity, takers, rest) -> release.complete(()).as(i)
        case State(queue, capacity, takers, offerers)                                       =>
          State(queue, capacity, takers.enqueue(taker), offerers) -> taker.get
      }.flatten
    }

  for {
    i <- take
    _ <- if i % 10000 == 0 then Console[F].println(s"Consumer $id has reached $i items") else Async[F].unit
    _ <- consumer(id, stateR)
  } yield ()
```

Функциональность продюсера немного проще:

- Если есть какой-то ожидающий `taker`, то произведенный элемент будет передан ему, освобождая заблокированное волокно.
- Если ожидающего `taker` нет, а `queue` не заполнен, то предложенный элемент будет поставлен в очередь.
- Если ожидающего `taker` нет, а `queue` уже заполнен, то создается новый `offerer`, 
  блокирующий волокно-производитель на методе `.get` экземпляра `Deferred`.

Теперь код производителя выглядит так:

```scala
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import scala.collection.immutable.Queue

case class State[F[_], A](
    queue: Queue[A],
    capacity: Int,
    takers: Queue[Deferred[F, A]],
    offerers: Queue[(A, Deferred[F, Unit])]
)

def producer[F[_]: Async: Console](id: Int, counterR: Ref[F, Int], stateR: Ref[F, State[F, Int]]): F[Unit] =

  def offer(i: Int): F[Unit] =
    Deferred[F, Unit].flatMap[Unit] { offerer =>
      stateR.modify {
        case State(queue, capacity, takers, offerers) if takers.nonEmpty       =>
          val (taker, rest) = takers.dequeue
          State(queue, capacity, rest, offerers) -> taker.complete(i).void
        case State(queue, capacity, takers, offerers) if queue.size < capacity =>
          State(queue.enqueue(i), capacity, takers, offerers) -> Async[F].unit
        case State(queue, capacity, takers, offerers)                          =>
          State(queue, capacity, takers, offerers.enqueue(i -> offerer)) -> offerer.get
      }.flatten
    }

  for {
    i <- counterR.getAndUpdate(_ + 1)
    _ <- offer(i)
    _ <- if i % 10000 == 0 then Console[F].println(s"Producer $id has reached $i items") else Async[F].unit
    _ <- producer(id, counterR, stateR)
  } yield ()
```

Как видите, производитель и потребитель закодированы вокруг идеи сохранения и изменения состояния, 
как и в случае с неограниченными очередями.

В качестве последнего шага мы должны адаптировать основную программу для использования этих новых потребителей и производителей. 
Допустим, мы ограничиваем размер очереди до `100`, тогда имеем:

```scala
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*

import scala.collection.immutable.Queue

object ProducerConsumerBounded extends IOApp:

  case class State[F[_], A](
      queue: Queue[A],
      capacity: Int,
      takers: Queue[Deferred[F, A]],
      offerers: Queue[(A, Deferred[F, Unit])]
  )

  object State:
    def empty[F[_], A](capacity: Int): State[F, A] = State(Queue.empty, capacity, Queue.empty, Queue.empty)

  override def run(args: List[String]): IO[ExitCode] =
    for {
      stateR   <- Ref.of[IO, State[IO, Int]](State.empty[IO, Int](capacity = 100))
      counterR <- Ref.of[IO, Int](1)
      producers = List.range(1, 11).map(producer(_, counterR, stateR)) // 10 производителей
      consumers = List.range(1, 11).map(consumer(_, stateR))           // 10 потребителей
      res      <- (producers ++ consumers).parSequence
                    .as(
                      ExitCode.Success
                    ) // Запуск producer и consumer в параллели до окончания выполнения (до отмены пользователем по CTRL-C)
                    .handleErrorWith { t =>
                      Console[IO].errorln(s"Error caught: ${t.getMessage}").as(ExitCode.Error)
                    }
    } yield res

  def producer[F[_]: Async](id: Int, counterR: Ref[F, Int], stateR: Ref[F, State[F, Int]]): F[Unit] =
    ??? // определено выше

  def consumer[F[_]: Async](id: Int, stateR: Ref[F, State[F, Int]]): F[Unit] = ??? // определено выше
```

Полная реализация этого производителя-потребителя с ограниченной очередью доступна [здесь](https://github.com/lrodero/cats-effect-tutorial/blob/series/3.x/src/main/scala/catseffecttutorial/producerconsumer/ProducerConsumerBounded.scala).

## Забота об отмене

Безопасна ли отмена этой реализации? 
То есть, что произойдет, если волокно, по которому работает потребитель или производитель, будет отменено? 
Становится ли состояние непоследовательным? 
Давайте сначала проверим `producer`. 
Его состояние обрабатывается внутренним `offer`, поэтому сосредоточимся на нем. 
И, для ясности в нашем анализе, давайте переформатируем код, используя для понимания:

```scala
import cats.effect.Deferred

def offer[F[_]](i: Int): F[Unit] =
  for {
    offerer <- Deferred[F, Int]
    op      <- stateR.modify { ??? } // `op` - это F[] для запуска
    _       <- op
  } yield ()
```

Теперь отмена вступает в действие в каждом `.flatMap` в `F`, т.е. в каждом шаге нашего _for-comprehension_. 
Если волокно отменяется прямо перед или после первого шага, это не проблема. 
`offerer` в конечном итоге будет удален сборщиком мусора, вот и все. 
Но что, если отмена произойдет сразу после вызова `modify`? 
Ну тогда `op` не будет запускаться.
Напомним, что по содержанию `modify`, `op` может быть `taker.complete(i).void`, `Sync[F].unit` или `offerer.get`. 
Отмена после удаления `taker` или добавления `offerer` в состояние, но без запуска `op`, оставит состояние несогласованным. 
Мы можем быстро исправить это, сделав этот код неотменяемым:

```scala
def offer[F[_]](i: Int): F[Unit] =
  for {
    offerer <- Deferred[F, Int]
    _       <- F.uncancelable { poll => // `poll` пока игнорируется, мы обсудим его позже
      for {
        op <- stateR.modify {???} // `op` - это F[] для запуска
        _  <- op // `taker.complete(i).void`, `Sync[F].unit` или `offerer.get`
      } yield ()
    }
  } yield ()
```

В чем проблема? 
Если `op` не блокируемый, то есть либо `F.unit` или `taker.complete(a).void`, то наше решение подойдет. 
Но когда выполняется операция `offerer.get`, у нас возникает проблема, поскольку `.get` будет блокироваться до тех пор, 
пока `offerer` не будет завершена (напомним, что это `Deferred` экземпляр). 
Таким образом, волокно не будет продвигаться, но в то же время мы установили эту операцию внутри неотменяемой области. 
Таким образом, нет никакого способа отменить это заблокированное волокно! 
Например, мы не можем установить таймаут на его выполнение! 
Таким образом, если `offerer` волокно никогда не будет завершено, то все это волокно никогда не будет завершено.

Это можно решить с помощью `Poll[F]`, который передается в качестве параметра `F.uncancelable`. 
`Poll[F]` используется для определения отменяемого кода внутри неотменяемой области. 
Поэтому, если операция, которую нужно запустить, была `offerer.get`, мы встроим этот вызов в `Poll[F]`, 
тем самым обеспечив возможность отмены заблокированного волокна. 
Наконец, мы также должны позаботиться об очистке состояния, если действительно есть отмена. 
Эта очистка должна будет удалить `offerer` из списка `offerers`, хранящихся в `State`, 
поскольку она никогда не будет завершена. 
Наша `offer` функция стала:

```scala
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import cats.effect.syntax.all.*
import scala.collection.immutable.Queue

case class State[F[_], A](
    queue: Queue[A],
    capacity: Int,
    takers: Queue[Deferred[F, A]],
    offerers: Queue[(A, Deferred[F, Unit])]
)

def producer[F[_]: Async: Console](id: Int, counterR: Ref[F, Int], stateR: Ref[F, State[F, Int]]): F[Unit] =

  def offer(i: Int): F[Unit] =
    Deferred[F, Unit].flatMap[Unit] { offerer =>
      Async[F].uncancelable {
        poll => // `poll`, используемый для встраивания отменяемого кода, то есть вызов `offerer.get`
          stateR.modify {
            case State(queue, capacity, takers, offerers) if takers.nonEmpty       =>
              val (taker, rest) = takers.dequeue
              State(queue, capacity, rest, offerers) -> taker.complete(i).void
            case State(queue, capacity, takers, offerers) if queue.size < capacity =>
              State(queue.enqueue(i), capacity, takers, offerers) -> Async[F].unit
            case State(queue, capacity, takers, offerers)                          =>
              val cleanup = stateR.update { s => s.copy(offerers = s.offerers.filter(_._2 ne offerer)) }
              State(queue, capacity, takers, offerers.enqueue(i -> offerer)) -> poll(offerer.get).onCancel(cleanup)
          }.flatten
      }
    }

  for {
    i <- counterR.getAndUpdate(_ + 1)
    _ <- offer(i)
    _ <- if i % 10000 == 0 then Console[F].println(s"Producer $id has reached $i items") else Async[F].unit
    _ <- producer(id, counterR, stateR)
  } yield ()
```

Потребительская часть должна работать с отменой таким же образом. 
Она будет использовать `poll` для включения отмены блокирующих вызовов, 
но в то же время обязательно очистит состояние при отмене. 
В этом случае вызов блокируется `taker.get`, 
при отмене такого вызова `taker` будет удален из списка принимающих в `State`. 
Итак, `consumer` сейчас:

```scala
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import cats.effect.syntax.all.*
import scala.collection.immutable.Queue

case class State[F[_], A](queue: Queue[A], capacity: Int, takers: Queue[Deferred[F,A]], offerers: Queue[(A, Deferred[F,Unit])])

def consumer[F[_]: Async: Console](id: Int, stateR: Ref[F, State[F, Int]]): F[Unit] =

  val take: F[Int] =
    Deferred[F, Int].flatMap { taker =>
      Async[F].uncancelable { poll =>
        stateR.modify {
          case State(queue, capacity, takers, offerers) if queue.nonEmpty && offerers.isEmpty =>
            val (i, rest) = queue.dequeue
            State(rest, capacity, takers, offerers) -> Async[F].pure(i)
          case State(queue, capacity, takers, offerers) if queue.nonEmpty =>
            val (i, rest) = queue.dequeue
            val ((move, release), tail) = offerers.dequeue
            State(rest.enqueue(move), capacity, takers, tail) -> release.complete(()).as(i)
          case State(queue, capacity, takers, offerers) if offerers.nonEmpty =>
            val ((i, release), rest) = offerers.dequeue
            State(queue, capacity, takers, rest) -> release.complete(()).as(i)
          case State(queue, capacity, takers, offerers) =>
            val cleanup = stateR.update { s => s.copy(takers = s.takers.filter(_ ne taker)) }
            State(queue, capacity, takers.enqueue(taker), offerers) -> poll(taker.get).onCancel(cleanup)
        }.flatten
      }
    }

  for {
    i <- take
    _ <- if i % 10000 == 0 then Console[F].println(s"Consumer $id has reached $i items") else Async[F].unit
    _ <- consumer(id, stateR)
  } yield ()
```

Мы сделали реализацию производителя-потребителя способной обрабатывать отмену. 
Примечательно, что нам не нужно было менять сигнатуры функций `producer` и `consumer`.


## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Fcats%2Feffect%2FProducerConsumer.scala&plain=1)


---

**References:**
- [Cats effect documentation](https://typelevel.org/cats-effect/docs/tutorial#a-nameproducerconsumeraproducer-consumer-problem---concurrency-and-fibers)
