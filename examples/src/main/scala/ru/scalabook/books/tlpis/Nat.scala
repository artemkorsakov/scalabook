package ru.scalabook.books.tlpis

sealed trait Nat
sealed trait _0             extends Nat
sealed trait Succ[N <: Nat] extends Nat

object Nat:
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

  infix type Compare[A <: Nat, B <: Nat] <: Comparison =
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
      case _0      => Succ[_0]
      case Succ[a] => Mult[A, Fact[a]]

  type Exp[A <: Nat, B <: Nat] <: Nat =
    B match
      case _0      => Succ[_0]
      case Succ[b] => Mult[A, Exp[A, b]]

  type Mod[A <: Nat, B <: Succ[?]] <: Nat =
    A Compare B match
      case LT => A
      case EQ => _0
      case GT =>
        A match
          case Succ[a] => Mod[Add[Mod[a, B], Succ[_0]], B]

  type Sq[N <: Nat] = Exp[N, Succ[Succ[_0]]]

  type Eq[A <: Nat, B <: Nat] <: Bool =
    Compare[A, B] match
      case EQ => True
      case _  => False

end Nat
