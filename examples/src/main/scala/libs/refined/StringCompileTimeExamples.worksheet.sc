import eu.timepit.refined.refineV
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import scala.compiletime.error

type NonEmptyString = String Refined NonEmpty

inline def refineNonEmptyString(inline str: String): NonEmptyString =
  inline str match
    case null: Null | "" => error("String must be non empty")
    case _               => refineV[NonEmpty].unsafeFrom(str)

//refineNonEmptyString("")
//refineNonEmptyString(null: String)
refineNonEmptyString("Алёна")
