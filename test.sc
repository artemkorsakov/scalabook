@annotation.tailrec
final def gcd(a: Long, b: Long): Long =
  if b == 0 then a else gcd(b, a % b)
  
println(gcd(4851, 3003))