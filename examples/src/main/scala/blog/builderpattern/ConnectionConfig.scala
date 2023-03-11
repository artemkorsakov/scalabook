package blog.builderpattern

import _root_.cats.data.*
import _root_.cats.syntax.all.*
import blog.builderpattern.ConnectionConfig.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.{*, given}
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
  opaque type HostRule     = MinLength[4]
  opaque type Host         = String :| HostRule
  opaque type PortRule     = GreaterEqual[1024] & LessEqual[65535]
  opaque type Port         = Int :| PortRule
  opaque type TimeoutRule  = Positive
  opaque type Timeout      = Int :| TimeoutRule
  opaque type RetryRule    = GreaterEqual[1] & LessEqual[10]
  opaque type Retry        = Int :| RetryRule
  opaque type UserRule     = Alphanumeric
  opaque type User         = String :| UserRule
  opaque type PasswordRule = Alphanumeric & MinLength[8] & MaxLength[16]
  opaque type Password     = String :| PasswordRule

  def builder(): ConnectionConfigBuilder = ConnectionConfigBuilder()

  final case class ConnectionConfigBuilder private (
      private val validatedHost: ValidatedNel[String, Host],
      private val validatedPort: ValidatedNel[String, Port],
      private val validatedTimeout: ValidatedNel[String, Timeout],
      private val validatedConnectionRetry: ValidatedNel[String, Retry],
      private val validatedUser: ValidatedNel[String, User],
      private val validatedPassword: ValidatedNel[String, Password]
  ):
    import ConnectionConfigBuilder.*

    def withHost(host: String): ConnectionConfigBuilder =
      copy(validatedHost = host.refineValidatedNel[HostRule])

    def withPort(port: Int): ConnectionConfigBuilder =
      copy(validatedPort = port.refineValidatedNel[PortRule])

    def withTimeout(timeout: Int): ConnectionConfigBuilder =
      copy(validatedTimeout = timeout.refineValidatedNel[TimeoutRule])

    def withConnectionRetry(
        connectionRetry: Int
    ): ConnectionConfigBuilder =
      copy(validatedConnectionRetry =
        connectionRetry.refineValidatedNel[RetryRule]
      )

    def withUser(user: String): ConnectionConfigBuilder =
      copy(validatedUser = user.refineValidatedNel[UserRule])

    def withPassword(password: String): ConnectionConfigBuilder =
      copy(validatedPassword = password.refineValidatedNel[PasswordRule])

    def build(): ConnectionConfig =
      new ConnectionConfig(
        host = "localhost",
        port = 8080,
        timeout = 10000,
        connectionRetry = 3,
        user = "root",
        password = "password"
      )
  end ConnectionConfigBuilder

  private object ConnectionConfigBuilder:
    def apply(): ConnectionConfigBuilder =
      new ConnectionConfigBuilder(
        validatedHost = Validated.Valid("localhost"),
        validatedPort = Validated.Valid(8080),
        validatedTimeout = Validated.Valid(10000),
        validatedConnectionRetry = Validated.Valid(3),
        validatedUser = Validated.Valid("root"),
        validatedPassword = Validated.Valid("password")
      )
  end ConnectionConfigBuilder
end ConnectionConfig
