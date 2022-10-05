package algorithms.search

object Search:
  def naiveSubstringSearch(searchWord: String, source: String): Int =
    (0 to source.length - searchWord.length)
      .find { i => searchWord.indices.forall(j => source(j + i) == searchWord(j)) }
      .getOrElse(-1)
