package libs.cats.effect

import cats.effect.*

import java.io.*

object CopyFiles extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for {
      _     <- if args.length < 2 then IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
               else IO.unit
      orig   = new File(args.head)
      dest   = new File(args(1))
      count <- copy(orig, dest)
      _     <- IO.println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}")
    } yield ExitCode.Success

  private def copy(origin: File, destination: File): IO[Long] =
    inputOutputStreams(origin, destination).use { (in, out) => transfer(in, out) }

  private def inputOutputStreams(in: File, out: File): Resource[IO, (InputStream, OutputStream)] =
    for
      inStream  <- inputStream(in)
      outStream <- outputStream(out)
    yield (inStream, outStream)

  private def inputStream(f: File): Resource[IO, FileInputStream] =
    Resource.make {
      IO.blocking(new FileInputStream(f))
    } { inStream =>
      IO.blocking(inStream.close()).handleErrorWith(_ => IO.unit)
    }

  private def outputStream(f: File): Resource[IO, FileOutputStream] =
    Resource.make {
      IO.blocking(new FileOutputStream(f))
    } { outStream =>
      IO.blocking(outStream.close()).handleErrorWith(_ => IO.unit)
    }

  private def transfer(origin: InputStream, destination: OutputStream): IO[Long] =
    transmit(origin, destination, new Array[Byte](1024 * 10), 0L)

  private def transmit(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] =
    for
      amount <- IO.blocking(origin.read(buffer, 0, buffer.length))
      count  <-
        if amount > -1 then
          IO.blocking(destination.write(buffer, 0, amount)) >> transmit(origin, destination, buffer, acc + amount)
        else IO.pure(acc)
    yield count
