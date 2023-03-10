package algorithms.str

object NumbersDescription:
  private val hundred: Long     = 100
  private val thousand: Long    = 1000
  private val million: Long     = 1000000
  private val billion: Long     = million * thousand
  private val trillion: Long    = billion * thousand
  private val quadrillion: Long = trillion * thousand

  /** @see
    *   <a href="https://en.wikipedia.org/wiki/English_numerals">English
    *   numerals</a>
    */
  def inEnglish(number: Long): Option[String] =
    number match
      case n if n < 21 || (n < hundred && n % 10 == 0) => toEnglishBase(n)
      case n if n < hundred && n            % 10 > 0 =>
        toEnglishBase((number / 10) * 10).flatMap(f =>
          toEnglishBase(number % 10).map(s => s"$f-$s")
        )
      case n if n < thousand    => constructEnglish(n, hundred)
      case n if n < million     => constructEnglish(n, thousand)
      case n if n < billion     => constructEnglish(n, million)
      case n if n < trillion    => constructEnglish(n, billion)
      case n if n < quadrillion => constructEnglish(n, trillion)
      case _                    => None

  private def constructEnglish(n: Long, base: Long): Option[String] =
    val first =
      inEnglish(n / base).flatMap(f => toEnglishBase(base).map(s => s"$f $s"))
    val rest = n % base
    val art  = if base == hundred then " and " else " "
    val second =
      if rest == 0 then Some("") else inEnglish(rest).map(str => s"$art$str")
    first.flatMap(f => second.map(s => s"$f$s"))

  private def toEnglishBase(n: Long): Option[String] =
    n match
      case 0              => Some("")
      case 1              => Some("one")
      case 2              => Some("two")
      case 3              => Some("three")
      case 4              => Some("four")
      case 5              => Some("five")
      case 6              => Some("six")
      case 7              => Some("seven")
      case 8              => Some("eight")
      case 9              => Some("nine")
      case 10             => Some("ten")
      case 11             => Some("eleven")
      case 12             => Some("twelve")
      case 13             => Some("thirteen")
      case 14             => Some("fourteen")
      case 15             => Some("fifteen")
      case 16             => Some("sixteen")
      case 17             => Some("seventeen")
      case 18             => Some("eighteen")
      case 19             => Some("nineteen")
      case 20             => Some("twenty")
      case 30             => Some("thirty")
      case 40             => Some("forty")
      case 50             => Some("fifty")
      case 60             => Some("sixty")
      case 70             => Some("seventy")
      case 80             => Some("eighty")
      case 90             => Some("ninety")
      case 100            => Some("hundred")
      case 1000           => Some("thousand")
      case 1000000        => Some("million")
      case 1000000000     => Some("billion")
      case 1000000000000L => Some("trillion")
      case _              => None

  def inRussian(number: Long): Option[String] =
    (number match {
      case n if n < 21 => toRussianBase(n)
      case n if n < hundred =>
        toRussianBase((number / 10) * 10).flatMap(f =>
          inRussian(number % 10).map(s => s"$f $s")
        )
      case n if n < thousand =>
        toRussianBase((number / hundred) * hundred).flatMap(f =>
          inRussian(number % hundred).map(s => s"$f $s")
        )
      case n if n < million =>
        val first = number / thousand
        val firstInRussian =
          if (first % 10 == 1 && (first % hundred) / 10 != 1) {
            inRussian((first / 10) * 10).map(s => s"$s ???????? ????????????")
          } else if (first % 10 == 2 && (first % hundred) / 10 != 1) {
            inRussian((first / 10) * 10).map(s => s"$s ?????? ????????????")
          } else if (
            3 <= first % 10 && first % 10 <= 4 && (first % hundred) / 10 != 1
          ) {
            inRussian(first).map(f => f + " ????????????")
          } else {
            inRussian(first).map(f => f + " ??????????")
          }
        firstInRussian.flatMap(f =>
          inRussian(number % thousand).map(s => s"$f $s")
        )
      case n if n < billion     => constructRussian(n, million)
      case n if n < trillion    => constructRussian(n, billion)
      case n if n < quadrillion => constructRussian(n, trillion)
      case _                    => None
    }).map(ans => ans.trim)

  private def constructRussian(n: Long, base: Long): Option[String] =
    val first = n / base
    val firstInRussian =
      if first % 10 == 1 && (first % base) / 10 != 1 then
        inRussian(first).flatMap(f => toRussianBase(base).map(s => s"$f $s"))
      else if 2 <= first % 10 && first % 10 <= 4 && (first % base) / 10 != 1
      then
        inRussian(first).flatMap(f => toRussianBase(base).map(s => s"$f ${s}??"))
      else
        inRussian(first).flatMap(f =>
          toRussianBase(base).map(s => s"$f ${s}????")
        )
    firstInRussian.flatMap(f => inRussian(n % base).map(s => s"$f $s"))

  private def toRussianBase(n: Long): Option[String] =
    n match
      case 0              => Some("")
      case 1              => Some("????????")
      case 2              => Some("??????")
      case 3              => Some("??????")
      case 4              => Some("????????????")
      case 5              => Some("????????")
      case 6              => Some("??????????")
      case 7              => Some("????????")
      case 8              => Some("????????????")
      case 9              => Some("????????????")
      case 10             => Some("????????????")
      case 11             => Some("??????????????????????")
      case 12             => Some("????????????????????")
      case 13             => Some("????????????????????")
      case 14             => Some("????????????????????????")
      case 15             => Some("????????????????????")
      case 16             => Some("??????????????????????")
      case 17             => Some("????????????????????")
      case 18             => Some("????????????????????????")
      case 19             => Some("????????????????????????")
      case 20             => Some("????????????????")
      case 30             => Some("????????????????")
      case 40             => Some("??????????")
      case 50             => Some("??????????????????")
      case 60             => Some("????????????????????")
      case 70             => Some("??????????????????")
      case 80             => Some("??????????????????????")
      case 90             => Some("??????????????????")
      case 100            => Some("??????")
      case 200            => Some("????????????")
      case 300            => Some("????????????")
      case 400            => Some("??????????????????")
      case 500            => Some("??????????????")
      case 600            => Some("????????????????")
      case 700            => Some("??????????????")
      case 800            => Some("??????????????????")
      case 900            => Some("??????????????????")
      case 1000000        => Some("??????????????")
      case 1000000000     => Some("????????????????")
      case 1000000000000L => Some("????????????????")
      case _              => None
