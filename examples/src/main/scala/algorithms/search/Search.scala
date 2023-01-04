package algorithms.search

object Search:
  def naiveSubstringSearch(searchWord: String, source: String): Int =
    (0 to source.length - searchWord.length)
      .find { i =>
        searchWord.indices.forall(j => source(j + i) == searchWord(j))
      }
      .getOrElse(-1)

  def kmpSubstringSearch(searchWord: String, source: String): Int = {
    val prefixTab = prefixTable(searchWord)
    source.indices
      .foldLeft((-1, 0)) {
        case ((foundIndex, _), _) if foundIndex > 0 => (foundIndex, 0)
        case ((_, x), i)                            =>
          val stepsX = LazyList.iterate(x)(x => prefixTab(x - 1))
          val lowerX =
            stepsX
              .find(x => x == 0 || searchWord(x) == source(i))
              .getOrElse(0)
          val newX   =
            if searchWord(lowerX) == source(i) then lowerX + 1
            else lowerX
          if newX == searchWord.length then (i - newX + 1, 0)
          else (-1, newX)
      }
      ._1
  }

  private def prefixTable(searchString: String): Vector[Int] =
    searchString.tail
      .foldLeft((0, Vector(0))) {
        case ((initialValue, prefixT), currentCharacter) =>
          val lowerValue =
            LazyList
              .iterate(initialValue)(initialValue => prefixT(initialValue - 1))
              .find(initialValue =>
                initialValue == 0 || searchString(
                  initialValue
                ) == currentCharacter
              )
              .getOrElse(0)
          val newValue   =
            if searchString(lowerValue) == currentCharacter then lowerValue + 1
            else lowerValue
          (newValue, prefixT :+ newValue)
      }
      ._2
