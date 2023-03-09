trait Greeting(using val name: String):
  val firstPart: String = "Hi"
  def msg = s"$firstPart $name"

trait Hello:
  val firstPart: String = "Hello"

given String = "Bob"

object EnglishGreeting extends Greeting, Hello:
  override val firstPart: String = "Good day"

EnglishGreeting.msg