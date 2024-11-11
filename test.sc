def gcdBasedOnParityOfNumbers(a: Long, b: Long): Long =
  println(s"a = $a, b = $b")
  val u = math.abs(a)
  val v = math.abs(b)
  if u == v then u
  else if u == 0 then v
  else if v == 0 then u
  else
    (~u & 1, ~v & 1) match
      case (1, 1) => gcdBasedOnParityOfNumbers(u >> 1, v >> 1) << 1
      case (1, 0) => gcdBasedOnParityOfNumbers(u >> 1, v)
      case (0, 1) => gcdBasedOnParityOfNumbers(u, v >> 1)
      case (_, _) =>
        if u > v then gcdBasedOnParityOfNumbers((u - v) >> 1, v)
        else gcdBasedOnParityOfNumbers((v - u) >> 1, u)
  
gcdBasedOnParityOfNumbers(4851, 3003) 