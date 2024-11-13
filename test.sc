def power(a: Long, n: Long): Long =
  @annotation.tailrec
  def loop(base: Long, power: Long, acc: Long): Long =
    if power == 0 then acc
    else if power % 2 == 0 then loop(base * base, power / 2, acc)
    else loop(base, power - 1, base * acc)
  loop(a, n, 1)

println(power(2, 17))