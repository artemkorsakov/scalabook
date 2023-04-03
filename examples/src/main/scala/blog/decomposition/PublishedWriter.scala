package blog.decomposition

class PublishedWriter(name: String, book: String) extends Writer(name):
  override def show(): Unit = println(s"Роман '$book' написал $name")
