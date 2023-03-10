# Копирование файлов 

## Основные понятия, обработка ресурсов и отмена

Наша цель — создать программу, которая копирует файлы. 
Сначала мы поработаем над функцией, выполняющей такую задачу, а затем создадим программу, 
которую можно будет вызывать из оболочки и использовать эту функцию.

Прежде всего, мы должны написать функцию, которая копирует содержимое из файла в другой файл. 
Функция принимает исходный и конечный файлы в качестве параметров. 
Но это функциональное программирование! 
Таким образом, вызов функции ничего не копирует, вместо этого он возвращает `IO` экземпляр, 
который инкапсулирует все задействованные побочные эффекты (открытие/закрытие файлов, чтение/запись содержимого), 
таким образом сохраняется чистота и отсутствие side effects. 
Только когда этот `IO` экземпляр будет выполнен, будут запущены все эти действия с побочными эффектами. 
В нашей реализации `IO` экземпляр будет возвращать количество байтов, скопированных при выполнении, 
но это всего лишь дизайнерское решение. 
Конечно могут возникать ошибки, но при работе с любыми `IO` их следует закладывать в `IO` пример. 
То есть никакое исключение не возникает за пределами `IO` 
и поэтому при использовании функции не нужно использовать `try` (или подобное), 
вместо этого выполнение `IO` завершится ошибкой и `IO` экземпляр будет её содержать.

Сигнатура функции выглядит так:

```scala
import cats.effect.IO
import java.io.File

def copy(origin: File, destination: File): IO[Long] = ???
```

Функция просто возвращает `IO` экземпляр. При запуске все побочные эффекты будут фактически выполнены, 
и IO экземпляр вернет количество скопированных байтов в виде `Long` 
(обратите внимание, что `IO` параметризовано типом возвращаемого значения). 
Теперь приступим к реализации функции. 

Во-первых, нужно открыть два потока, которые будут читать и записывать содержимое файла.

## Получение и освобождение ресурсов

Мы рассматриваем открытие потока как действие с побочным эффектом, 
поэтому мы должны инкапсулировать эти действия в их собственных `IO` экземплярах. 
Мы можем просто встроить действия, вызвав `IO(action)`, 
но при работе с действиями ввода/вывода рекомендуется использовать вместо этого `IO.blocking(action)`. 
Таким образом, мы помогаем cats-effect лучше спланировать, как назначать потоки действиям. 

Также воспользуемся cats-effect `Resource`. 
Это позволяет упорядоченно создавать, использовать, а затем высвобождать ресурсы. 

См. код:

```scala
import cats.effect.{IO, Resource}
import java.io.*

def inputStream(f: File): Resource[IO, FileInputStream] =
  Resource.make {
    IO.blocking(new FileInputStream(f))                         // построение
  } { inStream =>
    IO.blocking(inStream.close()).handleErrorWith(_ => IO.unit) // высвобождение
  }

def outputStream(f: File): Resource[IO, FileOutputStream] =
  Resource.make {
    IO.blocking(new FileOutputStream(f))                         // построение
  } { outStream =>
    IO.blocking(outStream.close()).handleErrorWith(_ => IO.unit) // высвобождение
  }

def inputOutputStreams(in: File, out: File): Resource[IO, (InputStream, OutputStream)] =
  for {
    inStream  <- inputStream(in)
    outStream <- outputStream(out)
  } yield (inStream, outStream)
```

Мы хотим гарантировать, что потоки будут закрыты после того, как мы закончим их использовать, несмотря ни на что. 
Именно поэтому мы используем `Resource` в обоих функциях `inputStream` и `outputStream`, 
каждая из которых возвращает ту `Resource`, которая инкапсулирует действия по открытию и закрытию каждого потока. 
`inputOutputStreams` инкапсулирует оба ресурса в один экземпляр `Resource`, 
который будет доступен после успешного создания обоих потоков и только в этом случае. 
Как видно из приведенного выше кода, экземпляры `Resource` могут быть объединены в _for-comprehensions_. 
Также обратите внимание, что при освобождении ресурсов мы должны позаботиться о любой возможной ошибке 
во время самого выполнения, например, с вызовом `.handleErrorWith`, как мы делаем в приведенном выше коде. 
В этом случае мы просто игнорируем ошибку, но обычно она должна быть хотя бы запротоколирована. 
Часто вы увидите, что `.attempt.void` используется для получения того же поведения - «проглотить и игнорировать ошибки».

