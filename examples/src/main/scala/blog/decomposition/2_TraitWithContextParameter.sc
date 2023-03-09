trait Greeting(using val name: String):
  val firstPart: String
  def msg = s"$firstPart $name"

trait Hello:
  val firstPart: String = "Hello"

given String = "Bob"

object EnglishGreeting extends Greeting, Hello

EnglishGreeting.msg
