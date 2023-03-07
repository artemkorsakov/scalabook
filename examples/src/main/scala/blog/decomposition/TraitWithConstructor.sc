trait Greeting(val name: String):
  protected val firstPart: String
  def msg = s"$firstPart $name"

trait Hello:
  val firstPart: String = "Hello"

object EnglishGreeting extends Greeting("Bob"), Hello

EnglishGreeting.msg
