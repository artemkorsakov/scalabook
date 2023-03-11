package blog.builderpattern

import blog.builderpattern.ConnectionConfig.*
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
