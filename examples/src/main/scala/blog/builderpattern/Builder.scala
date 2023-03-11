package blog.builderpattern

import blog.builderpattern.ConnectionConfig.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

object Builder:
  @main def run(): Unit =
    val cfg0 = ConnectionConfig
      .builder()
      .build()
    println(s"Config = $cfg0")

    val cfg = ConnectionConfig
      .builder()
      .withHost("127.0.0.1")
      .withPort(8081)
      .withTimeout(20000)
      .withConnectionRetry(5)
      .withUser("user")
      .withPassword("password")
      .build()
    println(s"Config = $cfg")