При желании мы могли бы использовать `Resource.fromAutoCloseable` для определения наших ресурсов, 
этот метод создает экземпляры объектов `Resource`, которые реализуют интерфейс `java.lang.AutoCloseable`, 
без необходимости определять, как освобождается ресурс. 
Итак, наша функция `inputStream` будет выглядеть так:

```scala
import cats.effect.{IO, Resource}
import java.io.{File, FileInputStream}

def inputStream(f: File): Resource[IO, FileInputStream] =
  Resource.fromAutoCloseable(IO(new FileInputStream(f)))
```

Этот код намного проще, но с ним мы не можем контролировать, что произойдет, если операция закрытия вызовет исключение. 
Также может случиться так, что мы хотим знать, когда выполняются операции закрытия, например, используя журналы. 
Напротив, использование `Resource.make` позволяет легко контролировать действия фазы выпуска.

Вернемся к нашей функции `copy`, которая теперь выглядит так:

```scala
import cats.effect.{IO, Resource}
import java.io.*

def inputStream(f: File): Resource[IO, FileInputStream] =
  Resource.make {
    IO.blocking(new FileInputStream(f))
  } { inStream =>
    IO.blocking(inStream.close()).handleErrorWith(_ => IO.unit)
  }

def outputStream(f: File): Resource[IO, FileOutputStream] =
  Resource.make {
    IO.blocking(new FileOutputStream(f))
  } { outStream =>
    IO.blocking(outStream.close()).handleErrorWith(_ => IO.unit)
  }

def inputOutputStreams(in: File, out: File): Resource[IO, (InputStream, OutputStream)] =
  for {
    inStream  <- inputStream(in)
    outStream <- outputStream(out)
  } yield (inStream, outStream)

// transfer будет выполнять всю работу
def transfer(origin: InputStream, destination: OutputStream): IO[Long] = ???

def copy(origin: File, destination: File): IO[Long] =
  inputOutputStreams(origin, destination).use { case (in, out) =>
    transfer(in, out)
  }
```

Новый метод `transfer` будет выполнять фактическое копирование данных после получения ресурсов (потоков). 
Когда они больше не нужны, независимо от исхода `transfer` (успеха или неудачи) оба потока будут закрыты. 
Если какой-то из потоков получить не удалось, то `transfer` запускаться не будет. 
Еще лучше, из-за семантики `Resource`, если есть какие-либо проблемы с открытием входного файла, 
выходной файл не будет открыт. 
С другой стороны, если при открытии выходного файла возникает проблема, входной поток будет закрыт.

## Что насчет bracket?

Если вы знакомы с cats effect `Bracket`, вам может быть интересно, почему мы не используем его, 
поскольку он так похож на `Resource` (и для этого есть веская причина: `Resource` основан на `bracket`). 
Хорошо, прежде чем двигаться дальше, стоит взглянуть на `bracket`.

При использовании `bracket` есть три этапа: _приобретение ресурсов_, _использование_ и _высвобождение_. 
Каждый этап определяется `IO` экземпляром. 
Фундаментальное свойство заключается в том, что этап высвобождения всегда будет выполняться независимо от того, 
завершился ли этап использования правильно или во время его выполнения возникло исключение. 
В нашем случае на этапе приобретения мы создадим потоки, затем на этапе использования скопируем содержимое 
и, наконец, на этапе высвобождения закроем потоки. 
Таким образом, мы могли бы определить нашу функцию `copy` следующим образом:

```scala
import cats.effect.IO
import cats.syntax.all.*
import java.io.*

// функция inputOutputStreams не нужна

// transfer будет выполнять всю работу
def transfer(origin: InputStream, destination: OutputStream): IO[Long] = ???

def copy(origin: File, destination: File): IO[Long] = {
  val inIO: IO[InputStream]   = IO(new FileInputStream(origin))
  val outIO: IO[OutputStream] = IO(new FileOutputStream(destination))

  (inIO, outIO) // Этап 1: получение ресурсов
    .tupled     // От (IO[InputStream], IO[OutputStream]) к IO[(InputStream, OutputStream)]
    .bracket { (in, out) =>
      transfer(in, out) // Этап 2: использование ресурсов (в данном случае - для копирования данных)
    } { (in, out) =>                           // Этап 3: высвобождение ресурсов
      (IO(in.close()), IO(out.close())).tupled // От (IO[Unit], IO[Unit]) к IO[(Unit, Unit)]
        .void.handleErrorWith(_ => IO.unit)
    }
}
```

