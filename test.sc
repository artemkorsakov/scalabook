sealed trait ComplexNumber:
  self =>

  def realPart: Double
  def imagPart: Double
  def magnitude: Double
  def angle: Double
  def makeFromRealImag(realPart: Double, imagPart: Double): ComplexNumber
  def makeFromMagAng(magnitude: Double, angle: Double): ComplexNumber

  def addComplex(z: ComplexNumber): ComplexNumber =
    makeFromRealImag(
      realPart = self.realPart + z.realPart,
      imagPart = self.imagPart + z.imagPart
    )

  def subComplex(z: ComplexNumber): ComplexNumber =
    makeFromRealImag(
      realPart = self.realPart - z.realPart,
      imagPart = self.imagPart - z.imagPart
    )

  def mulComplex(z: ComplexNumber): ComplexNumber =
    makeFromMagAng(
      magnitude = self.magnitude * z.magnitude,
      angle = self.angle + z.angle
    )

  def divComplex(z: ComplexNumber): ComplexNumber =
    makeFromMagAng(
      magnitude = self.magnitude / z.magnitude,
      angle = self.angle - z.angle
    )
end ComplexNumber

final case class CartesianComplexNumber(
    realPart: Double,
    imagPart: Double
) extends ComplexNumber:
  val magnitude: Double = math.sqrt(realPart * realPart + imagPart * imagPart)
  val angle: Double     = math.atan2(imagPart, realPart)

  def makeFromRealImag(
      realPart: Double,
      imagPart: Double
  ): CartesianComplexNumber =
    CartesianComplexNumber(realPart = realPart, imagPart = imagPart)

  def makeFromMagAng(magnitude: Double, angle: Double): CartesianComplexNumber =
    CartesianComplexNumber(
      realPart = magnitude * math.cos(angle),
      imagPart = magnitude * math.sin(angle)
    )

final case class PolarComplexNumber(
    magnitude: Double,
    angle: Double
) extends ComplexNumber:
  val realPart: Double = magnitude * math.cos(angle)
  val imagPart: Double = magnitude * math.sin(angle)

  def makeFromRealImag(realPart: Double, imagPart: Double): PolarComplexNumber =
    PolarComplexNumber(
      magnitude = math.sqrt(realPart * realPart + imagPart * imagPart),
      angle = math.atan2(imagPart, realPart)
    )

  def makeFromMagAng(magnitude: Double, angle: Double): PolarComplexNumber =
    PolarComplexNumber(magnitude = magnitude, angle = angle)
