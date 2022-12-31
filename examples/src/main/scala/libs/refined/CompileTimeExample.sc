import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.auto.*
import eu.timepit.refined.collection.*
import eu.timepit.refined.string.*

import scala.compiletime.error
import scala.language.implicitConversions

type NonEmptyString = String Refined NonEmpty

inline def refine(inline str: String): NonEmptyString =
  inline str match
    case null: Null | "" => error("String must be non Empty")
    case str             => refineV[NonEmpty].unsafeFrom(str)

refine("Алёна")
refine("")
refine(null: String)

println("-" * 100)

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]
object Name extends RefinedTypeOps[Name, String]

inline def refineName(inline str: String): Name =
  inline str match
    case null: Null | "" => error("String must be non Empty")
    case str             => Name.unsafeFrom(str)

refineName(null)
refine("")
refineName("€‡™µ")
refineName("12345")
refineName("Alyona")
refineName("Алёна18")
refineName("алёна")
refineName("Алёна")
