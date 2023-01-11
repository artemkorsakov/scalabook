package algorithms.str

import munit.FunSuite
import algorithms.str.NumbersDescription.*

class NumbersDescriptionSuite extends FunSuite:
  test("inEnglish") {
    assertEquals[Option[String], Option[String]](inEnglish(-1), None)
    assertEquals[Option[String], Option[String]](
      inEnglish(1000000000000000L),
      None
    )
    assertEquals[Option[String], Option[String]](inEnglish(11), Some("eleven"))
    assertEquals[Option[String], Option[String]](
      inEnglish(32),
      Some("thirty-two")
    )
    assertEquals[Option[String], Option[String]](
      inEnglish(111),
      Some("one hundred and eleven")
    )
    assertEquals[Option[String], Option[String]](
      inEnglish(115),
      Some("one hundred and fifteen")
    )
    assertEquals[Option[String], Option[String]](
      inEnglish(342),
      Some("three hundred and forty-two")
    )
    assertEquals[Option[String], Option[String]](
      inEnglish(500),
      Some("five hundred")
    )
    assertEquals[Option[String], Option[String]](
      inEnglish(5000),
      Some("five thousand")
    )
    assertEquals[Option[String], Option[String]](
      inEnglish(1435),
      Some("one thousand four hundred and thirty-five")
    )
    assertEquals[Option[String], Option[String]](
      inEnglish(999999),
      Some("nine hundred and ninety-nine thousand nine hundred and ninety-nine")
    )
    assertEquals[Option[String], Option[String]](
      inEnglish(999999999999999L),
      Some(
        "nine hundred and ninety-nine trillion nine hundred and ninety-nine billion nine hundred and " +
          "ninety-nine million nine hundred and ninety-nine thousand nine hundred and ninety-nine"
      )
    )
  }

  test("inRussian") {
    assertEquals[Option[String], Option[String]](inRussian(-1), None)
    assertEquals[Option[String], Option[String]](
      inRussian(1000000000000000L),
      None
    )
    assertEquals[Option[String], Option[String]](inRussian(1), Some("один"))
    assertEquals[Option[String], Option[String]](inRussian(2), Some("два"))
    assertEquals[Option[String], Option[String]](inRussian(3), Some("три"))
    assertEquals[Option[String], Option[String]](inRussian(4), Some("четыре"))
    assertEquals[Option[String], Option[String]](inRussian(5), Some("пять"))
    assertEquals[Option[String], Option[String]](inRussian(6), Some("шесть"))
    assertEquals[Option[String], Option[String]](inRussian(7), Some("семь"))
    assertEquals[Option[String], Option[String]](inRussian(8), Some("восемь"))
    assertEquals[Option[String], Option[String]](inRussian(9), Some("девять"))
    assertEquals[Option[String], Option[String]](inRussian(10), Some("десять"))
    assertEquals[Option[String], Option[String]](
      inRussian(11),
      Some("одиннадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(12),
      Some("двенадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(13),
      Some("тринадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(14),
      Some("четырнадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(15),
      Some("пятнадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(16),
      Some("шестнадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(17),
      Some("семнадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(18),
      Some("восемнадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(19),
      Some("девятнадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(20),
      Some("двадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(21),
      Some("двадцать один")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(32),
      Some("тридцать два")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(111),
      Some("сто одиннадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(115),
      Some("сто пятнадцать")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(342),
      Some("триста сорок два")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(500),
      Some("пятьсот")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(5000),
      Some("пять тысяч")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(1435),
      Some("одна тысяча четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(2435),
      Some("две тысячи четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(3435),
      Some("три тысячи четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(4435),
      Some("четыре тысячи четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(5435),
      Some("пять тысяч четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(11435),
      Some("одиннадцать тысяч четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(12435),
      Some("двенадцать тысяч четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(19435),
      Some("девятнадцать тысяч четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(999999),
      Some("девятьсот девяносто девять тысяч девятьсот девяносто девять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(1001435),
      Some("один миллион одна тысяча четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(2002435),
      Some("два миллиона две тысячи четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(3003435),
      Some("три миллиона три тысячи четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(4004435),
      Some("четыре миллиона четыре тысячи четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(5005435),
      Some("пять миллионов пять тысяч четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(11011435),
      Some("одиннадцать миллионов одиннадцать тысяч четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(12012435),
      Some("двенадцать миллионов двенадцать тысяч четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(19019435),
      Some("девятнадцать миллионов девятнадцать тысяч четыреста тридцать пять")
    )
    assertEquals[Option[String], Option[String]](
      inRussian(999999999999999L),
      Some(
        "девятьсот девяносто девять триллионов девятьсот девяносто девять миллиардов девятьсот девяносто девять миллионов " +
          "девятьсот девяносто девять тысяч девятьсот девяносто девять"
      )
    )
  }
