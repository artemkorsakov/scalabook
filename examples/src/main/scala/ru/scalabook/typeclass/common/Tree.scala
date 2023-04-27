package ru.scalabook.typeclass.common

final case class Tree[+A](head: A, tail: List[Tree[A]])
