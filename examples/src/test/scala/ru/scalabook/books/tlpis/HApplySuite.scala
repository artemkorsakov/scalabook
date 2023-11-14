package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.HApply.happly
import weaver.FunSuite

object HApplySuite extends FunSuite:
  import HList.::

  test("Testing to apply a list of HList functions to incoming HList values"):
    // два HList с типом Double :: Char :: HNil
    val y1 = 9.75 :: 'x' :: HNil
    val y2 = -2.125 :: 'X' :: HNil

    // Функции применения.
    // Тип z: (Double => Double) :: (Char => Boolean) :: HNil
    val z = ((_: Double) + .5) :: ((_: Char).isUpper) :: HNil

    // применение первого параметра HList z ко второму параметру y1
    val z1 = happly(z)(y1)

    // проверка типов
    val z1Types: Double :: Boolean :: HNil.type = z1

    // применение первого параметра HList z ко второму параметру y2
    val z2 = happly(z)(y2)

    // проверка типов
    val z2Types: Double :: Boolean :: HNil.type = z2

    // проверка значения
    expect.all(z1 == 10.25 :: false :: HNil, z2 == -1.625 :: true :: HNil)
