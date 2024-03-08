package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.Bool.*

sealed trait Dense:
  type digit <: Digit
  type tail <: Dense
  type ShiftR <: Dense
  type ShiftL <: Dense

type ::[H <: Digit, T <: Dense] = DCons[H, T]

sealed trait DNil extends Dense:
  type tail   = Nothing
  type digit  = Nothing
  type ShiftR = DNil
  type ShiftL = DNil

sealed trait DCons[d <: Digit, T <: Dense] extends Dense:
  type digit  = d
  type tail   = T
  type ShiftR = tail
  type ShiftL = Zero :: DCons[d, T]

object Dense:
  type _0  = DNil
  type _1  = One :: DNil
  type _2  = Zero :: One :: DNil
  type _3  = One :: One :: DNil
  type _4  = Zero :: Zero :: One :: DNil
  type _5  = One :: Zero :: One :: DNil
  type _6  = Zero :: One :: One :: DNil
  type _7  = One :: One :: One :: DNil
  type _8  = Zero :: Zero :: Zero :: One :: DNil
  type _9  = One :: Zero :: Zero :: One :: DNil
  type _10 = Zero :: One :: Zero :: One :: DNil
  type _11 = One :: One :: Zero :: One :: DNil
  type _12 = Zero :: Zero :: One :: One :: DNil
  type _13 = One :: Zero :: One :: One :: DNil
  type _14 = Zero :: One :: One :: One :: DNil
  type _15 = One :: One :: One :: One :: DNil
  type _16 = Zero :: Zero :: Zero :: Zero :: One :: DNil
  type _17 = One :: Zero :: Zero :: Zero :: One :: DNil
  type _18 = Zero :: One :: Zero :: Zero :: One :: DNil
  type _19 = One :: One :: Zero :: Zero :: One :: DNil
  type _20 = Zero :: Zero :: One :: Zero :: One :: DNil
  type _21 = One :: Zero :: One :: Zero :: One :: DNil

  type Inc[T <: Dense] <: Dense =
    T match
      case DNil           => One :: DNil
      case DCons[Zero, t] => One :: t
      case DCons[One, t]  => Zero :: Inc[t]

  type Dec[T <: Dense] <: Dense =
    T match
      case DNil             => Nothing
      case DCons[Zero, t]   => One :: Dec[t]
      case DCons[One, DNil] => DNil
      case DCons[One, t]    => Zero :: t

  infix type Add[A <: Dense, B <: Dense] <: Dense =
    A match
      case DNil => B
      case DCons[Zero, ta] =>
        B match
          case DNil          => A
          case DCons[hb, tb] => hb :: Add[ta, tb]
      case DCons[One, ta] =>
        B match
          case DNil            => A
          case DCons[Zero, tb] => One :: Add[ta, tb]
          case DCons[One, tb]  => Zero :: Inc[Add[ta, tb]]

  infix type Mult[A <: Dense, B <: Dense] <: Dense =
    A match
      case DNil             => DNil
      case DCons[One, DNil] => B
      case _ =>
        B match
          case DNil => DNil
          case _    => Add[Mult[Dec[A], B], B]

  infix type Exp[A <: Dense, B <: Dense] <: Dense =
    B match
      case DNil => _1
      case _    => Mult[A, Exp[A, Dec[B]]]

  type Sq[N <: Dense] = Exp[N, _2]

  infix type Compare[A <: Dense, B <: Dense] <: Comparison =
    A match
      case DNil =>
        B match
          case DNil => EQ
          case _    => LT
      case DCons[ha, ta] =>
        B match
          case DNil => GT
          case DCons[hb, tb] =>
            Compare[ta, tb] match
              case EQ => Digit.Compare[ha, hb]
              case LT => LT
              case GT => GT

  type AddToTheEnd[D <: Digit, T <: Dense] <: Dense =
    T match
      case DNil        => D :: DNil
      case DCons[h, t] => h :: AddToTheEnd[D, t]

  type Reverse[A <: Dense] <: Dense =
    A match
      case DNil        => DNil
      case DCons[h, t] => AddToTheEnd[h, Reverse[t]]

  def toInt[D <: Dense](using drep: DRep[D]): Int = drep.value

  final case class DRep[D <: Dense](value: Int)
  given DRep[DNil] = DRep[DNil](0)
  given dcons0ToRep[D <: Dense](using tailRep: DRep[D]): DRep[DCons[Zero, D]] =
    DRep(tailRep.value * 2)
  given dcons1ToRep[D <: Dense](using tailRep: DRep[D]): DRep[DCons[One, D]] =
    DRep(tailRep.value * 2 + 1)

end Dense
