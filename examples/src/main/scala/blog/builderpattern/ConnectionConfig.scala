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
    user: User,
    password: Password
)

object ConnectionConfig:
  opaque type HostRule = MinLength[4] DescribedAs "Invalid host"
  opaque type Host     = String :| HostRule
  opaque type PortRule = GreaterEqual[1024] & LessEqual[65535] DescribedAs
    "Invalid port"
  opaque type Port     = Int :| PortRule
  opaque type UserRule = Alphanumeric & MinLength[1] DescribedAs "Invalid user"
  opaque type User     = String :| UserRule
  opaque type PasswordRule =
    Alphanumeric & MinLength[8] & MaxLength[16] DescribedAs "Invalid password"
  opaque type Password = String :| PasswordRule

  def builder(): ConnectionConfigBuilder = ConnectionConfigBuilder()

  final case class ConnectionConfigBuilder private (
      private val validatedHost: ValidatedNel[String, Host],
      private val validatedPort: ValidatedNel[String, Port],
      private val validatedUser: ValidatedNel[String, User],
      private val validatedPassword: ValidatedNel[String, Password]
  ):
    def withHost(host: String): ConnectionConfigBuilder =
      copy(validatedHost = host.refineValidatedNel[HostRule])

    def withPort(port: Int): ConnectionConfigBuilder =
      copy(validatedPort = port.refineValidatedNel[PortRule])

    def withUser(user: String): ConnectionConfigBuilder =
      copy(validatedUser = user.refineValidatedNel[UserRule])

    def withPassword(password: String): ConnectionConfigBuilder =
      copy(validatedPassword = password.refineValidatedNel[PasswordRule])

    def build(): ValidatedNel[String, ConnectionConfig] =
      (
        validatedHost,
        validatedPort,
        validatedUser,
        validatedPassword
      ).mapN(ConnectionConfig.apply)
  end ConnectionConfigBuilder

  private object ConnectionConfigBuilder:
    def apply(): ConnectionConfigBuilder =
      new ConnectionConfigBuilder(
        validatedHost = Validated.Valid("localhost"),
        validatedPort = Validated.Valid(8080),
        validatedUser = Validated.Valid("root"),
        validatedPassword = Validated.Valid("password")
      )
  end ConnectionConfigBuilder
end ConnectionConfig
