{%
showPrev = true
prevPage = concurrency-primitives.html
showNext = true
nextPage = timeseries.html
%}

# Ввод-вывод (I/O)

## Сетевое взаимодействие (Networking)

Библиотека `fs2-io` поддерживает как TCP, так и UDP через пакет `fs2.io.net`. 
Этот пакет предоставляет чисто функциональные абстракции поверх поддержки сетевого ввода-вывода Java NIO (Non-blocking I/O). 
Пакет полностью использует гарантии безопасности ресурсов, предоставляемые cats-effect и `fs2.Stream`.

### TCP

Трейт `fs2.io.net.Socket` предоставляет механизмы для чтения и записи данных — как в виде отдельных действий, 
так и в составе потоковых программ.

#### Клиенты (Clients)

Для начала давайте напишем клиентскую программу, которая подключается к серверу, отправляет сообщение и читает ответ.

```dotty
import fs2.Chunk
import fs2.io.net.Network
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import cats.syntax.all.*
import com.comcast.ip4s.*

def client[F[_]: {MonadCancelThrow, Console, Network}]: F[Unit] =
  Network[F].connect(SocketAddress(host"localhost", port"5555")).use { socket =>
    socket.write(Chunk.array("Hello, world!".getBytes)) >>
      socket.read(8192).flatMap { response =>
        Console[F].println(s"Response: $response")
      }
  }
```

**Пояснение:**

- Чтобы открыть сокет, подключённый к `localhost:5555`, мы используем метод `connect` на экземпляре `Network` 
  (это класс типов, предоставляющий среду выполнения для создаваемых сокетов).
- `Network[F].connect` возвращает `Resource[F, Socket[F]]`, который автоматически закрывает сокет после использования ресурса. 
  Это гарантирует, что даже в случае ошибки сокет будет корректно освобождён.
- Для записи данных в сокет вызывается `socket.write`, который принимает `Chunk[Byte]` и возвращает `F[Unit]`.
- После завершения записи мы выполняем одно чтение из сокета через `socket.read`, 
  передавая максимальное количество байт, которое хотим прочитать. Метод возвращает `F[Option[Chunk[Byte]]]`:
  - `None` — если сокет достиг конца входных данных (соединение закрыто).
  - `Some` — если чтение вернуло чанк данных (возможно, меньшего размера, чем запрошено).
- Наконец, мы выводим ответ на консоль.

**Важное замечание:** в этом примере мы не используем никакого протокола фреймирования или пакетизации сообщений. 
Поэтому вполне возможно, что одно чтение получит только часть исходного сообщения — например, только байты для `"Hello, w"`. 
Это происходит потому, что TCP является потоковым протоколом и не сохраняет границы сообщений.

Для упрощения этой задачи можно использовать потоки FS2. Трейт `Socket` определяет потоковые операции — `writes` и `reads`. 
Перепишем пример с использованием этих операций:

```dotty
import fs2.{Stream, text}
import fs2.io.net.Network
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import com.comcast.ip4s.*

def client[F[_]: {MonadCancelThrow, Console, Network}]: Stream[F, Unit] =
  Stream.resource(Network[F].connect(SocketAddress(
    host"localhost",
    port"5555"
  ))).flatMap { socket =>
    Stream("Hello, world!")
      .through(text.utf8.encode)
      .through(socket.writes) ++
      socket.reads
        .through(text.utf8.decode)
        .foreach { response =>
          Console[F].println(s"Response: $response")
        }
  }
```

**Изменения в структуре:**

1. Ресурс сокета немедленно поднимается в поток с помощью `Stream.resource`.
2. Мы создаём поток из одной строки `"Hello, world!"`, преобразуем его с помощью `text.utf8.encode` в `Stream[Pure, Byte]`, 
   а затем снова преобразуем с помощью `socket.writes`, что даёт `Stream[F, Unit]`. 
   Метод `socket.writes` возвращает `Pipe`, который записывает каждый чанк входного потока в сокет.
