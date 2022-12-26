final case class Name(value: String) extends AnyVal

val name: Name = Name("€‡™µ")

final case class Person(name: Name)

Person(Name("€‡™µ"))
Person(Name("12345"))
Person(Name("Alyona"))
Person(Name("Алёна18"))
Person(Name("алёна"))
Person(Name("Алёна"))
