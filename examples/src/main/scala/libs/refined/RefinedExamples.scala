package libs.refined

import eu.timepit.refined.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto.*
import eu.timepit.refined.numeric.*

@main def main(): Unit =
  val x = 42
  println(refineV[Positive](x))
  println(refineV[Positive](-x))