Новое определение `copy` более сложное, хотя код в целом намного короче, 
так как функция `inputOutputStreams` нам не нужна. 
Но в приведенном выше коде есть нюанс. 
При использовании `bracket`, если есть проблема с получением ресурсов на первом этапе, 
то этап высвобождения не запустится. 
В приведенном выше коде сначала открывается исходный файл, 
а затем файл назначения (`.tupled` просто реорганизует оба `IO` экземпляра в один). 
Итак, что произойдет, если мы успешно откроем исходный файл (т.е. при вычислении `inIO`), 
но затем возникнет исключение при открытии целевого файла (т.е. при вычислении `outIO`)? 
В этом случае исходный поток не будет закрыт! 
Чтобы решить эту проблему, мы должны сначала получить первый поток с одним вызовом `bracket`, 
а затем второй поток с другим вызовом `bracket` внутри первого. 
Но в каком-то смысле именно это мы и делаем, когда `flatMap`-им экземпляры `Resource`. 
И код тоже выглядит чище. 
Таким образом, несмотря на то, что прямое использование `bracket` имеет место, 
вероятно, `Resource` будет лучшим выбором при работе с несколькими ресурсами одновременно.

## Копирование данных

Наконец-то стримы готовы! Сейчас мы должны сосредоточиться на кодировании `transfer`. 
Эта функция должна будет определить цикл, который на каждой итерации считывает данные из входного потока в буфер, 
а затем записывает содержимое буфера в выходной поток. 
В то же время цикл будет вести счетчик переданных байтов. 
Чтобы повторно использовать один и тот же буфер, мы должны определить его вне основного цикла 
и оставить фактическую передачу данных другой функции `transmit`, которая использует этот цикл. 
Что-то типа:

```scala
import cats.effect.IO
import cats.syntax.all.*
import java.io.*

def transfer(origin: InputStream, destination: OutputStream): IO[Long] =
  transmit(origin, destination, new Array[Byte](1024 * 10), 0L)

def transmit(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] =
  for {
    amount <- IO.blocking(origin.read(buffer, 0, buffer.length))
    count  <-
      if (amount > -1)
        IO.blocking(destination.write(buffer, 0, amount)) >> transmit(origin, destination, buffer, acc + amount)
      else IO.pure(acc) // Достигнут конец потока чтения (по java.io.InputStream), нечего писать
  } yield count         // Возвращает фактическое количество переданных байтов
```

Взгляните на `transmit`, обратите внимание, что как входные, так и выходные действия создаются путем вызова `IO.blocking`, 
который возвращает действия, инкапсулированные в (приостановленные в) `IO`. 
Мы также можем просто встроить действия, вызвав `IO(action)`, 
но при работе с действиями ввода/вывода рекомендуется вместо этого использовать `IO.blocking(action)`. 
Таким образом, мы помогаем cats-effect лучше спланировать, как назначать потоки действиям. 

Т.к. `IO` - монада, то мы можем упорядочить новые `IO` экземпляры, используя _for-comprehension_, чтобы создать другой `IO`. 
_for-comprehension_ зацикливается до тех пор, пока вызов `read()` не возвращает отрицательное значение, 
которое сигнализировало бы о достижении конца потока. 
`>>` является оператором Cats для последовательности двух операций, где вывод первой не нужен второй 
(т.е. он эквивалентен `first.flatMap(_ => second)`). 
В приведенном выше коде это означает, что после каждой операции записи мы снова рекурсивно вызываем `transmit`, 
но поскольку `IO` безопасен для стека, нас не беспокоят проблемы переполнения. 
На каждой итерации мы увеличиваем счетчик `acc` на количество байтов, прочитанных на этой итерации.

Если во время работы возникнет какое-либо исключение в `transfer`, то потоки будут автоматически закрыты `Resource`. 
Но есть еще кое-что, что мы должны учитывать: выполнение экземпляров `IO` может быть отменено! 
И отмену не стоит игнорировать, так как это ключевая особенность cats-effect. 

## Работа с отменой

Отмена — мощная, но нетривиальная функция cats-effect. 
В cats-effect некоторые `IO` экземпляры могут быть отменены (например, другие `IO` экземпляры, работающие параллельно), 
что означает, что их оценка будет прервана. 
Если программист будет осторожен, альтернативная `IO` задача будет запущена при отмене, 
например, для обработки возможных действий по очистке.

