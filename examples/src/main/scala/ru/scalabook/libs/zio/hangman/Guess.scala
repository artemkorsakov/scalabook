package ru.scalabook.libs.zio.hangman

final case class Guess private (char: Char)

object Guess:
  def make(str: String): Option[Guess] =
    Some(str.toList).collect:
      case c :: Nil if c.isLetter => Guess(c.toLower)
