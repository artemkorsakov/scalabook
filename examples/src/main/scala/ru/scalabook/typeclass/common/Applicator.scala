package ru.scalabook.typeclass.common

trait Applicator[F[_]]:
  def run[A](a: A): F[A] => Boolean
