package algorithms.numbers

import algorithms.fundamental.Numerical.gcd

/** <a href="https://en.wikipedia.org/wiki/Rational_number">Rational number</a>
  * or <a
  * href="https://en.wikipedia.org/wiki/Fraction_(mathematics)">Fraction</a>
  */
case class RationalNumber(x: BigInt, y: BigInt):
  require(y != 0, "denominator can't be 0")

  private val (n, d): (BigInt, BigInt) =
    if x == BigInt(0) then (BigInt(0), BigInt(1))
    else if y < 0 then (-x, -y)
    else (x, y)

  private val g           = gcd(n, d)
  val numerator: BigInt   = n / g
  val denominator: BigInt = d / g

  def +(r: RationalNumber): RationalNumber = add(r)

  def +(r: BigInt): RationalNumber = add(RationalNumber(r))

  def add(r: RationalNumber): RationalNumber =
    RationalNumber(
      numerator * r.denominator + r.numerator * denominator,
      denominator * r.denominator
    )

  def -(r: RationalNumber): RationalNumber = sub(r)

  def -(r: BigInt): RationalNumber = sub(RationalNumber(r))

  def sub(r: RationalNumber): RationalNumber =
    RationalNumber(
      numerator * r.denominator - r.numerator * denominator,
      denominator * r.denominator
    )

  def *(r: RationalNumber): RationalNumber = mul(r)

  def *(r: BigInt): RationalNumber = mul(RationalNumber(r))

  def mul(r: RationalNumber): RationalNumber =
    RationalNumber(numerator * r.numerator, denominator * r.denominator)

  def /(r: RationalNumber): RationalNumber = div(r)

  def /(r: BigInt): RationalNumber = div(RationalNumber(r))

  def div(r: RationalNumber): RationalNumber =
    RationalNumber(numerator * r.denominator, r.numerator * denominator)

  def %(r: RationalNumber): RationalNumber = mod(r)

  def %(r: BigInt): RationalNumber = mod(RationalNumber(r))

  def mod(r: RationalNumber): RationalNumber =
    RationalNumber(
      (numerator * r.denominator) % (denominator * r.numerator),
      denominator * r.denominator
    )

  def upend: RationalNumber = RationalNumber(denominator, numerator)

  def ==(r: RationalNumber): Boolean = this.equal(r)

  def equal(r: RationalNumber): Boolean =
    (numerator * r.denominator).equals(denominator * r.numerator)

  override def equals(obj: Any): Boolean =
    obj match {
      case rn: RationalNumber => this.equal(rn)
      case _                  => false
    }

  def <(r: RationalNumber): Boolean =
    numerator * r.denominator < r.numerator * denominator

  def <=(r: RationalNumber): Boolean =
    numerator * r.denominator <= r.numerator * denominator

  def >(r: RationalNumber): Boolean =
    numerator * r.denominator > r.numerator * denominator

  def >=(r: RationalNumber): Boolean =
    numerator * r.denominator >= r.numerator * denominator

  def max(that: RationalNumber): RationalNumber =
    if this < that then that else this

  override def toString: String = s"$numerator/$denominator"

  /** Fraction to percent. */
  def toPercent: Double =
    (BigDecimal(numerator) / BigDecimal(denominator)).rounded.toDouble

end RationalNumber

object RationalNumber:
  trait RationalNumberIsIntegral extends Integral[RationalNumber]:
    def plus(x: RationalNumber, y: RationalNumber): RationalNumber  = x + y
    def minus(x: RationalNumber, y: RationalNumber): RationalNumber = x - y
    def times(x: RationalNumber, y: RationalNumber): RationalNumber = x * y
    def quot(x: RationalNumber, y: RationalNumber): RationalNumber  = x / y
    def rem(x: RationalNumber, y: RationalNumber): RationalNumber   = x % y
    def negate(x: RationalNumber): RationalNumber                   = RationalNumber(-x.x, x.y)
    def fromInt(x: Int): RationalNumber                             = RationalNumber(x)
    def parseString(str: String): Option[RationalNumber]            = None
    def toInt(x: RationalNumber): Int                               = (x.numerator./(x.denominator)).toInt
    def toLong(x: RationalNumber): Long                             = (x.numerator./(x.denominator)).toLong
    def toFloat(x: RationalNumber): Float                           =
      (x.numerator./(x.denominator)).toFloat
    def toDouble(x: RationalNumber): Double                         =
      (x.numerator./(x.denominator)).toDouble
    override def sign(x: RationalNumber): RationalNumber            = x

  implicit object RationalNumberIsIntegral
      extends RationalNumberIsIntegral,
        Ordering[RationalNumber]:
    override def compare(a: RationalNumber, b: RationalNumber): Int =
      if a < b then -1 else if a == b then 0 else 1

  def apply(numerator: BigInt): RationalNumber =
    RationalNumber(numerator, BigInt(1))

  def apply(x: Int, y: Int): RationalNumber =
    RationalNumber(BigInt(x), BigInt(y))

  def apply(x: Int): RationalNumber = RationalNumber(BigInt(x), BigInt(1))

  def apply(x: Long, y: Long): RationalNumber =
    RationalNumber(BigInt(x), BigInt(y))

  def apply(x: Long): RationalNumber = RationalNumber(BigInt(x), BigInt(1))

  def apply(r: RationalNumber): RationalNumber =
    RationalNumber(r.numerator, r.denominator)

  val ZeroRationalNumber: RationalNumber = RationalNumber(0, 1)

  val OneRationalNumber: RationalNumber = RationalNumber(1, 1)

  def max(r1: RationalNumber, r2: RationalNumber): RationalNumber =
    if r1 < r2 then r2 else r1

end RationalNumber
