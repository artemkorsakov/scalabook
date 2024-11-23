import scala.annotation.tailrec

def pell(i: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, i: Int): BigInt =
    if i == 0 then b
    else loop(b, 2 * b + a, i - 1)

  if i <= 1 then i
  else loop(0, 1, i - 1)

println(pell(100))

def tribonacci(i: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, c: BigInt, i: Int): BigInt =
    if i == 0 then c
    else loop(b, c, a + b + c, i - 1)

  if i <= 1 then 0
  else if i == 2 then 1
  else loop(0, 0, 1, i - 2)

println(tribonacci(100))

def tetranacci(i: Int): BigInt =
  @tailrec
  def loop(
      a: BigInt,
      b: BigInt,
      c: BigInt,
      d: BigInt,
      i: Int
  ): BigInt =
    if i == 0 then d
    else loop(b, c, d, a + b + c + d, i - 1)

  if i <= 2 then 0
  else if i == 3 then 1
  else loop(0, 0, 0, 1, i - 3)

println(tetranacci(100))  
