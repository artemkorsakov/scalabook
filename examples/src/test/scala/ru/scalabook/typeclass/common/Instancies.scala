package ru.scalabook.typeclass.common

import ru.scalabook.typeclass.monoid.Monoid

object Instancies:
  given Monoid[String] with
    val empty                                 = ""
    def combine(x: String, y: String): String = x + y

  given [T]: Monoid[Vector[T]] with
    val empty                                          = Vector.empty[T]
    def combine(x: Vector[T], y: Vector[T]): Vector[T] = x ++ y
