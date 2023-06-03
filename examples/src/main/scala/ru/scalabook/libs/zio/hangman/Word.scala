package ru.scalabook.libs.zio.hangman

final case class Word private (word: String):
  val length: Int                   = word.length
  val toList: List[Char]            = word.toList
  val toSet: Set[Char]              = word.toSet
  def contains(char: Char): Boolean = word.contains(char)

object Word:
  def make(word: String): Option[Word] =
    if word.nonEmpty && word.forall(_.isLetter) then
      Some(Word(word.toLowerCase))
    else None
