package ru.scalabook.typeclass.common

object Functions:
  given Conversion[Int, String] with
    def apply(i: Int): String = i.toString

  given Conversion[String, Int] with
    def apply(s: String): Int = Integer.parseInt(s)

  given Conversion[String, Boolean] with
    def apply(s: String): Boolean = s.startsWith("1")

  given Conversion[String, Char] with
    def apply(s: String): Char = s.headOption.getOrElse('-')

  given Conversion[Boolean, String] with
    def apply(b: Boolean): String = b.toString

  given Conversion[Boolean, Char] with
    def apply(b: Boolean): Char = if b then 't' else 'f'
end Functions
