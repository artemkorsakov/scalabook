def pascalsTriangle(n: Int, k: Int): Int =
  if n == 0 || k == n || k == 0 then 1
  else pascalsTriangle(n - 1, k - 1) + pascalsTriangle(n - 1, k)

pascalsTriangle(4, 2)
