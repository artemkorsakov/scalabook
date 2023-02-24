# Стратегия

#### Назначение

Определение семейства алгоритмов, инкапсулирование каждого из них и создание их взаимозаменяемыми. 
Стратегия позволяет алгоритму изменяться независимо от клиентов, которые его используют.

#### Диаграмма

![Strategy](https://upload.wikimedia.org/wikipedia/ru/4/4c/Strategy_pattern.PNG)

#### Пример

`Strategy` определяет интерфейс алгоритма. 
`Context` поддерживает ссылку на текущий объект `Strategy` и перенаправляет запросы от клиентов конкретному алгоритму.

```scala
object FileMatcher:
  private val filesHere: Seq[String] =
    Seq(
      "example.txt",
      "file.txt.png",
      "1txt"
    )

  // Strategy selection
  def filesContaining(query: String): Seq[String] =
    filesMatching(_.contains(query)) // inline strategy

  def filesRegex(query: String): Seq[String] =
    filesMatching(matchRegex(query)) // using a method

  def filesEnding(query: String): Seq[String] =
    filesMatching(new FilesEnding(query).matchEnding) // lifting a method

  // matcher is a strategy
  private def filesMatching(matcher: String => Boolean): Seq[String] =
    for
      file <- filesHere
      if matcher(file)
    yield file

  // Strategies
  private def matchRegex(query: String): String => Boolean =
    (s: String) => s.matches(query)

  private class FilesEnding(query: String):
    def matchEnding(s: String): Boolean = s.endsWith(query)
```

```scala
val query = ".txt"
FileMatcher.filesContaining(query)
// res0: Seq[String] = List("example.txt", "file.txt.png")
FileMatcher.filesRegex(query)
// res1: Seq[String] = List("1txt")
FileMatcher.filesEnding(query)
// res2: Seq[String] = List("example.txt")
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Strategy_pattern)
