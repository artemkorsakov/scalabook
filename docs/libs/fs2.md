# FS2

[**FS2**][home] — это библиотека для работы с функциональными, эффективными и параллельными потоками (streams) в языке Scala.

Она предоставляет мощные абстракции для обработки данных в потоковом режиме, 
позволяя работать с вводом-выводом (например, сетевыми соединениями и файлами) с постоянным потреблением памяти, 
выполнять преобразования с сохранением состояния (stateful transformations), 
гарантировать безопасное управление ресурсами и контролировать побочные эффекты.

Библиотека построена на основе [**Cats Effect**](../cats-effect/index.md)
(известного фреймворка для асинхронного и конкурентного программирования в Scala) 
и служит фундаментом для ряда популярных проектов экосистемы Typelevel, включая:

- [**http4s**](https://http4s.org) — функциональную HTTP-библиотеку,
- [**Skunk**](https://typelevel.org/skunk/) — чисто функциональный PostgreSQL-клиент,
- [**Doobie**](https://tpolecat.github.io/doobie/) — JDBC-библиотеку для работы с базами данных в функциональном стиле.

## Подключение зависимостей (SBT)

Чтобы начать использовать FS2, добавьте в ваш `build.sbt` следующие зависимости ([актуальная версия](https://mvnrepository.com/artifact/co.fs2)):

```sbt
// Основной модуль — обязателен для работы с потоками
libraryDependencies += "co.fs2" %% "fs2-core" % "3.13.0"

// Дополнительный модуль для операций ввода-вывода (файлы, сеть)
libraryDependencies += "co.fs2" %% "fs2-io" % "3.13.0"

// Модуль для интеграции с Reactive Streams (стандарт асинхронных потоков)
libraryDependencies += "co.fs2" %% "fs2-reactive-streams" % "3.13.0"

// Модуль для работы с бинарными форматами через библиотеку scodec
libraryDependencies += "co.fs2" %% "fs2-scodec" % "3.13.0"
```

## Пример использования

FS2 — это библиотека потокового ввода-вывода. 
Её ключевые принципы — композиционность, выразительность, безопасность ресурсов и высокая производительность.

Рассмотрим простой, но показательный пример:

```dotty
import cats.effect.{IO, IOApp}
import fs2.{Stream, text}
import fs2.io.file.{Files, Path}

object Converter extends IOApp.Simple:

  private val converter: Stream[IO, Unit] =
    def fahrenheitToCelsius(f: Double): Double = (f - 32.0) * (5.0 / 9.0)

    Files[IO].readUtf8Lines(Path("testdata/fahrenheit.txt"))
      .withFilter(s => s.trim.nonEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble).toString)
      .intersperse("\n")
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(Path("testdata/celsius.txt")))

  // Точка входа в программу — запуск потока с финальным сбором результатов
  def run: IO[Unit] =
    converter.compile.drain
end Converter
```

### Что делает этот код?

Программа читает файл `testdata/fahrenheit.txt` **построчно**, _не загружая его целиком в память_.  
Она пропускает пустые строки и строки, начинающиеся с `//` (комментарии).  
Каждая непустая строка интерпретируется как температура в градусах Фаренгейта, преобразуется в Цельсий 
и записывается в виде числа в выходной файл `testdata/celsius.txt`.

> Запись также выполняется **постепенно, по мере поступления данных** (инкрементально),
> что обеспечивает **постоянное потребление памяти** (constant memory) — 
> объём использованной памяти не зависит от размера входных и выходных файлов.

Кроме того, библиотека автоматически закрывает файловые дескрипторы как при нормальном завершении,
так и в случае возникновения исключений — это пример **ресурсной безопасности**.

Обратите внимание, что для простоты пример специализирован на эффекте `IO` (из Cats Effect). 
Однако тип `Stream[F, A]` полностью полиморфен по эффекту `F[_]` — он может работать с любым эффектом, 
поддерживающим требуемые классы типов Cats Effect (например, `IO`, `Task`, `SyncIO` и другие).

## Дополнительные возможности

Библиотека FS2 выходит далеко за рамки простого файлового ввода-вывода и предоставляет множество продвинутых сценариев использования:

### 1. Соединение и слияние потоков (Zipping and merging)

Вы можете комбинировать несколько источников данных в одном потоке, например, объединять (zip) 
или сливать (merge) элементы из разных потоков с использованием произвольной функции преобразования. 
Гибкость композиции потоков практически не ограничена благодаря богатой алгебре комбинаторов.

### 2. Динамическое выделение ресурсов (Dynamic resource allocation)

Поток может выделять ресурсы динамически — например, читать список файлов, полученный через сетевое соединение, 
и обрабатывать каждый из них. 
При этом библиотека гарантирует корректное освобождение всех захваченных ресурсов при завершении (как штатном, так и аварийном).

### 3. Недетерминированная и конкурентная обработка (Nondeterministic & concurrent processing)

Поток может одновременно читать из нескольких входных каналов и обрабатывать результаты по мере их поступления (какой вернулся первым). 
Благодаря встроенным комбинаторам конкурентности и структурам данных, 
потоки могут выступать в роли лёгких декларативных потоков выполнения (нитей), 
позволяя строить сложное конкурентное поведение композиционно и без явного управления низкоуровневой синхронизацией.

## FS2

FS2 — это не просто библиотека для потокового ввода-вывода, 
а универсальный, декларативный и композиционный инструмент для описания произвольных потоков управления, 
обработки данных и конкурентных вычислений в функциональном стиле. 
Она идеально вписывается в [экосистему Typelevel](https://typelevel.org/projects/) и является стандартным выбором для многих проектов, 
требующих высокой производительности, безопасности и выразительности при работе с асинхронными и потоковыми данными.

## Экосистема FS2

Библиотеки, использующие FS2, и интеграция с хранилищами данных.

### Базы данных и хранилища (Database Clients & Storage)

Библиотеки для работы с базами данных, key-value хранилищами и файловыми системами:

- [doobie](https://github.com/tpolecat/doobie): Чисто функциональный JDBC-клиент, построенный на основе fs2.
- [ldbc](https://github.com/takapi327/ldbc): Чисто функциональный JDBC-слой с Cats Effect 3 и Scala 3.
- [mongo4cats](https://github.com/Kirill5k/mongo4cats): Обёртка клиента Mongo DB для Cats Effect и FS2.
- [neotypes](https://github.com/neotypes/neotypes): Лёгкий, типобезопасный и асинхронный драйвер для neo4j на Scala. Поддерживает потоковые запросы с использованием fs2.
- [redis4cats](https://redis4cats.profunktor.dev/): Redis-клиент, построенный поверх fs2 и cats-effect.
- [fs2-blobstore](https://github.com/fs2-blobstore/fs2-blobstore): Минималистичный, идиоматичный, основанный на потоках интерфейс Scala для S3, GCS, SFTP и других реализаций хранилищ ключ-значение.

### Очереди и сообщения (Message Queues & Messaging)

Библиотеки для работы с брокерами сообщений и системами очередей:

- [fs2-rabbit](https://fs2-rabbit.profunktor.dev/): Клиент для RabbitMQ на основе потоков, построенный поверх fs2 и cats-effect.
- [fs2-google-pubsub](https://github.com/permutive/fs2-google-pubsub): Реализация [Google Cloud Pub/Sub](https://cloud.google.com/pubsub/) с использованием fs2 и cats-effect.
- [kafka4s](https://github.com/Banno/kafka4s): Функциональное программирование с Kafka и Scala.
- [fd4s/fs2-kafka](https://github.com/fd4s/fs2-kafka): Функциональные Kafka-потоки для Scala.

### Сетевые протоколы и коммуникации (Networking & Protocols)

Библиотеки для работы с различными сетевыми протоколами:

- [http4s](http://http4s.org/): Минималистичный, идиоматичный интерфейс Scala для HTTP-сервисов с использованием fs2.
- [fs2-grpc](https://github.com/typelevel/fs2-grpc): Реализация gRPC для FS2 / Cats Effect.
- [fs2-ftp](https://github.com/regis-leray/fs2-ftp): Простой клиент для Ftp/Ftps/Sftp с использованием fs2 и cats-effect.
- [fs2-ssh](https://github.com/slamdata/fs2-ssh): Обёртка вокруг Apache SSHD, ориентированная на cats-effect и fs2.

### Облачные провайдеры (Cloud Providers)

Библиотеки для интеграции с облачными сервисами:

- [fs2-aws](https://github.com/saksdirect/fs2-aws): Потоки FS2 для взаимодействия с сервисами AWS.


### Обработка данных и форматов (Data Processing & Formats)

Библиотеки для парсинга, сериализации и обработки различных форматов данных:

- [fs2-data](https://github.com/satabin/fs2-data): Набор парсеров данных и библиотек для манипуляции различными форматами для fs2.
- [spata](https://github.com/fingo/spata): CSV-парсер, построенный на FS2.
- [mysql-binlog-stream](https://github.com/laserdisc-io/mysql-binlog-stream): Потоковая передача событий бинарного лога (binlog) MySQL с помощью FS2.

### Управление временем и планирование (Scheduling & Time)

Библиотеки для работы со временем, расписаниями и ограничением скорости:

- [fs2-cron](https://github.com/fthomas/fs2-cron): Потоки FS2 на основе cron-выражений.
- [fs2-throttler](https://github.com/kovstas/fs2-throttler): Механизм ограничения пропускной способности (throttling) для fs2, основанный на алгоритме «token bucket».
- [upperbound](https://github.com/SystemFw/upperbound): Чисто функциональный ограничитель скорости на основе интервалов с поддержкой обратного давления (backpressure).

### Безопасность и шифрование (Security & Encryption)

- [fs2-aes](https://github.com/jwojnowski/fs2-aes): Шифрование AES для fs2.

### Системные процессы (System Processes)

- [prox](https://vigoo.github.io/prox/): Библиотека для работы с системными процессами.

### Специализированные и предметно-ориентированные (Domain-Specific)

Библиотеки для решения специфических задач:

- [vinyldns](https://github.com/vinyldns/vinyldns): Система управления DNS, использующая fs2 для ограничения скорости обновлений в DNS-бэкендах.

---

**Ссылки:**

- [Fs2 home page][home]
- [Aquascape project](https://zainab-ali.github.io/aquascape/reference/)
- [A sky full of streams by Jakub Kozłowski](https://www.youtube.com/watch?v=oluPEFlXumw)
- [A streaming library with a superpower: FS2 and functional programming](https://medium.freecodecamp.org/a-streaming-library-with-a-superpower-fs2-and-functional-programming-6f602079f70a)
- [An introduction to FS2 Streams by Michael Pilquist](https://www.youtube.com/watch?v=B1wb4fIdtn4)
- [Basic streams and combinators in fs2 by Jakub Kozłowski](https://www.youtube.com/watch?v=TmhIabcu6Fs)
- [Build a Stream to fetch everything behind an offset paged API by David Francoeur](https://davidfrancoeur.com/post/paged-api-fs2/)
- [Compose your program flow with Stream by Fabio Labella](https://www.youtube.com/watch?v=x3GLwl1FxcA)
- [Compositional Streaming with FS2 by Michael Pilquist](https://www.youtube.com/watch?v=oFk8-a1FSP0)
- [Declarative Control Flow with fs2 Stream by Fabio Labella](https://www.youtube.com/watch?v=YSN__0VEsaw)
- [FS2 - Crash course by Łukasz Byczyński](https://www.youtube.com/watch?v=tbShO8eIXbI)
- [FS2 Internals: Performance by Michael Pilquist](https://www.youtube.com/watch?v=TXxzMF14pxU)
- [How slow is your tram? by Michał Pawlik](https://www.youtube.com/watch?v=E-gnY0weqsk)
- [Inference Driven Design by Michael Pilquist](https://mpilquist.github.io/blog/2018/07/04/fs2/)
- [Introduction to FS2: Functional Streams for Scala](https://www.baeldung.com/scala/fs2-functional-streams)
- [Leverage FS2 API to write to a file by David Francoeur](https://davidfrancoeur.com/post/file-writing-fs2/)
- [My fs2 (was scalaz-stream) User Notes](https://devon-miller.gitbook.io/test_private_book)
- [No leftovers: Working with pulls in fs2 by Zainab Ali](https://blog.kebab-ca.se/chapters/fs2/overview.html)
- [Scala FS2 — handle broken CSV lines by Alexey Novakov](https://medium.com/se-notes-by-alexey-novakov/scala-fs2-handle-broken-csv-lines-8d8c4defcd88)
- [Sharding a stream of values using Fs2 by Gabriel Volpe](https://www.youtube.com/watch?v=FWYXqYQWAc0)
- [Tips for working with FS2 by Pere Villega](https://underscore.io/blog/posts/2018/03/20/fs2.html)
- [Writing a simple Telegram bot with tagless final, http4s and fs2 by Vladimir Pavkin](https://pavkin.ru/writing-a-simple-telegram-bot-with-tagless-final-http4s-and-fs2/)

[home]: https://fs2.io/#/
