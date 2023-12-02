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
end Bool
