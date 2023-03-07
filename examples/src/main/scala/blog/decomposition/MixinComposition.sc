trait First:
  def hello: String = "First"

trait Second:
  def hello: String = "Second"

trait Third extends First, Second:
  override def hello: String = "Third"

object Third extends Third

Third.hello