3. Затем мы добавляем поток, который читает ответ — `socket.reads`, дающий `Stream[F, Byte]`, 
   завершающийся при закрытии сокета или получении индикации конца входных данных. 
   Мы преобразуем этот поток с помощью `text.utf8.decode`, получая `Stream[F, String]`, и выводим каждый полученный ответ на консоль.

**Проблема:** эта программа не завершится, пока серверная сторона не закроет сокет или не укажет, что данных больше нет. 
Чтобы исправить это, нам нужен протокол, согласованный как клиентом, так и сервером. 
Поскольку мы работаем с текстом, давайте используем простой протокол, 
где каждый кадр (или «пакет», или «сообщение») завершается символом `\n`. 
Обновим как запись, так и чтение:

```dotty
def client[F[_]: {MonadCancelThrow, Console, Network}]: Stream[F, Unit] =
  Stream.resource(Network[F].connect(SocketAddress(
    host"localhost",
    port"5555"
  ))).flatMap { socket =>
    Stream("Hello, world!")
      .interleave(Stream.constant("\n"))
      .through(text.utf8.encode)
      .through(socket.writes) ++
      socket.reads
        .through(text.utf8.decode)
        .through(text.lines)
        .head
        .foreach { response =>
          Console[F].println(s"Response: $response")
        }
  }
```

**Что изменилось:**

- На стороне записи мы добавили `.interleave(Stream.constant("\n"))` перед кодированием UTF-8. 
  Это приводит к тому, что после каждой входной строки добавляется `"\n"`.
- На стороне чтения мы преобразовали вывод `utf8.decode` с помощью `text.lines`, 
  который выдаёт строки между символами новой строки.
- Наконец, мы вызываем `head`, чтобы взять первую полную строку вывода. 
  Обратите внимание, что остальная часть потока `reads` отбрасывается после обработки первой полной строки. 
  Это приводит к корректному закрытию и очистке сокета.

#### Обработка ошибок подключения (Handling Connection Errors)

Если TCP-соединение не может быть установлено, `connect` завершается ошибкой `fs2.io.net.ConnectException`. 
Чтобы автоматически выполнять повторное подключение, мы можем перехватить `ConnectException` и попытаться подключиться снова.

```dotty
import scala.concurrent.duration.*
import cats.effect.Temporal
import fs2.io.net.{ConnectException, Socket}

def retryingConnect[F[_]: {Temporal, Network}](address: SocketAddress[Host])
    : Stream[F, Socket[F]] =
  Stream.resource(Network[F].connect(address))
    .handleErrorWith {
      case _: ConnectException =>
        retryingConnect(address).delayBy(5.seconds)
      case other => Stream.raiseError(other)
    }

def client[F[_]: {Temporal, Console, Network}]: Stream[F, Unit] =
  retryingConnect(SocketAddress(host"localhost", port"5555")).flatMap {
    socket =>
      Stream("Hello, world!")
        .interleave(Stream.constant("\n"))
        .through(text.utf8.encode)
        .through(socket.writes) ++
        socket.reads
          .through(text.utf8.decode)
          .through(text.lines)
          .head
          .foreach { response =>
            Console[F].println(s"Response: $response")
          }
  }
```

**Пояснение:**

- Мы выделили вызов `Network[F].connect` в отдельный метод `retryingConnect`.
- Метод пытается создать клиента и обрабатывает `ConnectException`. 
  При обнаружении исключения мы рекурсивно вызываем `retryingConnect` с задержкой 5 секунд (благодаря `delayBy`).
- Для использования `delayBy` нам потребовалось добавить ограничение `Temporal` к `F`.
- Этот же паттерн может быть использован для более сложных стратегий повторных попыток — 
  например, экспоненциальные задержки и завершение после фиксированного числа попыток.
