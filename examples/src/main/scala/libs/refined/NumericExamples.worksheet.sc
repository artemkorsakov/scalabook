import eu.timepit.refined.refineV
import eu.timepit.refined.numeric.*

refineV[Less[0.0]](0.8)
refineV[Less[0.0]](0.0)
refineV[Less[0.0]](-0.8)

refineV[LessEqual[0.0]](0.8)
refineV[LessEqual[0.0]](0.0)
refineV[LessEqual[0.0]](-0.8)

refineV[Greater[0.0]](0.8)
refineV[Greater[0.0]](0.0)
refineV[Greater[0.0]](-0.8)

refineV[GreaterEqual[0.0]](0.8)
refineV[GreaterEqual[0.0]](0.0)
refineV[GreaterEqual[0.0]](-0.8)

refineV[Positive](0.8)
refineV[Positive](0.0)
refineV[Positive](-0.8)

refineV[NonPositive](0.8)
refineV[NonPositive](0.0)
refineV[NonPositive](-0.8)

refineV[Negative](0.8)
refineV[Negative](0.0)
refineV[Negative](-0.8)

refineV[NonNegative](0.8)
refineV[NonNegative](0.0)
refineV[NonNegative](-0.8)

refineV[Interval.Open[1, 3]](0)
refineV[Interval.Open[1, 3]](1)
refineV[Interval.Open[1, 3]](2)
refineV[Interval.Open[1, 3]](3)
refineV[Interval.Open[1, 3]](4)

refineV[Interval.OpenClosed[1, 3]](0)
refineV[Interval.OpenClosed[1, 3]](1)
refineV[Interval.OpenClosed[1, 3]](2)
refineV[Interval.OpenClosed[1, 3]](3)
refineV[Interval.OpenClosed[1, 3]](4)

refineV[Interval.ClosedOpen[1, 3]](0)
refineV[Interval.ClosedOpen[1, 3]](1)
refineV[Interval.ClosedOpen[1, 3]](2)
refineV[Interval.ClosedOpen[1, 3]](3)
refineV[Interval.ClosedOpen[1, 3]](4)

refineV[Interval.Closed[1, 3]](0)
refineV[Interval.Closed[1, 3]](1)
refineV[Interval.Closed[1, 3]](2)
refineV[Interval.Closed[1, 3]](3)
refineV[Interval.Closed[1, 3]](4)

refineV[Modulo[3, 1]](7)
refineV[Modulo[3, 1]](6)

refineV[Divisible[3]](7)
refineV[Divisible[3]](6)

refineV[NonDivisible[3]](7)
refineV[NonDivisible[3]](6)

refineV[Even](7)
refineV[Even](6)

refineV[Odd](7)
refineV[Odd](6)

refineV[NonNaN](Double.NaN)
refineV[NonNaN](1.0)
