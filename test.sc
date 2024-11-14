@annotation.tailrec
def isSnoob(x: Int, y: Int): Boolean = 
  if x == 0 && y == 0 then true
  else if x == 0 || y == 0 then false
  else if x % 2 == 1 && y % 2 == 0 then isSnoob(x, y >> 1)
  else if x % 2 == 0 && y % 2 == 1 then isSnoob(x >> 1, y)
  else isSnoob(x >> 1, y >> 1)

println(isSnoob(11, 13))
