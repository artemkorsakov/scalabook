def expmod(base: Long, exp: Long, m: Long): Long =
  if exp == 0 then 1
  else if exp % 2 == 0 then
    val a = expmod(base, exp / 2, m)
    a * a % m
  else
    val a = base * expmod(base, exp - 1, m)
    a % m

def fermatTest(n: Long): Boolean =
  (2L until n).forall(a => expmod(a, n, n) == a)

fermatTest(561)
fermatTest(1105)
fermatTest(1729)
fermatTest(2465)
fermatTest(2821)
fermatTest(6601)
