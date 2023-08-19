package ru.scalabook.books.tlpis

sealed trait Nat:
  type FoldR[Init <: Type, Type, F <: Fold[Nat, Type]] <: Type

sealed trait _0             extends Nat
sealed trait Succ[N <: Nat] extends Nat

type Is0[A <: Nat] =
  A match
    case _0 => True
    case _  => False

type Compare[A <: Nat, B <: Nat] <: Comparison =
  A match
    case _0 =>
      B match
        case _0 => EQ
        case _  => LT
    case Succ[a] =>
      B match
        case _0      => GT
        case Succ[b] => Compare[a, b]

@main def runNat(): Unit =
  type _1 = Succ[_0]
  type _2 = Succ[_1]
  type _3 = Succ[_2]
  type _4 = Succ[_3]
  type _5 = Succ[_4]
  
  import Bool.*

  require(toBoolean[Is0[_0]])
  require(!toBoolean[Is0[_2]])

  require(toBoolean[(_0 Compare _0)#eq])
  require(!toBoolean[(_0 Compare _0)#lt])
  require(toBoolean[(_0 Compare _1)#lt])
  require(toBoolean[(_1 Compare _0)#gt])
  require(toBoolean[(_0 Compare _2)#lt])
  require(toBoolean[(_2 Compare _0)#gt])
  require(toBoolean[(_1 Compare _1)#eq])
  require(toBoolean[(_1 Compare _2)#lt])
  require(toBoolean[(_2 Compare _1)#gt])
  require(toBoolean[(_3 Compare _3)#eq])
  require(toBoolean[(_3 Compare _5)#lt])
  require(toBoolean[(_5 Compare _3)#gt])
