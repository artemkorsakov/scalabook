trait Greeting(name: String):
  val firstPart: String
  def msg = s"$firstPart $name"

trait Hello(val firstPart: String)

object EnglishGreeting extends Greeting("Bob"), Hello("Hello")

EnglishGreeting.msg
