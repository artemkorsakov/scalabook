package ru.scalabook.libs.cats.effect

import cats.effect.*
import cats.syntax.all.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.io.*

object CopyFiles extends IOApp:
  private given Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <-
        if args.length < 2 then
          IO.raiseError(
            new IllegalArgumentException("Не указаны исходный и целевой файлы")
          )
        else IO.unit
      orig = new File(args.head)
      dest = new File(args(1))
      count <- copy[IO](orig, dest)
      _     <- IO.println(
        s"$count bytes copied from ${orig.getPath} to ${dest.getPath}"
      )
    yield ExitCode.Success

  private def copy[F[_]: {Sync, Logger}](
      origin: File,
      destination: File
  ): F[Long] =
    inputOutputStreams(origin, destination).use { (in, out) =>
      transfer(in, out)
    }

  private def inputOutputStreams[F[_]: {Sync, Logger}](
      in: File,
      out: File
  ): Resource[F, (InputStream, OutputStream)] =
    for
      inStream  <- inputStream(in)
      outStream <- outputStream(out)
    yield (inStream, outStream)

  private def inputStream[F[_]: {Sync, Logger}](
      f: File
  ): Resource[F, FileInputStream] =
    Resource.make {
      Sync[F].blocking(new FileInputStream(f))
    } { inStream =>
      Sync[F].blocking(inStream.close())
        .handleErrorWith { error =>
          Logger[F].error(error)("Не смог закрыть исходный файл")
        }
    }

  private def outputStream[F[_]: {Sync, Logger}](
      f: File
  ): Resource[F, FileOutputStream] =
    Resource.make {
      Sync[F].blocking(new FileOutputStream(f))
    } { outStream =>
      Sync[F].blocking(outStream.close())
        .handleErrorWith { error =>
          Logger[F].error(error)("Не смог закрыть целевой файл")
        }
    }

  private def transfer[F[_]: Sync](
      origin: InputStream,
      destination: OutputStream
  ): F[Long] =
    val buffer = new Array[Byte](1024 * 10) // 10 KB буфер

    def loop(acc: Long): F[Long] =
      for
        bytesRead <- Sync[F].blocking(origin.read(buffer, 0, buffer.length))
        // Если прочитано -1 (конец файла), возвращаем накопленную сумму
        result <- if bytesRead == -1 then
          Sync[F].pure(acc) // конец потока, возвращаем результат
        else
          // Записываем прочитанный блок
          Sync[F].blocking(destination.write(buffer, 0, bytesRead)) >>
            loop(acc + bytesRead)
      yield result

    loop(0L)
  end transfer
end CopyFiles
