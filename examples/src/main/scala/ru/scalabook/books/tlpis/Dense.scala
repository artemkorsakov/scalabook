package ru.scalabook.books.tlpis

sealed trait Dense:
  type digit <: Digit
  type tail <: Dense
  type ShiftR <: Dense
  type ShiftL <: Dense

type ::[H <: Digit, T <: Dense] = DCons[H, T]

type Inc[T <: Dense] <: Dense =
  T match
    case DNil => One :: DNil
    case DCons[h, t] =>
      h match
        case Zero => One :: t
        case One  => Zero :: Inc[t]

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

type _0 = DNil
type _1 = One :: DNil
type _2 = Zero :: One :: DNil
type _3 = One :: One :: DNil
type _4 = Zero :: Zero :: One :: DNil
type _5 = One :: Zero :: One :: DNil
type _6 = Zero :: One :: One :: DNil
type _7 = One :: One :: One :: DNil
type _8 = Zero :: Zero :: Zero :: One :: DNil

@main def runDense(): Unit =
  summon[Inc[_0] =:= _1]
  summon[Inc[_1] =:= _2]
  summon[Inc[_2] =:= _3]
  summon[Inc[_3] =:= _4]
  summon[Inc[_4] =:= _5]

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
