package ru.scalabook.libs.cats.effect

import cats.effect.*
import cats.effect.std.Console
import cats.effect.syntax.all.*
import cats.syntax.all.*

import scala.collection.immutable.Queue

object ProducerConsumer extends IOApp:
  private case class State[F[_], A](
      queue: Queue[A],                        // элементы, ожидающие обработки
      capacity: Int,                          // максимальный размер очереди
      takers: Queue[Deferred[F, A]],          // потребители, ожидающие элемент
      offerers: Queue[(A, Deferred[F, Unit])] // производители, ожидающие места
  )

  private object State:
    def empty[F[_], A](capacity: Int): State[F, A] =
      State(Queue.empty, capacity, Queue.empty, Queue.empty)

  override def run(args: List[String]): IO[ExitCode] =
    for
      // Создаём состояние с максимальным размером очереди 100
      stateR <- Ref.of[IO, State[IO, Int]](State.empty[IO, Int](capacity = 100))
      counterR <- Ref.of[IO, Int](1)

      producers = List.range(1, 11).map(producer(_, counterR, stateR))
      consumers = List.range(1, 11).map(consumer(_, stateR))

      res <- (producers ++ consumers)
        .parSequence
        .as(ExitCode.Success)
        .handleErrorWith { t =>
          Console[IO].errorln(s"Ошибка: ${t.getMessage}").as(ExitCode.Error)
        }
    yield res

  private def producer[F[_]: {Async, Console}](
      id: Int,
      counterR: Ref[F, Int],
      stateR: Ref[F, State[F, Int]]
  ): F[Unit] =
    def offer(i: Int): F[Unit] =
      Deferred[F, Unit].flatMap { offerer =>
        // Защищаем критическую секцию от отмены
        Async[F].uncancelable { poll =>
          stateR.modify {
            // Сценарий 1: Есть ожидающие потребители
            case State(queue, capacity, takers, offerers) if takers.nonEmpty =>
              val (taker, rest) = takers.dequeue
              State(queue, capacity, rest, offerers) -> taker.complete(i).void

            // Сценарий 2: Нет ждущих потребителей, очередь не заполнена
            case State(queue, capacity, takers, offerers)
                if queue.size < capacity =>
              State(queue.enqueue(i), capacity, takers, offerers) ->
                Async[F].unit

            // Сценарий 3: Нет ждущих потребителей, очередь заполнена
            case State(queue, capacity, takers, offerers) =>
              // Функция очистки: удаляем offerer из очереди, если отмена произойдёт
              val cleanup = stateR.update { s =>
                s.copy(offerers = s.offerers.filter(_._2 ne offerer))
              }
              // Возвращаем блокирующий эффект, обёрнутый в poll (отменяемый)
              State(queue, capacity, takers, offerers.enqueue(i -> offerer)) ->
                poll(offerer.get).onCancel(cleanup)
          }.flatten
        }
      }

    for
      i <- counterR.getAndUpdate(_ + 1)
      _ <- offer(i)
      _ <- Async[F].whenA(i % 100000 == 0)(
        Console[F].println(s"Производитель $id произвёл $i элементов")
      )
      _ <- producer(id, counterR, stateR)
    yield ()
  end producer

  private def consumer[F[_]: {Async, Console}](
      id: Int,
      stateR: Ref[F, State[F, Int]]
  ): F[Unit] =
    val take: F[Int] =
      Deferred[F, Int].flatMap { taker =>
        Async[F].uncancelable { poll =>
          stateR.modify {
            // Сценарий 1: Очередь не пуста, нет ожидающих производителей
            case State(queue, capacity, takers, offerers)
                if queue.nonEmpty && offerers.isEmpty =>
              val (i, rest) = queue.dequeue
              State(rest, capacity, takers, offerers) -> Async[F].pure(i)

            // Сценарий 2: Очередь не пуста, есть ожидающие производители
            case State(queue, capacity, takers, offerers) if queue.nonEmpty =>
              val (i, rest)               = queue.dequeue
              val ((move, release), tail) = offerers.dequeue
              State(rest.enqueue(move), capacity, takers, tail) ->
                release.complete(()).as(i)

            // Сценарий 3: Очередь пуста, но есть ожидающие производители
            case State(queue, capacity, takers, offerers)
                if offerers.nonEmpty =>
              val ((i, release), rest) = offerers.dequeue
              State(queue, capacity, takers, rest) -> release.complete(()).as(i)

            // Сценарий 4: Очередь пуста и нет ожидающих производителей — блокируемся
            case State(queue, capacity, takers, offerers) =>
              // Функция очистки: удаляем taker из очереди при отмене
              val cleanup = stateR.update { s =>
                s.copy(takers = s.takers.filter(_ ne taker))
              }
              State(queue, capacity, takers.enqueue(taker), offerers) ->
                poll(taker.get).onCancel(cleanup)
          }.flatten
        }
      }

    for
      i <- take
      _ <- Async[F].whenA(i % 100000 == 0)(
        Console[F].println(s"Потребитель $id обработал $i элементов")
      )
      _ <- consumer(id, stateR)
    yield ()
  end consumer
end ProducerConsumer
