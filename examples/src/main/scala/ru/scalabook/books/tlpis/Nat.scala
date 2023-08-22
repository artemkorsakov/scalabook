package ru.scalabook.books.tlpis

sealed trait Nat
sealed trait _0             extends Nat
sealed trait Succ[N <: Nat] extends Nat

type _1  = Succ[_0]
type _2  = Succ[_1]
type _3  = Succ[_2]
type _4  = Succ[_3]
type _5  = Succ[_4]
type _6  = Succ[_5]
type _7  = Succ[_6]
type _8  = Succ[_7]
type _9  = Succ[_8]
type _10 = Succ[_9]

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

type Add[A <: Nat, B <: Nat] <: Nat =
  A match
    case _0      => B
    case Succ[a] => Add[a, Succ[B]]

type Mult[A <: Nat, B <: Nat] <: Nat =
  A match
    case _0 => _0
    case _1 => B
    case Succ[a] =>
      B match
        case _0 => _0
        case _  => Add[Mult[a, B], B]

type Fact[A <: Nat] <: Nat =
  A match
    case _0      => _1
    case Succ[a] => Mult[A, Fact[a]]

type Exp[A <: Nat, B <: Nat] <: Nat =
  B match
    case _0      => _1
    case Succ[b] => Mult[A, Exp[A, b]]

type Mod[A <: Nat, B <: Succ[?]] <: Nat =
  A Compare B match
    case LT => A
    case EQ => _0
    case GT =>
      A match
        case Succ[a] => Mod[Add[Mod[a, B], _1], B]

@main def runNat(): Unit =
  import Bool.*

  require(toBoolean[Is0[_0]])
  require(!toBoolean[Is0[_2]])

  // Compare
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

  // Add
  summon[Add[_0, _3] =:= _3]
  summon[Add[_3, _0] =:= _3]
  summon[Add[_3, _2] =:= _5]

  // Mult
  summon[Mult[_0, _3] =:= _0]
  summon[Mult[_3, _0] =:= _0]
  summon[Mult[_3, _2] =:= _6]

  // Fact
  summon[Fact[_3] =:= _6]

  // Exp
  summon[Exp[_3, _0] =:= _1]
  summon[Exp[_3, _1] =:= _3]
  summon[Exp[_3, _2] =:= _9]

  // Mod
  summon[Mod[_5, _1] =:= _0]
  summon[Mod[_5, _2] =:= _1]
  summon[Mod[_5, _3] =:= _2]
  summon[Mod[_5, _4] =:= _1]
  summon[Mod[_5, _5] =:= _0]
  summon[Mod[_5, _6] =:= _5]
