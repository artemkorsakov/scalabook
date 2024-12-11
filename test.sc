val ones: LazyList[Int] = LazyList.cons(1, ones)
ones.take(5).toList
// res2: List[Int] = List(1, 1, 1, 1, 1)
ones.exists(_ % 2 != 0)
// res3: Boolean = true
