package ru.scalabook.algorithms.str

import scala.annotation.tailrec

object StringsOps:
  def isPalindrome(s: String): Boolean =
    @tailrec
    def loop(i: Int, j: Int): Boolean =
      if i >= j then true
      else if s.charAt(i) != s.charAt(j) then false
      else loop(i + 1, j - 1)

    loop(0, s.length - 1)

  def longestPalindrome(s: String): String =
    @tailrec
    def check(i: Int, j: Int): Boolean =
      if i >= j then true
      else if s.charAt(i) == s.charAt(j) then check(i + 1, j - 1)
      else false

    @tailrec
    def search(i: Int, l: Int, j: Int, m: Int): String =
      if i == s.length then s.substring(j - m + 1, j + 1)
      else if i - l > 0 && check(i - l - 1, i) then
        if l + 2 > m then search(i + 1, l + 2, i, l + 2)
        else search(i + 1, l + 2, j, m)
      else if i - l >= 0 && check(i - l, i) then
        if l + 1 > m then search(i + 1, l + 1, i, l + 1)
        else search(i + 1, l + 1, j, m)
      else search(i + 1, 1, j, m)

    if s.isEmpty then s
    else search(1, 1, 1, 1)
  end longestPalindrome

  /** <a
    * href="https://en.wikipedia.org/wiki/Longest_common_subsequence_problem">Longest
    * common subsequence problem</a>
    */
  def longestCommonSubsequence(a: String, b: String): String =
    val C: Array[Array[Int]] = lcsLength(a, b)
    backtrack(C, a, b, a.length, b.length)

  private def backtrack(
      C: Array[Array[Int]],
      aStr: String,
      bStr: String,
      x: Int,
      y: Int
  ): String =
    if x == 0 | y == 0 then ""
    else if aStr(x - 1) == bStr(y - 1) then
      backtrack(C, aStr, bStr, x - 1, y - 1) + aStr(x - 1)
    else if C(x)(y - 1) > C(x - 1)(y) then backtrack(C, aStr, bStr, x, y - 1)
    else backtrack(C, aStr, bStr, x - 1, y)

  private def lcsLength(a: String, b: String): Array[Array[Int]] =
    val C = new Array[Array[Int]](a.length + 1)
    C.indices.foreach(i => C(i) = new Array[Int](b.length + 1))
    a.indices.foreach(i => C(i)(0) = 0)
    b.indices.foreach(j => C(0)(j) = 0)
    a.indices.foreach: i =>
      b.indices.foreach: j =>
        C(i + 1)(j + 1) =
          if a(i) == b(j) then C(i)(j) + 1
          else math.max(C(i + 1)(j), C(i)(j + 1))
    C

  def longestCommonSubstring(a: String, b: String): String =
    @tailrec
    def loop(
        i: Int,
        j: Int,
        maxI: Int,
        maxJ: Int,
        acc: Map[(Int, Int), Int]
    ): String =
      val maxLength = acc.getOrElse((maxI, maxJ), 0)
      if i > a.length then
        b.substring(maxJ - maxLength, maxJ)
      else
        val length =
          if i > 0 && j > 0 && a(i - 1) == b(j - 1) then
            acc.getOrElse((i - 1, j - 1), 0) + 1
          else 0
        val nexAcc         = acc + ((i, j) -> length)
        val (nextI, nextJ) = if j == b.length then (i + 1, 0) else (i, j + 1)
        val (nextMaxI, nextMaxJ) =
          if length >= maxLength then (i, j) else (maxI, maxJ)
        loop(nextI, nextJ, nextMaxI, nextMaxJ, nexAcc)
    end loop

    loop(i = 0, j = 0, maxI = 0, maxJ = 0, acc = Map.empty[(Int, Int), Int])

  end longestCommonSubstring
end StringsOps
