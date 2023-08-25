package ru.scalabook.books.tlpis

sealed trait Bool
sealed trait True  extends Bool
sealed trait False extends Bool

object Bool:
  type &&[A <: Bool, B <: Bool] <: Bool =
    A match
      case False => False
      case _     => B
  type ||[A <: Bool, B <: Bool] <: Bool =
    A match
      case True => True
      case _    => B
  type Not[A <: Bool] <: Bool =
    A match
      case True  => False
      case False => True

  class BoolRep[B <: Bool](val value: Boolean)
  given BoolRep[False] = new BoolRep(false)
  given BoolRep[True]  = new BoolRep(true)

  def toBoolean[B <: Bool: BoolRep]: Boolean =
    summon[BoolRep[B]].value

@main def runBool(): Unit =
  import Bool.*

  type Rep[A <: Bool] =
    A match
      case True  => Int
      case False => Long

  summon[Rep[True] =:= Int]
  summon[Rep[False] =:= Long]

  summon[((True && False) || Not[False]) =:= True]

  summon[(True && True) =:= True]
  summon[(True && False) =:= False]
  summon[(False && True) =:= False]
  summon[(False && False) =:= False]

  summon[(True || True) =:= True]
  summon[(True || False) =:= True]
  summon[(False || True) =:= True]
  summon[(False || False) =:= False]

  summon[Not[True] =:= False]
  summon[Not[False] =:= True]

  require(toBoolean[(True && False) || Not[False]])
