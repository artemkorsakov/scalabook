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
      waiting: Option[Deferred[F, A]] // волокно, ожидающее задачу
  )

  class WorkerPool[F[_]: Concurrent, A](
      state: Ref[F, State[F, A]],
      workers: Int
  ):
    // Добавить задачу в очередь
    def submit(a: A): F[Unit] =
      state.modify {
        // Есть ожидающий воркер — передаём задачу напрямую
        case State(queue, Some(waiting)) =>
          (State(queue, None), waiting.complete(a).void)

        // Нет ожидающих — кладём в очередь
        case State(queue, None) =>
          (State(queue.enqueue(a), None), Concurrent[F].unit)
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
      Concurrent[F].deferred[A].flatMap { waiting =>
        state.modify {
          // Есть задача в очереди — отдаём немедленно
          case State(queue, None) if queue.nonEmpty =>
            val (a, newQueue) = queue.dequeue
            (State(newQueue, None), a.pure[F])

          // Нет задач — регистрируем ожидание
          case State(queue, None) =>
            (State(queue, Some(waiting)), waiting.get)

          // Не должно случиться (waiting всегда None при some)
          case State(queue, Some(_)) =>
            (State(queue, None), Concurrent[F].canceled >> take)
        }.flatten
      }
  end WorkerPool

  object WorkerPool:
    def make[F[_]: Concurrent, A](workers: Int): Resource[F, WorkerPool[F, A]] =
      Resource.eval(
        Concurrent[F].ref(State[F, A](Queue.empty, None)).map { state =>
          new WorkerPool[F, A](state, workers)
        }
      )

  // Использование
  def run: IO[Unit] =
    val resource: Resource[IO, WorkerPool[IO, String]] =
      for
        pool <- WorkerPool.make[IO, String](3)
        _    <- pool.start { task =>
          IO.sleep(500.millis) *> IO.println(s"✅ Обработано: $task")
        }
      yield pool

    resource.use { pool =>
      for
        _ <- IO.println("🚀 Отправляю задачи...")
        _ <- (1 to 10).toList.traverse(i => pool.submit(s"Задача $i"))
        _ <- IO.sleep(6.seconds) // Даём время на обработку
        _ <- IO.println("🏁 Завершаем работу")
      yield ()
    }
end WorkerPoolDemo
