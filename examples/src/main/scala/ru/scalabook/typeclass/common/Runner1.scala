package ru.scalabook.typeclass.common

trait Runner1[F[_]]:
  def run[A]: F[A] => A

object Runner1:
  def apply[F[_]: Runner1]: Runner1[F] = summon[Runner1[F]]
