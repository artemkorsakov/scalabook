trait InvariantFunctor[F[_]]:
  extension [A](fa: F[A])
    def xmap[B](f: A => B, g: B => A): F[B]

case class ParserPrinter[A](
    parse: String => Either[String, A],
    print: A => String
)

given InvariantFunctor[ParserPrinter] with
  extension [A](pp: ParserPrinter[A])
    def xmap[B](f: A => B, g: B => A): ParserPrinter[B] =
      ParserPrinter(
        parse = (s: String) => pp.parse(s).map(f),
        print = (b: B) => pp.print(g(b))
      )

// Пример использования
val intParserPrinter: ParserPrinter[Int] = ParserPrinter(
  parse = s => Right(s.toInt),
  print = i => i.toString
)

val stringParserPrinter: ParserPrinter[String] =
  summon[InvariantFunctor[ParserPrinter]].xmap(intParserPrinter)(_.toString, _.toInt)

println(stringParserPrinter.parse("123")) // Right("123")
println(stringParserPrinter.print("456")) // "456"
