package algorithms.equations

import algorithms.numbers.ComplexNumber

/** a*x<sup>2</sup> + b*x + c = 0;
  *
  * @see
  *   <a href="https://en.wikipedia.org/wiki/Quadratic_equation">detailed
  *   description</a>
  */
case class QuadraticEquation(a: Double, b: Double, c: Double):

  /** x = (-b +/- &#8730;(b<sup>2</sup> - 4ac) )/2a
    */
  def solutionsInIntegers: Seq[Long] =
    val d = b * b - 4 * a * c
    if d < 0 || !d.isWhole || !math.sqrt(d).isWhole then Seq.empty[Long]
    else
      val sqrt = math.round(math.sqrt(d))
      val den  = 2 * a
      if sqrt == 0 then
        if b % den == 0 then Seq((-b / den).toLong) else Seq.empty[Long]
      else
        ((-b + sqrt) % den == 0, (-b - sqrt) % den == 0) match
          case (true, true)   =>
            Seq(((-b + sqrt) / den).toLong, ((-b - sqrt) / den).toLong)
          case (true, false)  => Seq(((-b + sqrt) / den).toLong)
          case (false, true)  => Seq(((-b - sqrt) / den).toLong)
          case (false, false) => Seq.empty[Long]

  /** x = (-b +/- &#8730;(b<sup>2</sup> - 4ac) )/2a
    */
  def solutions: Seq[Double] =
    val d = b * b - 4 * a * c
    if d < 0 then Seq.empty[Double]
    else
      val sqrt = math.sqrt(d)
      val den  = 2 * a
      if sqrt == 0 then Seq(-b / den)
      else Seq((-b + sqrt) / den, (-b - sqrt) / den)

  /** x = -b/2a +/- (&#8730;(4ac - b<sup>2</sup>)/2a)i
    */
  def solutionsInComplexNumbers: Seq[ComplexNumber] =
    val d = b * b - 4 * a * c
    if d >= 0 then solutions.map(ComplexNumber(_, 0.0))
    else
      val sqrt = math.sqrt(-d)
      val den  = 2 * a
      Seq(
        ComplexNumber(-b / den, sqrt / den),
        ComplexNumber(-b / den, -sqrt / den)
      )
