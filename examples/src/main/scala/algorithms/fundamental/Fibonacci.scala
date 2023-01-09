package algorithms.fundamental

import scalaz.*
import scala.annotation.tailrec

object Fibonacci:
  val memoizedFib: Int => BigInt = Memo.mutableHashMapMemo {
    case 0 => BigInt(0)
    case 1 => BigInt(1)
    case n => memoizedFib(n - 2) + memoizedFib(n - 1)
  }

  def iterativeFib(n: Int): BigInt =
    @tailrec
    def loop(a: BigInt, b: BigInt, count: Int): BigInt =
      if count == 0 then b
      else loop(b, a + b, count - 1)

    loop(BigInt(0), BigInt(1), n - 1)
