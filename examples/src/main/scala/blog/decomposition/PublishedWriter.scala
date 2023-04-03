package blog.decomposition

@main def runWriter(): Unit =
  val w = new PublishedWriter("1Толстой", "Анна Каренина")
  w.show()

class PublishedWriter(name: String, book: String) extends Writer(name):
  override def show(): Unit = println(s"Роман '$book' написал $name")
