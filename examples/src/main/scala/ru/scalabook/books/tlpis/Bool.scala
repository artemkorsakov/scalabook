package ru.scalabook.books.tlpis

sealed trait Bool:
  type If[T <: Up, F <: Up, Up] <: Up
  type And[B <: Bool] = If[B, False, Bool]
  type Or[B <: Bool]  = If[True, B, Bool]
  type Not            = If[False, True, Bool]

object Bool:
  class BoolRep[B <: Bool](val value: Boolean)
  given BoolRep[False] = new BoolRep(false)
  given BoolRep[True]  = new BoolRep(true)

  def toBoolean[B <: Bool: BoolRep]: Boolean =
    summon[BoolRep[B]].value

sealed trait True extends Bool:
  type If[T <: Up, F <: Up, Up] = T
sealed trait False extends Bool:
  type If[T <: Up, F <: Up, Up] = F

@main def runBool(): Unit =
  summon[True#If[Int, Long, AnyVal] =:= Int]
  summon[False#If[Int, Long, AnyVal] =:= Long]
  
  summon[True#And[True] =:= True]
  summon[True#And[False] =:= False]
  summon[False#And[True] =:= False]
  summon[False#And[False] =:= False]

  summon[True#Or[True] =:= True]
  summon[True#Or[False] =:= True]
  summon[False#Or[True] =:= True]
  summon[False#Or[False] =:= False]

  summon[True#Not =:= False]
  summon[False#Not =:= True]

  println(Bool.toBoolean[True#And[False]#Or[False#Not]])
