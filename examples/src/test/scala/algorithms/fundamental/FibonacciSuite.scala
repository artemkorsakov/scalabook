package algorithms.fundamental

import algorithms.fundamental.Fibonacci.*
import munit.FunSuite

class FibonacciSuite extends FunSuite:
  test("Вычисление чисел Фибоначчи с помощью меморизации") {
    assertEquals(memoizedFib(30), BigInt(832040))
    assertEquals(memoizedFib(40), BigInt(102334155))
    assertEquals(memoizedFib(45), BigInt(1134903170))
    assertEquals(memoizedFib(60), BigInt(1548008755920L))
  }

  test("Итеративное вычисление чисел Фибонначи") {
    assertEquals(iterativeFib(30), BigInt(832040))
    assertEquals(iterativeFib(40), BigInt(102334155))
    assertEquals(iterativeFib(45), BigInt(1134903170))
    assertEquals(iterativeFib(60), BigInt(1548008755920L))
  }

  test("Вычисление чисел Фибонначи с логарифмической сложностью") {
    assertEquals(fastFib(30), BigInt(832040))
    assertEquals(fastFib(40), BigInt(102334155))
    assertEquals(fastFib(45), BigInt(1134903170))
    assertEquals(fastFib(60), BigInt(1548008755920L))
  }

  test(
    "Вычисление приблизительного значения числа Фибонначи с помощью 'золотого сечения'"
  ) {
    assertEquals(approximateFibonacci(30), BigInt(832040))
    assertEquals(approximateFibonacci(40), BigInt(102334155))
    assertEquals(approximateFibonacci(45), BigInt(1134903170))
    assertEquals(approximateFibonacci(100), BigInt("354224848179265190133"))
  }
