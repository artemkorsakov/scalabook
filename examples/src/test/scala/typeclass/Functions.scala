package typeclass

object Functions:
  given Conversion[Int, String] with
    def apply(i: Int): String = i.toString

  given Conversion[String, Int] with
    def apply(s: String): Int = Integer.parseInt(s)

  given Conversion[String, Boolean] with
    def apply(s: String): Boolean = s.startsWith("1")

  given Conversion[Boolean, String] with
    def apply(b: Boolean): String = b.toString
