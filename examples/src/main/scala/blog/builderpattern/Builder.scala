package blog.builderpattern

import cats.data.*
import blog.builderpattern.ConnectionConfig.*

object Builder:
  @main def run(): Unit =
    val invalidConfig = ConnectionConfig
      .builder()
      .withHost("")
      .withPort(-1)
      .withUser("")
      .withPassword("")
      .build()
    printConfig(invalidConfig)

    val validConfig = ConnectionConfig
      .builder()
      .withHost("127.0.0.1")
      .withPort(8081)
      .withUser("user")
      .withPassword("password")
      .build()
    printConfig(validConfig)

  private def printConfig(
      config: ValidatedNel[String, ConnectionConfig]
  ): Unit =
    config match
      case Validated.Valid(cfg) => println(s"Valid config:\n$cfg")
      case Validated.Invalid(errors) =>
        println("Invalid config:")
        errors.iterator.foreach(println)
        println("")
