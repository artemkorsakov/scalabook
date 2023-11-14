package ru.scalabook.algorithms.str

import ru.scalabook.algorithms.str.NumbersDescription.*
import weaver.FunSuite

object NumbersDescriptionSuite extends FunSuite:
  test("inEnglish") {
    expect.all(
      inEnglish(-1).isEmpty,
      inEnglish(1000000000000000L).isEmpty,
      inEnglish(11).contains("eleven"),
      inEnglish(32).contains("thirty-two"),
      inEnglish(111).contains("one hundred and eleven"),
      inEnglish(115).contains("one hundred and fifteen"),
      inEnglish(342).contains("three hundred and forty-two"),
      inEnglish(500).contains("five hundred"),
      inEnglish(5000).contains("five thousand"),
      inEnglish(1435).contains("one thousand four hundred and thirty-five"),
      inEnglish(999999).contains(
        "nine hundred and ninety-nine thousand nine hundred and ninety-nine"
      ),
      inEnglish(999999999999999L).contains(
        "nine hundred and ninety-nine trillion nine hundred and ninety-nine billion nine hundred and " +
          "ninety-nine million nine hundred and ninety-nine thousand nine hundred and ninety-nine"
      )
    )
  }

  test("inRussian") {
    expect.all(
      inRussian(-1).isEmpty,
      inRussian(1000000000000000L).isEmpty,
      inRussian(1).contains("один"),
      inRussian(2).contains("два"),
      inRussian(3).contains("три"),
      inRussian(4).contains("четыре"),
      inRussian(5).contains("пять"),
      inRussian(6).contains("шесть"),
      inRussian(7).contains("семь"),
      inRussian(8).contains("восемь"),
      inRussian(9).contains("девять"),
      inRussian(10).contains("десять"),
      inRussian(11).contains("одиннадцать"),
      inRussian(12).contains("двенадцать"),
      inRussian(13).contains("тринадцать"),
      inRussian(14).contains("четырнадцать"),
      inRussian(15).contains("пятнадцать"),
      inRussian(16).contains("шестнадцать"),
      inRussian(17).contains("семнадцать"),
      inRussian(18).contains("восемнадцать"),
      inRussian(19).contains("девятнадцать"),
      inRussian(20).contains("двадцать"),
      inRussian(21).contains("двадцать один"),
      inRussian(32).contains("тридцать два"),
      inRussian(111).contains("сто одиннадцать"),
      inRussian(115).contains("сто пятнадцать"),
      inRussian(342).contains("триста сорок два"),
      inRussian(500).contains("пятьсот"),
      inRussian(5000).contains("пять тысяч"),
      inRussian(1435).contains("одна тысяча четыреста тридцать пять"),
      inRussian(2435).contains("две тысячи четыреста тридцать пять"),
      inRussian(3435).contains("три тысячи четыреста тридцать пять"),
      inRussian(4435).contains("четыре тысячи четыреста тридцать пять"),
      inRussian(5435).contains("пять тысяч четыреста тридцать пять"),
      inRussian(11435).contains("одиннадцать тысяч четыреста тридцать пять"),
      inRussian(12435).contains("двенадцать тысяч четыреста тридцать пять"),
      inRussian(19435).contains("девятнадцать тысяч четыреста тридцать пять"),
      inRussian(999999).contains(
        "девятьсот девяносто девять тысяч девятьсот девяносто девять"
      ),
      inRussian(1001435).contains(
        "один миллион одна тысяча четыреста тридцать пять"
      ),
      inRussian(2002435).contains(
        "два миллиона две тысячи четыреста тридцать пять"
      ),
      inRussian(3003435).contains(
        "три миллиона три тысячи четыреста тридцать пять"
      ),
      inRussian(4004435).contains(
        "четыре миллиона четыре тысячи четыреста тридцать пять"
      ),
      inRussian(5005435).contains(
        "пять миллионов пять тысяч четыреста тридцать пять"
      ),
      inRussian(11011435).contains(
        "одиннадцать миллионов одиннадцать тысяч четыреста тридцать пять"
      ),
      inRussian(12012435).contains(
        "двенадцать миллионов двенадцать тысяч четыреста тридцать пять"
      ),
      inRussian(19019435).contains(
        "девятнадцать миллионов девятнадцать тысяч четыреста тридцать пять"
      ),
      inRussian(999999999999999L).contains(
        "девятьсот девяносто девять триллионов девятьсот девяносто девять миллиардов девятьсот девяносто девять миллионов " +
          "девятьсот девяносто девять тысяч девятьсот девяносто девять"
      )
    )
  }
