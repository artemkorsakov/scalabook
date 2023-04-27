package ru.scalabook.typeclass.common

final case class IdT[M[_], A](run: M[Id[A]])
