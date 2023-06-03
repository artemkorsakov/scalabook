package ru.scalabook.libs.zio.hangman

final case class State private (name: Name, guesses: Set[Guess], word: Word):
  lazy val failuresCount: Int  = (guesses.map(_.char) -- word.toSet).size
  lazy val playerLost: Boolean = failuresCount > 5
  lazy val playerWon: Boolean  = (word.toSet -- guesses.map(_.char)).isEmpty

  def addGuess(guess: Guess): State =
    State(name, guesses + guess, word)

object State:
  def initial(name: Name, word: Word): State =
    State(name, Set.empty, word)
