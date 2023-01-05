import eu.timepit.refined.refineV
import eu.timepit.refined.generic.*

refineV[Equal[1]](1)
refineV[Equal[1]](2)

refineV[Equal["string"]]("string")
refineV[Equal["string"]]("   ")
