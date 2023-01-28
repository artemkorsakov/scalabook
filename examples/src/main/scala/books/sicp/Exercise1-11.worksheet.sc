def recursion(n: Int): Int =
  if n < 3 then n
  else recursion(n - 1) + recursion(n - 2) + recursion(n - 3)

recursion(0)
recursion(1)
recursion(2)
recursion(3)
recursion(4)
recursion(5)
recursion(6)

def iteration(n: Int): Int =
  def loop(a: Int, b: Int, c: Int, count: Int): Int =
    if count == 0 then c
    else loop(a + b + c, a, b, count - 1)
  loop(2, 1, 0, n)

iteration(0)
iteration(1)
iteration(2)
iteration(3)
iteration(4)
iteration(5)
iteration(6)
