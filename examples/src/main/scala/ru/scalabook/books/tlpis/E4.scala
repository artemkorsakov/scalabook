package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.Bool.*
import ru.scalabook.books.tlpis.Dense.*

object E4:
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

  trait Pali:
    type Result = (Dense, Dense, Dense)

    type Apply[
        max <: Dense,
        iMax <: Dense,
        jMax <: Dense,
        i <: Dense,
        j <: Dense,
        P <: Pali
    ]

  trait Pali0 extends Pali:
    // Точка входа
    type App[start <: Dense, P <: Pali] =
      P match
        case _ => Apply[_1, _1, _1, start, start, P]

    // Рекурсия
    type Apply[
        max <: Dense,
        iMax <: Dense,
        jMax <: Dense,
        i <: Dense,
        j <: Dense,
        P <: Pali
    ] =
      P match
        case _ => Apply1[max, iMax, jMax, i, j, Mult[j, i], P]

    type Apply1[
        max <: Dense,
        iMax <: Dense,
        jMax <: Dense,
        i <: Dense,
        j <: Dense,
        prod <: Dense,
        P <: Pali
    ] =
      isLargerPalindrome[prod, max] match
        case True  => Apply2[prod, i, j, i, Dec[j], P]
        case False => Apply2[max, iMax, jMax, i, Dec[j], P]

    type Apply2[
        max <: Dense,
        iMax <: Dense,
        jMax <: Dense,
        i <: Dense,
        j <: Dense,
        P <: Pali
    ] =
      j match
        case _0 => Apply3[max, iMax, jMax, Dec[i], Dec[i], P]
        case _  => Apply3[max, iMax, jMax, i, j, P]

    type Apply3[
        max <: Dense,
        iMax <: Dense,
        jMax <: Dense,
        i <: Dense,
        j <: Dense,
        P <: Pali
    ] =
      i match
        case _0 => (max, iMax, jMax)
        case _  => Pali0#Apply[max, iMax, jMax, i, j, P]

  end Pali0

  object Pali1 extends Pali0:
    summon[App[_7, Pali1.type] =:= (_21, _7, _3)]
    summon[App[_15, Pali1.type] =:= (Mult[_15, _13], _15, _13)]

end E4

@main def runE4(): Unit =
  import E4.*

  // isPalindrome
  require(toBoolean[isPalindrome[_3]])
  require(!toBoolean[isPalindrome[_6]])
  require(toBoolean[isPalindrome[_15]])

  // isLargerPalindrome
  require(toBoolean[isLargerPalindrome[_7, _3]])
  require(!toBoolean[isLargerPalindrome[_3, _5]])
  require(!toBoolean[isLargerPalindrome[_8, _5]])

/*
  println( toTuple3[Pali0#App[_7, Pali0]] )
  // (21,7,3)

  // already pretty slow at _15
  println( toTuple3[Pali1.App[_15, Pali1.type]] )
  // (195,15,13)
 */
