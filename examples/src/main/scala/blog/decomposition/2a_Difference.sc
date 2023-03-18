trait GreetingFirst(val name: String):
  val msg = s"GreetingFirst $name"

object GreetingFirst extends GreetingFirst("Bob")
GreetingFirst.name
GreetingFirst.msg

trait GreetingSecond:
  val name: String
  val msg = s"GreetingSecond $name"

object GreetingSecond extends GreetingSecond:
  override val name: String = "Bob"
GreetingSecond.name
GreetingSecond.msg

trait GreetingThird:
  lazy val name: String
  val msg = s"GreetingThird $name"

object GreetingThird extends GreetingThird:
  override lazy val name: String = "Bob"
GreetingThird.name
GreetingThird.msg

trait GreetingForth:
  def name: String
  val msg = s"GreetingForth $name"

object GreetingForth extends GreetingForth:
  override lazy val name: String = "Bob"
GreetingForth.name
GreetingForth.msg
