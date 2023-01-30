def power(a: Long, n: Long): BigInt =
  @scala.annotation.tailrec
  def loop(base: BigInt, power: Long, acc: BigInt): BigInt =
    if power == 0 then acc
    else if power % 2 == 0 then loop(base * base, power / 2, acc)
    else loop(base, power - 1, base * acc)
  loop(a, n, 1)

power(2, 10)