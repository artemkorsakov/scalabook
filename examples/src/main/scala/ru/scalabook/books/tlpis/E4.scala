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
