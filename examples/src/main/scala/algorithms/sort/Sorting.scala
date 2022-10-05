package algorithms.sort

object Sorting:
  def bubbleSort[T: Ordering](array: Array[T]): Unit =
    val ord = summon[Ordering[T]]
    def loop(j: Int): Boolean =
      (0 to array.length - 1 - j)
        .withFilter(i => ord.gt(array(i), array(i + 1)))
        .map(i => swap(array, i, i + 1))
        .nonEmpty
    (1 until array.length).takeWhile(loop)
    ()

  def selectionSort[T: Ordering](array: Array[T]): Unit =
    (0 until array.length - 1).foreach { i =>
      val j = (i until array.length).minBy(j => array(j))
      swap(array, i, j)
    }

  def insertionSort(array: Array[Int]): Unit =
    for (j <- 1 until array.length) do
      val key = array(j)
      var i = j - 1
      while i >= 0 && array(i) > key
      do
        array(i + 1) = array(i)
        i -= 1
      array(i + 1) = key

  def insertionSortReverse(array: Array[Int]): Unit =
    for (j <- 1 until array.length) do
      val key = array(j)
      var i = j - 1
      while i >= 0 && array(i) < key
      do
        array(i + 1) = array(i)
        i -= 1
      array(i + 1) = key

  def mergeSort(array: Array[Int]): Unit =
    mergeSort(array, 0, array.length - 1)

  private def mergeSort(array: Array[Int], first: Int, last: Int): Unit =
    if last > first then
      val mid = first + (last - first) / 2
      mergeSort(array, first, mid)
      mergeSort(array, mid + 1, last)

      val buf = new Array[Int](array.length)
      array.copyToArray(buf)
      if last + 1 - first >= 0 then System.arraycopy(array, first, buf, first, last + 1 - first)

      var i = first
      var j = mid + 1
      for (k <- first to last)
        if i > mid then
          array(k) = buf(j)
          j += 1
        else if j > last then
          array(k) = buf(i)
          i += 1
        else if buf(j) < buf(i) then
          array(k) = buf(j)
          j += 1
        else
          array(k) = buf(i)
          i += 1

  private def swap[T](array: Array[T], i: Int, j: Int): Unit =
    val temp = array(j)
    array(j) = array(i)
    array(i) = temp

end Sorting
