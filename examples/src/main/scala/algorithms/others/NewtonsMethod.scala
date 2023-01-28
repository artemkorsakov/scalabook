package algorithms.others

object NewtonsMethod:
  private val accuracy = 1e-6
  private val dx       = 1e-6

  @scala.annotation.tailrec
  def newtonsMethod(g: Double => Double, guess: Double): Double =
    val next = transform(g)(guess)
    if math.abs(next - guess) < accuracy then next
    else newtonsMethod(g, next)

  def derivative(x: Double, g: Double => Double): Double =
    (g(x + dx) - g(x)) / dx

  private def transform(g: Double => Double): Double => Double =
    x => x - g(x) / derivative(x, g)
