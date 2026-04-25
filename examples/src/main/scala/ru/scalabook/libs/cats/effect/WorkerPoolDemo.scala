package ru.scalabook.libs.cats.effect

import cats.*
import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*
import cats.syntax.all.*

import scala.collection.immutable.Queue
import scala.concurrent.duration.*

object WorkerPoolDemo extends IOApp.Simple:
  case class State[F[_], A](
      queue: Queue[A],
      waiting: Queue[Deferred[F, A]] // ← очередь ожидающих воркеров
  )

  class WorkerPool[F[_]: Concurrent, A](
      state: Ref[F, State[F, A]],
      workers: Int
  ):
    // Добавить задачу в очередь
    def submit(a: A): F[Unit] =
      state.modify {
        // Есть ожидающие воркеры — отдаём первому
        case State(queue, waiting) if waiting.nonEmpty =>
          val (waitingDeferred, newWaiting) = waiting.dequeue
          (State(queue, newWaiting), waitingDeferred.complete(a).void)

        // Нет ожидающих — кладём в очередь
        case State(queue, waiting) =>
          (State(queue.enqueue(a), waiting), Concurrent[F].unit)
      }.flatten

    // Запустить воркеров
    def start(workerTask: A => F[Unit]): Resource[F, Unit] =
      (1 to workers).toList.traverse { _ =>
        Resource.make(
          take.flatMap(workerTask).foreverM.start // запускаем
        )(_.cancel)                               // и отменяем при освобождении
      }.map(_ => ()) // объединяем все Resource в один

    // Забрать задачу (воркер вызывает этот метод)
    private def take: F[A] =
      Concurrent[F].deferred[A].flatMap { waitingDeferred =>
        state.modify {
          // Есть задача в очереди — отдаём немедленно
          case State(queue, waiting) if queue.nonEmpty =>
            val (a, newQueue) = queue.dequeue
            (State(newQueue, waiting), a.pure[F])

          // Нет задач — регистрируем ожидание
          case State(queue, waiting) =>
            (
              State(queue, waiting.enqueue(waitingDeferred)),
              waitingDeferred.get
            )
        }.flatten
      }
  end WorkerPool

  object WorkerPool:
    def make[F[_]: Concurrent, A](workers: Int): Resource[F, WorkerPool[F, A]] =
      Resource.eval(
        // Пустая очередь и пустая очередь ожидающих
        Concurrent[F].ref(State[F, A](Queue.empty, Queue.empty)).map { state =>
          new WorkerPool[F, A](state, workers)
        }
      )

  // Демонстрация параллельной работы
  def run: IO[Unit] =
    val resource: Resource[IO, WorkerPool[IO, String]] =
      for
        pool <- WorkerPool.make[IO, String](3)
        _    <- pool.start { task =>
          // Каждая задача выполняется 1 секунду
          IO.sleep(1.second) *>
            IO.println(
              s"✅ ${Thread.currentThread().getName} обработал: $task [${java.time.LocalTime.now()}]"
            )
        }
      yield pool

    resource.use { pool =>
      for
        _ <- IO.println("🚀 Отправляю 10 задач...")
        _ <- (1 to 10).toList.traverse(i => pool.submit(s"Задача $i"))
        _ <- IO.sleep(5.seconds) // Даём время на обработку
        _ <- IO.println("🏁 Завершаем работу")
      yield ()
    }
  end run
end WorkerPoolDemo
