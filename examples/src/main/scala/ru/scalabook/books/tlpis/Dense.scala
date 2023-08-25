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

  type Add[A <: Dense, B <: Dense] <: Dense =
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

  type Mult[A <: Dense, B <: Dense] <: Dense =
    A match
      case DNil             => DNil
      case DCons[One, DNil] => B
      case _ =>
        B match
          case DNil => DNil
          case _    => Add[Mult[Dec[A], B], B]

  type Exp[A <: Dense, B <: Dense] <: Dense =
    B match
      case DNil => _1
      case _    => Mult[A, Exp[A, Dec[B]]]

  type Sq[N <: Dense] = Exp[N, _2]

  type Compare[A <: Dense, B <: Dense] <: Comparison =
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

  type isPalindrome[D <: Dense] <: Bool =
    Reverse[D] Compare D match
      case EQ => True
      case _  => False

  type isGreaterThan[D <: Dense, Max <: Dense] <: Bool =
    D Compare Max match
      case GT => True
      case _  => False

  type isLargerPalindrome[D <: Dense, Max <: Dense] =
    isPalindrome[D] && (D isGreaterThan Max)

  def toInt[D <: Dense](using drep: DRep[D]): Int = drep.value

  final case class DRep[D <: Dense](value: Int)
  given DRep[DNil] = DRep[DNil](0)
  given dcons0ToRep[D <: Dense](using tailRep: DRep[D]): DRep[DCons[Zero, D]] =
    DRep(tailRep.value * 2)
  given dcons1ToRep[D <: Dense](using tailRep: DRep[D]): DRep[DCons[One, D]] =
    DRep(tailRep.value * 2 + 1)

end Dense

@main def runDense(): Unit =
  import Dense.*

  summon[Inc[_0] =:= _1]
  summon[Inc[_1] =:= _2]
  summon[Inc[_2] =:= _3]
  summon[Inc[_3] =:= _4]
  summon[Inc[_4] =:= _5]

  summon[Dec[_0] =:= Nothing]
  summon[Dec[_1] =:= _0]
  summon[Dec[_2] =:= _1]
  summon[Dec[_3] =:= _2]
  summon[Dec[_4] =:= _3]
  summon[Dec[_5] =:= _4]
  summon[Dec[_6] =:= _5]
  summon[Dec[_7] =:= _6]
  summon[Dec[_8] =:= _7]
  summon[Dec[_9] =:= _8]
  summon[Dec[_10] =:= _9]

  summon[_0#digit =:= Nothing]
  summon[_1#digit =:= One]
  summon[_2#digit =:= Zero]
  summon[_3#digit =:= One]
  summon[_4#digit =:= Zero]
  summon[_5#digit =:= One]

  summon[_0#tail =:= Nothing]
  summon[_1#tail =:= _0]
  summon[_2#tail =:= _1]
  summon[_3#tail =:= _1]
  summon[_4#tail =:= _2]
  summon[_5#tail =:= _2]

  summon[_0#ShiftR =:= DNil]
  summon[_1#ShiftR =:= _0]
  summon[_2#ShiftR =:= _1]
  summon[_3#ShiftR =:= _1]
  summon[_4#ShiftR =:= _2]
  summon[_5#ShiftR =:= _2]

  summon[_0#ShiftL =:= DNil]
  summon[_1#ShiftL =:= _2]
  summon[_2#ShiftL =:= _4]
  summon[_3#ShiftL =:= _6]
  summon[_4#ShiftL =:= _8]

  summon[(_0 Add _7) =:= _7]
  summon[(_7 Add _0) =:= _7]
  summon[(_1 Add _7) =:= _8]
  summon[(_7 Add _7) =:= _14]
  summon[(_4 Add _8) =:= _12]

  summon[(_0 Mult _7) =:= _0]
  summon[(_7 Mult _0) =:= _0]
  summon[(_1 Mult _7) =:= _7]
  summon[(_7 Mult _1) =:= _7]
  summon[(_4 Mult _2) =:= _8]

  // toInt
  require(toInt[_4] == 4)
  require(toInt[_14] == 14)
  require(toInt[Inc[_5]] == 6)
  require(toInt[_5 Add _7] == 12)
  require(toInt[_12 Mult _8 Mult _13] == 1248)
  require(toInt[_4 Exp _15] == 1073741824)

  // Compare
  summon[(_0 Compare _0) =:= EQ]
  summon[(_0 Compare _1) =:= LT]
  summon[(_1 Compare _0) =:= GT]
  summon[(_0 Compare _2) =:= LT]
  summon[(_2 Compare _0) =:= GT]
  summon[(_1 Compare _1) =:= EQ]
  summon[(_1 Compare _2) =:= LT]
  summon[(_2 Compare _1) =:= GT]
  summon[(_3 Compare _3) =:= EQ]
  summon[(_3 Compare _5) =:= LT]
  summon[(_5 Compare _3) =:= GT]

  // Reverse
  summon[Reverse[_0] =:= _0]
  summon[Reverse[_1] =:= _1]
  summon[Reverse[_2] =:= (One :: Zero :: DNil)]
  summon[Reverse[_3] =:= _3]
  summon[Reverse[_4] =:= (One :: Zero :: Zero :: DNil)]
  summon[Reverse[_5] =:= _5]
  summon[Reverse[_6] =:= (One :: One :: Zero :: DNil)]
  summon[Reverse[_7] =:= _7]
  summon[Reverse[_8] =:= (One :: Zero :: Zero :: Zero :: DNil)]
  summon[Reverse[_9] =:= _9]
  summon[Reverse[_10] =:= (One :: Zero :: One :: Zero :: DNil)]

  // isPalindrome
  require(toBoolean[isPalindrome[_3]])
  require(!toBoolean[isPalindrome[_6]])
  require(toBoolean[isPalindrome[_15]])

  // isLargerPalindrome
  require(toBoolean[isLargerPalindrome[_7, _3]])
  require(!toBoolean[isLargerPalindrome[_3, _5]])
  require(!toBoolean[isLargerPalindrome[_8, _5]])
