package ru.scalabook.libs.zio.hangman

enum GuessResult:
  case Won, Lost, Correct, Incorrect, Unchanged
