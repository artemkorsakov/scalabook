package ru.scalabook.libs.zio.hangman

final case class Name private (name: String)

object Name:
  def make(name: String): Option[Name] =
    if name.nonEmpty then Some(Name(name)) else None
