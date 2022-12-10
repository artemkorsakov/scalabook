import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.auto.*
import eu.timepit.refined.boolean.*
import eu.timepit.refined.char.*
import eu.timepit.refined.collection.*
import eu.timepit.refined.generic.*
import eu.timepit.refined.numeric.*
import eu.timepit.refined.string.*
import shapeless.{::, HNil}

refineV[Contains["a"]](Vector("a", "b", "c", "d"))
refineV[Contains["e"]](Vector("a", "b", "c", "d"))

// - `Count[PA, PC]` - подсчитывает количество элементов в Iterable, удовлетворяющих предикату PA, и передает результат предикату PC.

refineV[Empty](Vector())
refineV[Empty](Vector("a"))
refineV[Empty](Vector("a", "b", "c", "d"))

refineV[NonEmpty](Vector())
refineV[NonEmpty](Vector("a"))
refineV[NonEmpty](Vector("a", "b", "c", "d"))

refineV[Forall[Even]](Vector(2, 4, 6, 8))
refineV[Forall[Even]](Vector(2, 4, 6, 7))
refineV[Forall[Even]](Vector(1, 3, 5, 7))

refineV[Exists[Even]](Vector(2, 4, 6, 8))
refineV[Exists[Even]](Vector(2, 4, 6, 7))
refineV[Exists[Even]](Vector(1, 3, 5, 7))

refineV[Head[Even]](Vector(2, 4, 6, 8))
refineV[Head[Even]](Vector(2, 4, 6, 7))
refineV[Head[Even]](Vector(1, 3, 5, 7))

// refineV[Index[1, Even]](Array(2, 4, 6, 8))
// refineV[Index[1, Even]](Array(2, 4, 6, 7))
// refineV[Index[1, Even]](Array(1, 3, 5, 7))

refineV[Init[Even]](Vector(2, 4, 6, 8))
refineV[Init[Even]](Vector(2, 4, 6, 7))
refineV[Init[Even]](Vector(1, 3, 5, 7))

refineV[Last[Even]](Vector(2, 4, 6, 8))
refineV[Last[Even]](Vector(2, 4, 6, 7))
refineV[Last[Even]](Vector(1, 3, 5, 7))

refineV[Tail[Even]](Vector(2, 4, 6, 8))
refineV[Tail[Even]](Vector(2, 4, 6, 7))
refineV[Tail[Even]](Vector(1, 3, 5, 7))

refineV[Size[Even]](Vector(2, 4, 6, 7))
refineV[Size[Even]](Vector(2, 4, 6))

refineV[MinSize[4]](Vector(2, 4, 6, 7))
refineV[MinSize[4]](Vector(2, 4, 6))

refineV[MaxSize[3]](Vector(2, 4, 6, 7))
refineV[MaxSize[3]](Vector(2, 4, 6))