- Потоки, вызывающие методы на `Socket`, также могут завершаться исключениями из-за потери нижележащего TCP-соединения. 
  Такие исключения можно обрабатывать аналогичным образом.

#### Серверы (Servers)

Теперь реализуем серверное приложение, взаимодействующее с клиентом, которого мы только что создали. 
Сервер будет простым эхо-сервером — на каждую полученную строку текста он отвечает той же строкой.

```dotty
import cats.effect.Concurrent

def echoServer[F[_]: {Concurrent, Network}]: F[Unit] =
  Stream.resource(Network[F].bind(SocketAddress.port(port"5555"))).map {
    serverSocket =>
      serverSocket.accept.map { clientSocket =>
        clientSocket.reads
          .through(text.utf8.decode)
          .through(text.lines)
          .interleave(Stream.constant("\n"))
          .through(text.utf8.encode)
          .through(clientSocket.writes)
          .handleErrorWith(_ =>
            Stream.empty
          ) // обрабатываем ошибки клиентских сокетов
      }
  }.parJoin(100).compile.drain
```

**Пошаговое объяснение:**

1. Мы начинаем с вызова `Network[F].bind`, который возвращает `Resource[F, ServerSocket[F]]`.
2. `ServerSocket` определяет метод `accept` типа `Stream[F, Socket[F]]`. Это бесконечный поток клиентских сокетов — 
   каждый раз, когда клиент подключается к серверу, генерируется `Socket[F]`, позволяющий взаимодействовать с этим клиентом.
3. Время жизни клиентского сокета управляется общим потоком: например, `flatMap` по сокету будет держать его открытым, 
   пока внутренний поток не завершится, после чего клиентский сокет закрывается, а все нижележащие ресурсы возвращаются среде выполнения.
4. Мы применяем `map` к этому бесконечному потоку клиентов и предоставляем логику обработки отдельного клиента. 
   В данном случае мы читаем из сокета клиента, декодируем полученные байты в UTF-8, извлекаем отдельные строки 
   и записываем каждую строку обратно клиенту. Эта логика реализована как один `Stream[F, Unit]`.
5. Поскольку мы применили `map` к бесконечному потоку клиентов, мы получаем `Stream[F, Stream[F, Unit]]`. 
   Мы преобразуем это в единый `Stream[F, Unit]` через `parJoin(100)`, который выполняет до 100 внутренних потоков параллельно. 
   По мере завершения внутренних потоков из источника извлекаются новые внутренние потоки. 
   Таким образом, `parJoin` контролирует максимальное количество одновременных клиентских запросов, обрабатываемых сервером.
6. Важно обрабатывать ошибки в клиентских потоках при объединении всех потоков вместе.

**Типовой паттерн:**

```
Network[F].bind(address).map(ss => handleClient(ss.accept)).parJoin(maxConcurrentClients)
```

Это очень распространённый паттерн при работе с серверными сокетами.

Более простой эхо-сервер можно было бы реализовать с такой базовой логикой:

```dotty
client.reads.through(client.writes)
```

Однако такая реализация будет отправлять байты обратно клиенту по мере их получения, а не только после получения полных строк текста.

