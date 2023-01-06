import eu.timepit.refined.refineV
import eu.timepit.refined.boolean.*
import eu.timepit.refined.char.*
import eu.timepit.refined.numeric.*
import shapeless.{::, HNil}

refineV[True](0.8)

refineV[False](0.8)

refineV[Not[Less[0.0]] And Not[Greater[1.0]]](0.8)
refineV[Not[Less[0.0]] And Not[Greater[1.0]]](1.1)
refineV[Not[Less[0.0]] And Not[Greater[1.0]]](-0.1)

refineV[Less[0.0] Or Greater[1.0]](0.8)
refineV[Less[0.0] Or Greater[1.0]](1.1)
refineV[Less[0.0] Or Greater[1.0]](-0.1)

refineV[Less[1.0] Xor Greater[0.0]](0.8)
refineV[Less[1.0] Xor Greater[0.0]](1.1)
refineV[Less[1.0] Xor Greater[0.0]](-0.1)

refineV[Nand[Greater[0.0], Less[1.0]]](0.8)
refineV[Nand[Greater[0.0], Less[1.0]]](1.1)
refineV[Nand[Greater[0.0], Less[1.0]]](-0.1)

refineV[Nor[Less[0.0], Greater[1.0]]](0.8)
refineV[Nor[Less[0.0], Greater[1.0]]](1.1)
refineV[Nor[Less[0.0], Greater[1.0]]](-0.1)

refineV[AllOf[Greater[1] :: Divisible[3] :: Even :: HNil]](6)
refineV[AllOf[Greater[1] :: Divisible[3] :: Even :: HNil]](9)

refineV[AnyOf[Digit :: Letter :: Whitespace :: HNil]]('F')

// OneOf[PS] - исключительная дизъюнкция всех предикатов в PS
