# Шаблон Строитель в Scala 3

По определению шаблон [Строитель (Builder)][Wiki] отделяет конструирование сложного объекта от его представления,
что особенно хорошо, когда нужно провести валидацию параметров перед получением итогового экземпляра.
Особенно удобно комбинировать шаблон Строитель с [уточняющими типами][habr].

Рассмотрим использование Строителя на Scala версии `"3.2.2"`.

Представим, что у нас есть конфиг:

```scala
final case class ConnectionConfig (
    host: String,
    port: Int,
    user: String,
    password: String
)
```

И мы хотим предоставить пользователю возможность создавать конфиг различными способами, но 
при этом валидировать значения перед формированием итогового результата.
Например, по следующим правилам:

- `host` - строка от 4 символов
- `port` - число от 1024 до 65535
- `user` - непустая строка, содержащая только буквы и цифры
- `password` - строка, содержащая только буквы и цифры, длиной от 8 до 16 символов

Весьма удобно использовать для этого уточняющие типы:

```scala
final case class ConnectionConfig(
    host: Host,
    port: Port,
    user: User,
    password: Password
)

object ConnectionConfig:
  opaque type Host     = String :| MinLength[4]
  opaque type Port     = Int :| GreaterEqual[1024] & LessEqual[65535]
  opaque type User     = String :| Alphanumeric & MinLength[1]
  opaque type Password = String :| Alphanumeric & MinLength[8] & MaxLength[16]
```

У case class-а `ConnectionConfig` конструктор можно определить как приватный, 
чтобы ограничить создание конфига только по шаблону. 

Тогда сам шаблон Строитель можно определить вот так:

```scala

object ConnectionConfig:
  ...

  def builder(): ConnectionConfigBuilder = ConnectionConfigBuilder()

  final case class ConnectionConfigBuilder private (
      private val host: String,
      private val port: Int,
      private val user: String,
      private val password: String
  ):
    def withHost(host: String): ConnectionConfigBuilder =
      copy(host = host)

    def withPort(port: Int): ConnectionConfigBuilder =
      copy(port = port)

    def withUser(user: String): ConnectionConfigBuilder =
      copy(user = user)

    def withPassword(password: String): ConnectionConfigBuilder =
      copy(password = password)

    def build(): ConnectionConfig =
      new ConnectionConfig(
        host = ???,
        port = ???,
        user = ???,
        password = ???
      )
  end ConnectionConfigBuilder

  private object ConnectionConfigBuilder:
    def apply(): ConnectionConfigBuilder =
      new ConnectionConfigBuilder(
        host = "localhost",
        port = 8080,
        user = "root",
        password = "root"
      )
  end ConnectionConfigBuilder
end ConnectionConfig
```

Здесь есть несколько моментов, на которые стоит обратить внимание:

- В сопутствующем объекте `ConnectionConfigBuilder` определен конфиг по умолчанию
- Метод `builder()` создает конструктор из конфига по умолчанию
- Сопутствующий объект приватный для того, чтобы доступ к конфигу по умолчанию осуществлялся только через `builder()`
- В конструкторе `ConnectionConfigBuilder` объявлены методы `with...` для установки каждого параметра
- Метод `build()` отдает итоговый конфиг
- У `ConnectionConfigBuilder` приватные параметры конструктора в первую очередь для того, 
  чтобы пользователь "видел" только методы установки значений `with...`,
  а итоговое состояние конфига получал только через `build()`
- Метод `copy` недоступен за пределами `case class ConnectionConfigBuilder` из-за приватного конструктора,
  что опять же позволяет задавать параметры только через `with...`

Таким образом построить `ConnectionConfig` по шаблону можно так:

```scala
ConnectionConfig
  .builder()
  .withHost("localhost")
  .withPort(9090)
  .withUser("user")
  .withPassword("12345")
  .build()
```

Другие способы создания `ConnectionConfig` недоступны, как нет и других методов работы с `ConnectionConfigBuilder`.

### А как же валидация параметров?

Как уже упоминалось [в статье об уточняющих типах][habr] желательно сохранять все ошибки валидации,
а затем либо выдавать корректный результат, либо - список ошибок.
Поэтому пойдем по тому же пути, что и в указанной статье.

Из типа `Host` выделим тип, описывающий уточняющие правила 
и, если необходимо, переопределим сообщение об ошибке:

```scala
opaque type HostRule = MinLength[4] DescribedAs "Invalid host"
opaque type Host     = String :| HostRule
```

В конструкторе `ConnectionConfigBuilder` заменим тип параметра `host` на `ValidatedNel[String, Host]` 
и переименуем его в `validatedHost`.
Тогда метод установки значения можно заменить на:

```scala
def withHost(host: String): ConnectionConfigBuilder =
  copy(validatedHost = host.refineValidatedNel[HostRule])
```

Проделаем точно такие же изменения для остальных параметров.

Builder примет следующий вид:

```scala
final case class ConnectionConfigBuilder private (
    private val validatedHost: ValidatedNel[String, Host],
    private val validatedPort: ValidatedNel[String, Port],
    private val validatedUser: ValidatedNel[String, User],
    private val validatedPassword: ValidatedNel[String, Password]
)
```

Конфиг по умолчанию станет равным:

```scala
def apply(): ConnectionConfigBuilder =
  new ConnectionConfigBuilder(
    validatedHost = Validated.Valid("localhost"),
    validatedPort = Validated.Valid(8080),
    validatedUser = Validated.Valid("root"),
    validatedPassword = Validated.Valid("password")
  )
```

При этом в конфиге по умолчанию также можно указать и невалидные значения, 
если для заданного параметра значение по умолчанию отсутствует и требуется его установка пользователем.

Например:

```scala
validatedPassword = Validated.Invalid(NonEmptyList.one("Invalid password"))
```

Или:

```scala
validatedPassword = "".refineValidatedNel[PasswordRule]
```

Остается только определить метод `build()`:

```scala
def build(): ValidatedNel[String, ConnectionConfig] =
  (
    validatedHost,
    validatedPort,
    validatedUser,
    validatedPassword
  ).mapN(ConnectionConfig.apply)
```

В результате использования паттерна Строитель будет выведены либо список всех ошибок:

```scala
val invalidConfig = ConnectionConfig
  .builder()
  .withHost("")
  .withPort(-1)
  .withUser("")
  .withPassword("")
  .build()

// Invalid(NonEmptyList(Invalid host, Invalid port, Invalid user, Invalid password))
```

Либо корректный конфиг:

```scala
val validConfig = ConnectionConfig
  .builder()
  .withHost("127.0.0.1")
  .withPort(8081)
  .withUser("user")
  .withPassword("password")
  .build()

// Valid(ConnectionConfig(127.0.0.1,8081,user,password))
```

[Полный пример доступен на Scastie](https://scastie.scala-lang.org/zalqhllqTu23ngxmD3nl4w)


[Wiki]: https://ru.wikipedia.org/wiki/%D0%A1%D1%82%D1%80%D0%BE%D0%B8%D1%82%D0%B5%D0%BB%D1%8C_(%D1%88%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD_%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F)
[habr]: https://habr.com/ru/company/kryptonite/blog/719488/
