def double(n: Long): Long =
  n << 1

def halve(n: Long): Long =
  n >> 1

double(4)
halve(4)

def fastMul(a: Long, b: Long): Long =
  @scala.annotation.tailrec
  def loop(base: Long, times: Long, acc: Long): Long =
    if times == 0 then acc
    else if times % 2 == 0 then loop(double(base), halve(times), acc)
    else loop(base, times - 1, base + acc)
  loop(a, b, 0)

fastMul(22, 10)