К счастью, `Resource` упрощает отмену. Если внутри `IO` отменяется `Resource.use`, запускается высвобождение этого ресурса. 
В нашем примере это означает, что входной и выходной потоки будут правильно закрыты. 
Кроме того, cats-effect не отменяет код внутри `IO.blocking` экземпляров. 
В случае с функцией `transmit` это означает, что выполнение будет прервано только между двумя вызовами `IO.blocking`. 
Если мы хотим, чтобы выполнение экземпляра `IO` было прервано при отмене, не дожидаясь его завершения,
мы должны создать его экземпляр с помощью `IO.interruptible`.

## IOApp для итоговой программы

Создадим программу, которая копирует файлы и принимает только два параметра: имя исходного и конечного файлов. 
Для кодирования будем использовать `IOApp`, 
так как она позволяет сохранить чистоту в наших определениях вплоть до основной функции программы.

`IOApp` является своего рода «функциональным» эквивалентом Scala `App`, 
где вместо кодирования эффективного метода `main` мы кодируем чистую функцию `run`. 
При выполнении класса `main` метод, определенный в `IOApp`, вызовет функцию `run`, которую мы закодировали. 
Любое прерывание (например, нажатие Ctrl-c) будет рассматриваться как отмена запущенного `IO`.

При кодировании `IOApp` вместо функции `main` у нас есть функция `run`, 
которая создает `IO` экземпляр, формирующий программу. 
В нашем случае наш `run` метод может выглядеть так:

```scala
import cats.effect.*
import java.io.File

object CopyingFiles extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for {
      _     <- if (args.length < 2) IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
               else IO.unit
      orig   = new File(args.head)
      dest   = new File(args(1))
      count <- copy(orig, dest)
      _     <- IO.println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}")
    } yield ExitCode.Success

  // copy был определен выше
  private def copy(origin: File, destination: File): IO[Long] = ???
```

Обратите внимание, как `run` проверяет переданный список `args`. Если аргументов меньше двух, возникает ошибка. 
Так как `IO` реализуют `MonadError`, то мы можем в любой момент вызвать `IO.raiseError`, 
чтобы прервать последовательность `IO` операций. 
Сообщение логов распечатываются удобным способом `IO.println`.

Можно возразить, что использование `IO{java.nio.file.Files.copy(...)}` даст `IO` те же характеристики чистоты, что и наша функция. 
Но есть разница: наш `IO` можно смело отменить! 
Таким образом, пользователь может остановить работающий код в любой момент, например, нажав Ctrl-c, 
наш код будет заниматься безопасным освобождением ресурсов (закрытием потоков) даже при таких обстоятельствах. 
То же самое будет применяться, если функция `copy` запускается из других модулей, которым требуется ее функциональность. 
Если возвращаемое этой функцией значение `IO` будет отменено во время выполнения, 
все равно ресурсы будут правильно освобождены.


## Полиморфный код с cats-effect

Есть важная характеристика `IO`, о которой нужно знать. 
`IO` способен асинхронно приостанавливать побочные эффекты благодаря наличию экземпляра `Async[IO]`. 
Поскольку `Async extends Sync`, то `IO` также может синхронно приостанавливать побочные эффекты. 
Вдобавок к этому `Async` расширяет классы типов, такие как `MonadCancel`, `Concurrent` или `Temporal`, 
которые дают возможность отменить `IO` экземпляр, запустить несколько `IO` экземпляров одновременно, 
установить тайм-аут выполнения, заставить выполнение ждать (sleep) и т.д.

`Sync` и `Async` может приостановить побочные эффекты. 
Мы использовали `IO` до сих пор в основном для этой цели. 
Теперь, возвращаясь к коду, который мы создали для копирования файлов, 
могли ли мы закодировать его функции в терминах some `F[_]: Sync` и `F[_]: Async` вместо `IO`? 
Мы могли бы определить полиморфную версию функции `transfer` с таким подходом, 
просто заменив любое использование `IO` вызовов на методы `delay` и `pure` экземпляра `Sync[F]`.

Только в `main` функции будем устанавливать `IO` как `F` для нашей программы. 
Для этого, конечно, экземпляр `Sync[IO]` должен быть в области видимости, но этот экземпляр передается прозрачно `IOApp`, 
поэтому нам не нужно беспокоиться об этом.


---

**Ссылки:**

- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Fcats%2Feffect%2FCopyFiles.scala&plain=1)
- [Исходный код с полиморфными методами](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Fcats%2Feffect%2FCopyFilesF.scala&plain=1)
- [Cats effect documentation](https://typelevel.org/cats-effect/docs/tutorial#a-namecopyingfilesacopying-files---basic-concepts-resource-handling-and-cancelation)
