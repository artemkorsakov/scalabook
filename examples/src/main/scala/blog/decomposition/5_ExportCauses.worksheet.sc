trait Greeting(val name: String):
  val firstPart: String
  def msg = s"$firstPart $name"

trait Hello:
  val firstPart: String = "Hello"

object EnglishGreeting extends Greeting("Bob"), Hello

EnglishGreeting.name
EnglishGreeting.firstPart
EnglishGreeting.msg

object EnglishGreetingModule:
  private val greeting = new Greeting("Bob"):
    override val firstPart: String = "Hi"

  export greeting.msg

EnglishGreetingModule.msg
