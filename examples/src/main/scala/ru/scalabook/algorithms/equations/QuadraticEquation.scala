package ru.scalabook.algorithms.equations

import spire.math.Complex

/** a*x<sup>2</sup> + b*x + c = 0;
  *
  * @see
  *   <a href="https://en.wikipedia.org/wiki/Quadratic_equation">detailed
  *   description</a>
  */
final case class QuadraticEquation(a: Double, b: Double, c: Double):
  private val d = b * b - 4 * a * c

  /** x = (-b +/- &#8730;(b<sup>2</sup> - 4ac) )/2a
    */
  def solutionsInIntegers: Array[Long] =
    if d < 0 || !d.isWhole || !math.sqrt(d).isWhole then Array.empty[Long]
    else
      val sqrt = math.round(math.sqrt(d))
      val den  = 2 * a
      if sqrt == 0 then
        if b % den == 0 then Array((-b / den).toLong) else Array.empty[Long]
      else
        val x1 = -b + sqrt
        val x2 = -b - sqrt
        (x1 % den == 0, x2 % den == 0) match
          case (true, true)   => Array((x1 / den).toLong, (x2 / den).toLong)
          case (true, false)  => Array((x1 / den).toLong)
          case (false, true)  => Array((x2 / den).toLong)
          case (false, false) => Array.empty[Long]

  /** x = (-b +/- &#8730;(b<sup>2</sup> - 4ac) )/2a
    */
  def solutions: Array[Double] =
    if d < 0 then Array.empty[Double]
    else
      val sqrt = math.sqrt(d)
      val den  = 2 * a
      if sqrt == 0 then Array(-b / den)
      else Array((-b + sqrt) / den, (-b - sqrt) / den)

  /** x = -b/2a +/- (&#8730;(4ac - b<sup>2</sup>)/2a)i
    */
  def solutionsInComplexNumbers: Array[Complex[Double]] =
    if d >= 0 then solutions.map(Complex(_, 0.0))
    else
      val sqrt = math.sqrt(-d)
      val den  = 2 * a
      Array(
        Complex(-b / den, sqrt / den),
        Complex(-b / den, -sqrt / den)
      )
end QuadraticEquation
