package ru.scalabook.books.tlpis

sealed trait Nat:
  type Match[NonZero[N <: Nat] <: Up, IfZero <: Up, Up] <: Up
  private type ConstFalse[_] = False
  type Is0                   = Match[ConstFalse, True, Bool]

sealed trait _0 extends Nat:
  type Match[NonZero[N <: Nat] <: Up, IfZero <: Up, Up] = IfZero
sealed trait Succ[N <: Nat] extends Nat:
  type Match[NonZero[N <: Nat] <: Up, IfZero <: Up, Up] = NonZero[N]

type _1 = Succ[_0]
type _2 = Succ[_1]

@main def runNat(): Unit =
  println(Bool.toBoolean[_0#Is0])
  println(Bool.toBoolean[_1#Is0])
  println(Bool.toBoolean[_2#Is0])
