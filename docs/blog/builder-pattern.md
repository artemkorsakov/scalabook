# Шаблон Строитель в Scala 3

Как известно, шаблон [Строитель (Builder)][Wiki] отделяет конструирование сложного объекта от его представления,
что особенно хорошо, когда нужно провести валидацию параметров перед получением итогового экземпляра.
Особенно удобно комбинировать шаблон Строитель с [уточняющими типами][habr].

Рассмотрим использование Строителя на Scala версии `"3.2.2"`.

Представим, что у нас есть конфиг:

```scala
final case class ConnectionConfig (
    host: String,
    port: Int,
    timeout: Int,
    connectionRetry: Int,
    user: String,
    password: String
)
```

И мы хотим предоставить пользователю возможность создавать конфиг различными способами, но 
при этом валидировать значения перед формированием итогового конфига.
Например, по следующим правилам:

- `host` - строка от 4 символов
- `port` - число от 1024 до 65535
- `timeout` - неотрицательное число
- `connectionRetry` - от 1 до 10
- `user` - строка, содержащая только буквы и цифры
- `password` - строка без пробелов длиной от 8 до 16 символов

Весьма удобно использовать для этого уточняющие типы:

```scala
final case class ConnectionConfig(
    host: Host,
    port: Port,
    timeout: Timeout,
    connectionRetry: Retry,
    user: User,
    password: Password
)

object ConnectionConfig:
  opaque type Host     = String :| MinLength[4]
  opaque type Port     = Int :| GreaterEqual[1024] & LessEqual[65535]
  opaque type Timeout  = Int :| Positive
  opaque type Retry    = Int :| GreaterEqual[1024] & LessEqual[65535]
  opaque type User     = String :| Alphanumeric
  opaque type Password = String :| Match["\\S{8, 16}"]
```

У case class-а `ConnectionConfig` конструктор можно определить как приватный, 
чтобы ограничить создание конфига только через Строитель. 

Тогда сам шаблон Строитель можно определить вот так:

```scala

object ConnectionConfig:
  ...

  def builder(): ConnectionConfigBuilder = ConnectionConfigBuilder()

  final case class ConnectionConfigBuilder private (
      private val host: String,
      private val port: Int,
      private val timeout: Int,
      private val connectionRetry: Int,
      private val user: String,
      private val password: String
  ):
    import ConnectionConfigBuilder.*

    def withHost(host: String): ConnectionConfigBuilder =
      copy(host = host)

    def withPort(port: Int): ConnectionConfigBuilder =
      copy(port = port)

    def withTimeout(timeout: Int): ConnectionConfigBuilder =
      copy(timeout = timeout)

    def withConnectionRetry(
        connectionRetry: Int
    ): ConnectionConfigBuilder =
      copy(connectionRetry = connectionRetry)

    def withUser(user: String): ConnectionConfigBuilder =
      copy(user = user)

    def withPassword(password: String): ConnectionConfigBuilder =
      copy(password = password)

    def build(): ConnectionConfig =
      new ConnectionConfig(
        host = ???,
        port = ???,
        timeout = ???,
        connectionRetry = ???,
        user = ???,
        password = ???
      )
  end ConnectionConfigBuilder

  private object ConnectionConfigBuilder:
    def apply(): ConnectionConfigBuilder =
      new ConnectionConfigBuilder(
        host = "localhost",
        port = 8080,
        timeout = 10000,
        connectionRetry = 3,
        user = "root",
        password = "root"
      )
  end ConnectionConfigBuilder
end ConnectionConfig
```

Здесь есть несколько моментов, на которые стоит обратить внимание:

- Метод `builder()` создает сам конструктор объекта
- В конструкторе `ConnectionConfigBuilder` объявлены методы для добавления каждого параметра
- Метод `build()` отдает итоговый конфиг
- В сопутствующем объекте `ConnectionConfigBuilder` определен конфиг по умолчанию
- Сопутствующий объект приватный для того, чтобы доступ к приватному конструктору осуществлялся только через `builder()`
- У `ConnectionConfigBuilder` приватные параметры конструктора в первую очередь для того, 
  чтобы пользователь "видел" только методы установки значений `with...`
- Метод `copy` недоступен за пределами `case class ConnectionConfigBuilder` из-за приватного конструктора,
  что опять же позволяет задавать параметры только через `with...`

Тогда этим Строителем можно пользоваться вот так:

```scala
ConnectionConfig
  .builder()
  .withHost("localhost")
  .withPort(9090)
  .withTimeout(1000)
  .withConnectionRetry(1)
  .withUser("user")
  .withPassword("12345")
  .build()
```

Других способов создания `ConnectionConfig` нет.

### А как же валидация параметров?



---

**Ссылки:**

- [Wikipedia][Wiki]

[Wiki]: https://ru.wikipedia.org/wiki/%D0%A1%D1%82%D1%80%D0%BE%D0%B8%D1%82%D0%B5%D0%BB%D1%8C_(%D1%88%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD_%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F)
[habr]: https://habr.com/ru/company/kryptonite/blog/719488/
