package blog.builderpattern

import blog.builderpattern.ConnectionConfig.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

final case class ConnectionConfig private (
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
