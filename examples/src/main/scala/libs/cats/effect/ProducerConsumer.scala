package libs.cats.effect

import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*

import scala.collection.immutable.Queue

object ProducerConsumer extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for {
      queueR <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
      res <- (consumer(queueR), producer(queueR, 0))
        .parMapN((_, _) => ExitCode.Success) // Запуск producer и consumer в параллели до окончания выполнения (до отмены пользователем по CTRL-C)
        .handleErrorWith { t =>
          Console[IO].errorln(s"Error caught: ${t.getMessage}").as(ExitCode.Error)
        }
    } yield res

  private def producer[F[_]: Sync: Console](queueR: Ref[F, Queue[Int]], counter: Int): F[Unit] =
    for {
      _ <- if counter % 10000 == 0 then Console[F].println(s"Produced $counter items") else Sync[F].unit
      _ <- queueR.getAndUpdate(_.enqueue(counter + 1))
      _ <- producer(queueR, counter + 1)
    } yield ()

  private def consumer[F[_]: Sync: Console](queueR: Ref[F, Queue[Int]]): F[Unit] =
    for {
      iO <- queueR.modify { queue =>
              queue.dequeueOption.fold((queue, Option.empty[Int])) { (i, queue) => (queue, Option(i)) }
            }
      _  <- if iO.exists(_ % 10000 == 0) then Console[F].println(s"Consumed ${iO.get} items") else Sync[F].unit
      _  <- consumer(queueR)
    } yield ()
