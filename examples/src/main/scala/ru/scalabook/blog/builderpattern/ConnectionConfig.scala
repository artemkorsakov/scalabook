package ru.scalabook.blog.builderpattern

import cats.data.*
import cats.syntax.all.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.*
import io.github.iltotore.iron.constraint.all.*
import ru.scalabook.blog.builderpattern.ConnectionConfig.*

final case class ConnectionConfig private (
    host: Host,
    port: Port,
    user: User,
    password: Password
)

object ConnectionConfig:
  opaque type HostRule = DescribedAs[MinLength[4], "Invalid host"]
  opaque type Host     = String :| HostRule
  opaque type PortRule =
    DescribedAs[GreaterEqual[1024] & LessEqual[65535], "Invalid port"]
  opaque type Port = Int :| PortRule
  opaque type UserRule =
    DescribedAs[Alphanumeric & MinLength[1], "Invalid user"]
  opaque type User = String :| UserRule
  opaque type PasswordRule =
    DescribedAs[Alphanumeric & MinLength[8] & MaxLength[16], "Invalid password"]
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
