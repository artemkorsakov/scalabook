package libs.cats.effect

import cats.effect.*
import cats.syntax.all.*

import java.io.*

object CopyFilesF extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for {
      _     <- if (args.length < 2)
                 IO.raiseError(
                   new IllegalArgumentException("Need origin and destination files")
                 )
               else IO.unit
      orig   = new File(args.head)
      dest   = new File(args(1))
      count <- copy[IO](orig, dest)
      _     <- IO.println(
                 s"$count bytes copied from ${orig.getPath} to ${dest.getPath}"
               )
    } yield ExitCode.Success

  private def copy[F[_]: Sync](origin: File, destination: File): F[Long] =
    inputOutputStreams(origin, destination).use { (in, out) =>
      transfer(in, out)
    }

  private def inputOutputStreams[F[_]: Sync](
      in: File,
      out: File
  ): Resource[F, (InputStream, OutputStream)] =
    for {
      inStream  <- inputStream(in)
      outStream <- outputStream(out)
    } yield (inStream, outStream)

  private def inputStream[F[_]: Sync](f: File): Resource[F, FileInputStream] =
    Resource.make {
      Sync[F].blocking(new FileInputStream(f))
    } { inStream =>
      Sync[F].blocking(inStream.close()).handleErrorWith(_ => Sync[F].unit)
    }

  private def outputStream[F[_]: Sync](f: File): Resource[F, FileOutputStream] =
    Resource.make {
      Sync[F].blocking(new FileOutputStream(f))
    } { outStream =>
      Sync[F].blocking(outStream.close()).handleErrorWith(_ => Sync[F].unit)
    }

  private def transfer[F[_]: Sync](
      origin: InputStream,
      destination: OutputStream
  ): F[Long] =
    transmit(origin, destination, new Array[Byte](1024 * 10), 0L)

  private def transmit[F[_]: Sync](
      origin: InputStream,
      destination: OutputStream,
      buffer: Array[Byte],
      acc: Long
  ): F[Long] =
    for {
      amount <- Sync[F].blocking(origin.read(buffer, 0, buffer.length))
      count  <-
        if (amount > -1)
          Sync[F].blocking(destination.write(buffer, 0, amount)) >> transmit(
            origin,
            destination,
            buffer,
            acc + amount
          )
        else Sync[F].pure(acc)
    } yield count
