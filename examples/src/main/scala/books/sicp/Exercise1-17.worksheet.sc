def double(n: Long): Long =
  n << 1

def halve(n: Long): Long =
  n >> 1

double(4)
halve(4)

def fastMul(a: Long, b: Long): Long =
  if b == 0 then 0L
  else if b % 2 == 0 then double(fastMul(a, halve(b)))
  else a + fastMul(a, b - 1)

fastMul(22, 10)
