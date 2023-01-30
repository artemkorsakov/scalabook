def cube(x: Double): Double = x * x * x

def p(x: Double): Double =
  println(s"Run p for x = '$x'")
  3 * x - 4 * cube(x)

def sine(angle: Double): Double =
  println(s"Run sine for angle = '$angle'")
  if math.abs(angle) <= 0.1 then angle
  else p(sine(angle / 3.0))

sine(12.15)
