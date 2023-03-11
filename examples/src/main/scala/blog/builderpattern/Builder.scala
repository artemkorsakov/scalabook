package blog.builderpattern

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

object Builder:
  @main def run(): Unit =
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

final case class ConnectionConfig private (
    host: String,
    port: Int,
    timeout: Int,
    connectionRetry: Int,
    user: String,
    password: String
)

object ConnectionConfig {
  opaque type Host = String :| MinLength[4]
  opaque type Port = Int :| GreaterEqual[1024] & LessEqual[65535]
  opaque type User = String :| Alphanumeric
  opaque type Password = String :| Match["\\S{8, 16}"]
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
