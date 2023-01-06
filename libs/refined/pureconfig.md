# Пример взаимодействия **refined** с **pureconfig**

Рассмотрим взаимодействие библиотеки **refined** с [**pureconfig**](https://pureconfig.github.io/). 
На данный момент библиотека [собрана только под Scala 2](https://index.scala-lang.org/fthomas/refined/artifacts/refined-pureconfig?pre-releases=false)
, поэтому будем рассматривать код на этой версии Scala.

Предположим, что у нас есть некий конфиг с настройками подключения к БД:

```text
{
    host: "example.com"
    port: 8080
}
```

Довольно часто в современных проектах используется микросервисная архитектура 
и конфиги разрастаются до огромных размеров, усложняющих их поддержку и нахождения в них ошибок и опечаток.

Что если разработчик указал пустой хост 
(допустим для того, чтобы указать его позже, когда станет известна конфигурация на стенде) 
и опечатался в `port`?!

```text
{
    host: ""
    port: 808
}
```

Даже такая конфигурация будет успешно прочитана:

```scala
import pureconfig._
import pureconfig.generic.auto._

object Main {
  private case class Config(host: String, port: Int)
  
  def main(args: Array[String]): Unit = {
    parseConfig("""{
              host: ""
              port: 808
          }""")
  }
  
  private def parseConfig(source: String): Unit = {
    println("---")
    ConfigSource.string(source).load[Config] match {
      case Right(config) =>
        println(s"Configuration loaded successfully:\n$config")
      case Left(error) =>
        println(s"Error loading configuration:\n$error")
    }
    println("---")
  }
}
```

В этом случае будет выдано сообщение о том, что конфиг задан верно:

```text
---
Configuration loaded successfully:
Config(,808)
---
```

[Разобранный пример на Scastie](https://scastie.scala-lang.org/vq79scDfRyOA4GoDh1MFnw)

Безусловно, ошибка была бы найдена при развертывании на стенде, например, при получении ошибки подключения.
Но сколько времени ушло бы на поиск опечатки в хосте или порту?

Здесь нам очень помогли бы уточняющие типы, более детально описывающие, какая конфигурация требуется заданному стенду.
Уточняющие типы могли бы исключить использование `localhost` на боевом стенде, да и многое другое.

Рассмотрим самый простой пример, уточняющий хост до непустой строки и порт - как число, большее `1024`:

```scala
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Greater
import eu.timepit.refined.pureconfig._
import pureconfig._
import pureconfig.generic.auto._

object Main {
  private type NonEmptyString = String Refined NonEmpty
  private type ServerPort = Int Refined Greater[1024]

  private case class Config(host: NonEmptyString, port: ServerPort)
  
  def main(args: Array[String]): Unit = {
    parseConfig("""{
              host: ""
              port: 808
          }""")
    parseConfig("""{
              host: "example.com"
              port: 8080
          }""")
  }

  private def parseConfig(source: String): Unit = {
    println("---")
    ConfigSource.string(source).load[Config] match {
      case Right(config) =>
        println(s"Configuration loaded successfully:\n$config")
      case Left(error) =>
        println(s"Error loading configuration:\n$error")
    }
    println("---")
  }
}
```

В этом случае успешно распарсился бы только второй конфиг, а в логах было бы выведено следующее:

```text
---
Error loading configuration:
ConfigReaderFailures(
  ... Predicate isEmpty() did not fail.) ... host),
  ... Predicate failed: (808 > 1024).) ... port)
)
---
---
Configuration loaded successfully:
Config(example.com,8080)
---
```

[Разобранный пример на Scastie](https://scastie.scala-lang.org/Lr7fwKLJT7GNZ7TApANjgg)

В лице уточняющих типов мы можем более четко формулировать, какие значения считаются валидными в конфигах
и отсеивать явные опечатки.