**Пример приложения:** [fs2-chat](https://github.com/typelevel/fs2-chat) — это пример много пользовательского чат-сервера 
и клиента, использующий поддержку TCP в FS2 и [scodec](https://scodec.org) для бинарной обработки данных.

### UDP

Поддержка UDP работает во многом аналогично TCP. 
Трейт `fs2.io.net.DatagramSocket` предоставляет механизмы для чтения и записи UDP-дейтаграмм. 
UDP-сокеты создаются с помощью метода `bindDatagramSocket` на `fs2.io.net.Network`. 
В отличие от TCP, здесь нет разделения на клиентские и серверные сокеты.

Адаптируем пример TCP-клиента для UDP:

```dotty
import fs2.{Stream, text}
import fs2.io.net.Network
import cats.effect.Concurrent
import cats.effect.std.Console
import com.comcast.ip4s.*

def client[F[_]: {Concurrent, Console, Network}]: F[Unit] =
  val address = SocketAddress(ip"127.0.0.1", port"5555")
  Stream.resource(Network[F].bindDatagramSocket()).flatMap { socket =>
    Stream("Hello, world!")
      .through(text.utf8.encode)
      .chunks
      .evalTap(data => socket.write(data, address)) ++
      socket.reads
        .flatMap(datagram => Stream.chunk(datagram.bytes))
        .through(text.utf8.decode)
        .foreach { response =>
          Console[F].println(s"Response: $response")
        }
  }.compile.drain
```

**Отличия от TCP:**

- Мы вызываем `socket.write`, передавая чанк байт и адрес назначения.
- При чтении мы преобразуем `Stream[F, Datagram]` в `Stream[F, Byte]` через `flatMap(datagram => Stream.chunk(datagram.bytes))`. 
  Каждая дейтаграмма содержит не только данные, но и адрес отправителя.
- В остальном пример не изменился.

Серверная реализация для UDP значительно проще, чем для TCP:

```dotty
def echoServer[F[_]: {Concurrent, Network}]: F[Unit] =
  Stream.resource(
    Network[F].bindDatagramSocket(SocketAddress.port(port"5555"))
  ).flatMap { socket =>
    socket.reads.through(socket.writes)
  }.compile.drain
```

**Как это работает:**

- Мы открываем сокет, привязанный к порту `5555`.
- Затем читаем дейтаграммы из этого сокета и записываем каждую дейтаграмму обратно.
- Поскольку каждая полученная дейтаграмма содержит удалённый адрес отправителя, мы можем использовать ту же дейтаграмму для записи.

### TLS (Transport Layer Security)

Пакет `fs2.io.net.tls` предоставляет поддержку TLS поверх TCP и DTLS поверх UDP, 
построенную на основе `javax.net.ssl` на JVM, [s2n-tls][s2n-tls] в Scala Native и [модуля `node:tls`][`node:tls` module] в Node.js.

TLS поверх TCP обеспечивается трейтом `TLSSocket`, который создаётся через `Network.tlsContext`. 
`TLSContext` предоставляет криптографический материал, используемый при установлении TLS-сессии — 
например, набор доверенных сертификатов (иногда называемый хранилищем доверия) 
и, опционально, набор сертификатов, идентифицирующих приложение (иногда называемый хранилищем ключей).

Трейт `TLSContext.Builder` предоставляет множество способов создания `TLSContext`:

- `system` — использует системное хранилище доверия по умолчанию (делегирует `javax.net.ssl.SSLContext.getDefault` на JVM).
- `fromKeyStoreFile(pathToFile, storePassword, keyPassword)` — загружает файл Java Key Store (только для JVM).
- `insecure` — доверяет всем сертификатам (крайне небезопасно! Использовать только для быстрых тестов).

`TLSContext` обычно создаётся при запуске приложения через `Network[F].tlsContext` 
и используется для всех сокетов в течение всего времени жизни приложения. 
После создания `TLSContext` методы `client` и `server` используются для создания экземпляров `TLSSocket` 
(и методы `dtlsClient` / `dtlsServer` для `DTLSSocket`). 
В каждом случае необходимо предоставить обычный сокет, который `TLSSocket` будет использовать для выполнения TLS-рукопожатия, 
а также для передачи и приёма зашифрованных данных. 
`TLSSocket` расширяет `fs2.io.net.Socket`, что делает добавление поддержки TLS простой заменой в программе, использующей `fs2-io`.

Адаптируем TCP-эхо-клиент для TLS:

```dotty
import fs2.{Chunk, Stream, text}
import fs2.io.net.Network
import fs2.io.net.tls.TLSContext
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import cats.syntax.all.*
import com.comcast.ip4s.*

def client[F[_]: {MonadCancelThrow, Console, Network}](
    tlsContext: TLSContext[F]
): Stream[F, Unit] =
  Stream.resource(Network[F].connect(SocketAddress(
    host"localhost",
    port"5555"
  ))).flatMap { underlyingSocket =>
    Stream.resource(tlsContext.client(underlyingSocket)).flatMap { socket =>
      Stream("Hello, world!")
        .interleave(Stream.constant("\n"))
        .through(text.utf8.encode)
        .through(socket.writes) ++
        socket.reads
          .through(text.utf8.decode)
          .through(text.lines)
          .head
          .foreach { response =>
            Console[F].println(s"Response: $response")
          }
    }
  }
```

**Единственное отличие:** мы оборачиваем нижележащий сокет в `TLSSocket`, что обеспечивает шифрование всего трафика.

#### Настройка параметров TLS-сессии

Различные методы на `TLSContext`, создающие `TLSSocket` и `DTLSSocket`, принимают аргумент `TLSParameters`, 
позволяющий настраивать параметры сессии. Это позволяет настраивать такие вещи, как аутентификация клиента, 
поддерживаемые протоколы и наборы шифров, а также расширения SNI.

Например:

```dotty
import fs2.io.net.tls.{TLSParameters, TLSSocket}
import cats.effect.Resource
import javax.net.ssl.SNIHostName

def tlsClientWithSni[F[_]: Network](
    tlsContext: TLSContext[F],
    address: SocketAddress[Host]
): Resource[F, TLSSocket[F]] =
  Network[F].connect(address).flatMap { underlyingSocket =>
    tlsContext.clientBuilder(
      underlyingSocket
    ).withParameters(
      TLSParameters(
        protocols = Some(List("TLSv1.3")),
        serverNames = Some(List(new SNIHostName(address.host.toString)))
      )
    ).build
  }
```

В этом примере мы настроили TLS-сессию на использование только TLS 1.3 и добавили расширение SNI с именем хоста целевого сервера. 
Это особенно важно при работе с виртуальным хостингом, когда один IP-адрес обслуживает несколько доменов.

#### Доступ к информации о TLS-сессии

`TLSSocket` расширяет `Socket` и предоставляет несколько дополнительных методов. 
Особый интерес представляет метод `session`, который возвращает `F[javax.net.ssl.SSLSession]`, 
содержащий информацию об установленной TLS-сессии. 
Это позволяет нам запрашивать такие данные, как сертификат узла или согласованный набор шифров.

В следующем примере мы извлекаем различную информацию о сессии, чтобы помочь в отладке TLS-соединений:

```dotty
def debug[F[_]: {MonadCancelThrow, Network}](
    tlsContext: TLSContext[F],
    address: SocketAddress[Host]
): F[String] =
  Network[F].connect(address).use { underlyingSocket =>
    tlsContext
      .clientBuilder(underlyingSocket)
      .withParameters(
        TLSParameters(serverNames =
          Some(List(new SNIHostName(address.host.toString)))
        )
      )
      .build
      .use { tlsSocket =>
        tlsSocket.write(Chunk.empty) >>
          tlsSocket.session.map { session =>
            s"Cipher suite: ${session.getCipherSuite}\r\n" +
              "Peer certificate chain:\r\n" +
              session.getPeerCertificates.zipWithIndex
                .map { case (cert, idx) => s"Certificate $idx: $cert" }
                .mkString("\r\n")
          }
      }
  }
```

Этот код:

1. Устанавливает TLS-соединение с указанным адресом.
2. Отправляет пустой чанк (просто чтобы инициировать рукопожатие).
3. Извлекает объект `SSLSession`, который содержит подробную информацию о соединении.
4. Возвращает строку с набором шифров и цепочкой сертификатов узла.

## Файлы (Files)

Пакет `fs2.io.file` предоставляет поддержку для работы с файлами. 
Пример из README демонстрирует два простейших случая использования — инкрементальное чтение из файла 
с помощью `fs2.io.file.readAll` и инкрементальная запись в файл с помощью `fs2.io.file.writeAll`.

Другой пример — вычисление SHA-256 дайджеста файла и запись его в шестнадцатеричном виде в соседний файл:

```dotty
import cats.effect.Concurrent
import cats.syntax.functor.*
import fs2.text
import fs2.Stream
import fs2.hashing.{Hashing, HashAlgorithm}
import fs2.io.file.{Files, Path}

def writeDigest[F[_]: {Files, Hashing, Concurrent}](path: Path): F[Path] =
  val target = Path(path.toString + ".sha256")
  Files[F].readAll(path)
    .through(Hashing[F].hash(HashAlgorithm.SHA256))
    .flatMap(h => Stream.chunk(h.bytes))
    .through(text.hex.encode)
    .through(text.utf8.encode)
    .through(Files[F].writeAll(target))
    .compile
    .drain
    .as(target)
```

**Объяснение:**

1. `Files[F].readAll(path)` — потоково читает файл, выдавая байты.
2. `Hashing[F].hash(HashAlgorithm.SHA256)` — вычисляет SHA-256 хеш от входного потока. Возвращает `Stream[F, Hash]`.
3. `.flatMap(h => Stream.chunk(h.bytes))` — преобразует `Hash` в поток байт.
4. `text.hex.encode` — кодирует байты в шестнадцатеричный текст.
5. `text.utf8.encode` — кодирует текст в UTF-8 байты.
6. `Files[F].writeAll(target)` — записывает результат в целевой файл.

### Расширенная работа с файлами

Для более сложных случаев использования доступны несколько типов:

- `FileHandle[F]` — представляет открытый файл и предоставляет различные методы для взаимодействия с файлом: 
  чтение данных, запись данных, запрос размера и т.д. — всё в эффекте `F`. 
  Создаётся вызовом `FileHandle.fromPath(path, blocker)`.
- `ReadCursor` — объединяет `FileHandle[F]` со смещением в файле в байтах. 
  Методы на `ReadCursor` выполняют операции чтения, начиная с текущего смещения, 
  и возвращают обновлённый курсор вместе с прочитанными данными.
- `WriteCursor` — аналогично, объединяет `FileHandle[F]` со смещением. 
  Методы используют смещение как позицию для записи следующего чанка байт, возвращая обновлённый курсор.

Эти типы полезны, когда вам нужно выполнять операции с файлами с произвольным доступом (random access) 
или когда требуется более тонкий контроль над позицией чтения/записи.

### Работа с файловой системой

Объект пакета `fs2.io.file` также предоставляет множество способов взаимодействия с файловой системой: 
перемещение файлов, создание каталогов, обход всех путей в дереве каталогов, отслеживание изменений в каталогах и т.д.

Например, подсчёт общего количества байтов в дереве каталогов можно выполнить одной строкой кода:

```dotty
def totalBytes[F[_]: {Files, Concurrent}](path: Path): F[Long] =
  Files[F].walk(path).evalMap(p => Files[F].size(p).handleError(_ => 0L)).compile.foldMonoid
```

**Как это работает:**

- `Files[F].walk(path)` — обходит все пути в дереве каталогов, начиная с `path`, выдавая каждый путь.
- `.evalMap(p => Files[F].size(p).handleError(_ => 0L))` — для каждого пути получает размер файла. 
  Если файл недоступен (например, ошибка доступа), возвращает 0.
- `.compile.foldMonoid` — суммирует все размеры, используя моноид для `Long` (сложение).

### Более сложный пример: подсчёт строк кода на Scala

Объединяя `walk`, `readAll` и различные операции парсинга, мы можем подсчитать количество строк кода Scala:

```dotty
def scalaLineCount[F[_]: {Files, Concurrent}](path: Path): F[Long] =
  Files[F].walk(path).filter(_.extName == ".scala").flatMap { p =>
    Files[F].readAll(p).through(text.utf8.decode).through(text.lines).as(1L)
  }.compile.foldMonoid
```

**Объяснение:**

1. `Files[F].walk(path)` — обходит дерево каталогов.
2. `.filter(_.extName == ".scala")` — оставляет только файлы с расширением `.scala`.
3. Для каждого файла:
    - `Files[F].readAll(p)` — читает файл потоково.
    - `.through(text.utf8.decode)` — декодирует в строки.
    - `.through(text.lines)` — разбивает на строки.
    - `.as(1L)` — превращает каждую строку в число 1.
4. `.compile.foldMonoid` — суммирует все единицы, получая общее количество строк.

### Файловые системы и кастомные пути

Объект `Files` не зависит от конкретной файловой системы. 
Можно использовать кастомную файловую систему, например, смонтированную внутри JAR-файла, чтобы получить доступ к ресурсам. 
Если уже создана `java.nio.file.FileSystem`, взаимодействие с файлами можно осуществить, вызвав `Path.fromFsPath` для получения объекта `Path`.

## Процессы (Processes)

Пакет `fs2.io.process` предоставляет API для запуска и взаимодействия с нативными процессами.

Часто вас интересует вывод процесса. Вот как можно получить вывод команды `echo`:

```dotty
import cats.effect.{Concurrent, MonadCancelThrow}
import fs2.io.process.{Processes, ProcessBuilder}
import fs2.text

def helloProcess[F[_]: {Concurrent, Processes}]: F[String] =
  ProcessBuilder("echo", "Hello, process!").spawn.use { process =>
    process.stdout.through(text.utf8.decode).compile.string
  }
```

**Объяснение:**

- `ProcessBuilder("echo", "Hello, process!")` — создаёт описание процесса.
- `.spawn` — запускает процесс, возвращая `Resource[F, Process[F]]`.
- `process.stdout` — поток байт из стандартного вывода процесса.
- `.through(text.utf8.decode)` — декодирует байты в строки.
- `.compile.string` — собирает все строки в одну.

### Ввод данных в процесс

Вы также можете передавать данные в процесс. Пример с `wc --words` (подсчёт слов):

```dotty
def wordCountProcess[F[_]: {Concurrent, Processes}](words: String)
    : F[Option[Int]] =
  ProcessBuilder("wc", "--words").spawn.use { process =>
    val in = Stream.emit(words).through(text.utf8.encode).through(process.stdin)
    val out = process.stdout.through(text.utf8.decode)
    out.concurrently(in).compile.string.map(_.strip.toIntOption)
  }
```

**Объяснение:**

- `process.stdin` — это `Pipe[F, Byte, Nothing]` для записи данных в стандартный ввод процесса.
- `out.concurrently(in)` — запускает поток записи `in` параллельно с потоком чтения `out`. 
  Поток `out` будет читать вывод, пока `in` записывает данные. 
  Когда `in` завершится, `out` продолжит чтение до закрытия stdout процесса.
- `.compile.string` — собирает вывод в строку.
- `.map(_.strip.toIntOption)` — удаляет пробелы и преобразует в `Int`.

### Ожидание завершения процесса

Или просто дождаться завершения процесса и получить код выхода:

```dotty
def sleepProcess[F[_]: {MonadCancelThrow, Processes}]: F[Int] =
  ProcessBuilder("sleep", "1s").spawn.use(_.exitValue)
```

`exitValue` возвращает `F[Int]` с кодом завершения процесса.

### Управление временем жизни процесса

Чтобы завершить работающий процесс, достаточно выйти из блока `use`:

```dotty
import cats.effect.Temporal
import scala.concurrent.duration.*

def oneSecondServer[F[_]: {Temporal, Processes}]: F[Unit] =
  ProcessBuilder("python", "-m", "http.server").spawn.surround {
    Temporal[F].sleep(1.second)
  }
```

**Объяснение:**

- `.spawn` запускает процесс и возвращает `Resource[F, Process[F]]`.
- `.surround` гарантирует, что ресурс (процесс) будет освобождён после выполнения переданного эффекта.
- `Temporal[F].sleep(1.second)` — ждёт 1 секунду, после чего ресурс освобождается, и процесс завершается (отправляется сигнал SIGTERM).

## Консольные операции (Console Operations)

Запись в консоль часто сводится к `s.evalMap(o => IO.println(o))`. 
Это хорошо работает для быстрых тестов, но может быть проблематично в больших приложениях. 
Вызов `println` блокируется до тех пор, пока данные не будут записаны в поток вывода стандартного вывода. 
Это может вызывать проблемы с честностью планирования (fairness) с другими неблокирующими операциями, 
выполняющимися в основном пуле потоков.

Для таких ситуаций `fs2-io` предоставляет несколько утилит:

```dotty
def stdoutLines[F[_]: Sync, O: Show](
    charset: Charset = utf8Charset
): Pipe[F, O, Nothing]

def stdout[F[_]: Sync]: Pipe[F, Byte, Nothing]
```

Оба этих `Pipe` предоставляются в объекте пакета `fs2.io`. 
Метод `stdoutLines` использует экземпляр `Show[O]` для преобразования элементов потока в строки. 
В отличие от `println`, эти операции выполняют запись в консоль с использованием специального пула потоков 
для блокирующих операций (blocking pool), что предотвращает проблемы с основным пулом.

Объект пакета `fs2.io` также предоставляет несколько утилит для чтения значений из консоли:

```dotty
def stdin[F[_]: Sync](bufSize: Int): Stream[F, Byte]

def stdinUtf8[F[_]: Sync](bufSize: Int): Stream[F, String]
```

Как и их аналоги для вывода, эти операции выполняют блокирующее чтение на пуле для блокирующих операций.

## Взаимодействие с Java-потоками (Java Stream Interop)

Объект пакета `fs2.io` предоставляет возможность взаимодействия с `java.io.InputStream` и `java.io.OutputStream`.

- `fs2.io.readInputStream` (и `unsafeReadInputStream` — см. ScalaDoc для различий) создаёт `Stream[F, Byte]` из `InputStream`.
- `fs2.io.writeOutputStream` предоставляет `Pipe`, который записывает байты, выдаваемые `Stream[F, Byte]`, в `OutputStream`.
- `fs2.io.readOutputStream` создаёт `Stream[F, Byte]` из функции, которая записывает данные в `OutputStream`.

**Примеры использования:**

```dotty
import cats.effect.IO
import fs2.*
import fs2.io.{readInputStream, readOutputStream, writeOutputStream}
import java.io.{InputStream, OutputStream}

// Чтение из InputStream
val inputStream: InputStream      = ???
val bytesStream: Stream[IO, Byte] = readInputStream[IO](IO(inputStream), 8192)

// Запись в OutputStream
val outputStream: OutputStream      = ???
val writePipe: Pipe[IO, Byte, Unit] = writeOutputStream[IO](IO(outputStream))

// Создание потока из функции, записывающей в OutputStream
val generatedStream: Stream[IO, Byte] =
  readOutputStream[IO](8192) { out =>
    IO {
      out.write("Hello".getBytes)
      out.flush()
    }
  }
```

Эти функции особенно полезны при интеграции с существующим Java-кодом или библиотеками, 
работающими с классическими потоками ввода-вывода.

---

**Ссылки:**

- [Fs2 home page][home]

[home]: https://fs2.io/#/
[s2n-tls]: https://github.com/aws/s2n-tls
[`node:tls` module]: https://nodejs.org/api/tls.html
