package ru.scalabook.fp.trampolining

import ru.scalabook.fp.trampolining.Trampoline.given

import scala.language.implicitConversions

object Hello:
  val hello: Trampoline[Unit] =
    for
      _ <- print(" Hello , ")
      _ <- println(" World !")
    yield ()

@main def runHello(): Unit =
  import Hello.hello
  (hello zip hello).runT
