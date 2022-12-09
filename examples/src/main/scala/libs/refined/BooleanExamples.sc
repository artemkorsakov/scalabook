import eu.timepit.refined.*
import eu.timepit.refined.api.{RefType, Refined}
import eu.timepit.refined.auto.*
import eu.timepit.refined.boolean.*
import eu.timepit.refined.numeric.{Greater, Less}

refineV[Not[Less[0.0]] And Not[Greater[1.0]]](0.8)
refineV[Not[Less[0.0]] And Not[Greater[1.0]]](1.1)
refineV[Not[Less[0.0]] And Not[Greater[1.0]]](-0.1)

refineV[Less[0.0] Or Greater[1.0]](0.8)
refineV[Less[0.0] Or Greater[1.0]](1.1)
refineV[Less[0.0] Or Greater[1.0]](-0.1)
