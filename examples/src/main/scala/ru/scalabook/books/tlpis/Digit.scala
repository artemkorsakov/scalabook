package ru.scalabook.books.tlpis

sealed trait Digit
sealed trait Zero extends Digit
sealed trait One  extends Digit

object Digit:
  type Is0[A <: Digit] =
    A match
      case Zero => True
      case One  => False
  
  type Compare[A <: Digit, B <: Digit] <: Comparison =
    A match
      case Zero =>
        B match
          case Zero => EQ
          case One  => LT
      case One =>
        B match
          case Zero => GT
          case One  => EQ

@main def runDigit(): Unit =
  import Digit.*
  
  summon[Is0[Zero] =:= True]
  summon[Is0[One] =:= False]
  summon[(Zero Compare Zero) =:= EQ]
  summon[(Zero Compare One) =:= LT]
  summon[(One Compare Zero) =:= GT]
  summon[(One Compare One) =:= EQ]
