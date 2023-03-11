# Шаблон Строитель в Scala 3

#### Назначение

Отделение построения сложного объекта от его представления,
чтобы один и тот же процесс построения мог создавать разные представления.

#### Диаграмма

![Builder](https://upload.wikimedia.org/wikipedia/ru/2/28/Builder.gif)

#### Пример

Особенно `Builder` хорош, когда нужно провести валидацию параметров перед получением итогового экземпляра
с [использованием уточняющих типов](https://scalabook.gitflic.space/docs/libs/refined).

```scala
final case class ConnectionConfig private (
    host: String,
    port: Int,
    timeout: Int,
    connectionRetry: Int,
    user: String,
    password: String
)

object ConnectionConfig {
  def builder(): ConnectionConfigBuilder = ConnectionConfigBuilder()

  case class ConnectionConfigBuilder private (
      private val host: String,
      private val port: Int,
      private val timeout: Int,
      private val connectionRetry: Int,
      private val user: String,
      private val password: String
  ) {
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

    def build(): ConnectionConfig = {
      new ConnectionConfig(
        host = host,
        port = port,
        timeout = timeout,
        connectionRetry = connectionRetry,
        user = user,
        password = password
      )
    }
  }

  private object ConnectionConfigBuilder {
    def apply(): ConnectionConfigBuilder =
      new ConnectionConfigBuilder(
        host = "localhost",
        port = 8080,
        timeout = 10000,
        connectionRetry = 3,
        user = "root",
        password = "root"
      )
  }
}
```

Использование паттерна строитель:

```scala
val cfg = ConnectionConfig
  .builder()
  .withHost("localhost")
  .withPort(9090)
  .withTimeout(1000)
  .withConnectionRetry(1)
  .withUser("user")
  .withPassword("12345")
  .build()
println(s"Config = $cfg")
// Config = ConnectionConfig(localhost,9090,1000,1,user,12345)
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Builder_pattern)
