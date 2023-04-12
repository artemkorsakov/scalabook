trait Show[A]:
  extension (a: A) def show: String

case class Person(firstName: String, lastName: String)

given Show[Person] with
  extension (p: Person)
    def show: String =
      s"${p.firstName} ${p.lastName}"

def showAll[S: Show](xs: List[S]): Unit =
  xs.foreach(x => println(x.show))

showAll(List(Person("Jane", "Jackson"), Person("Mary", "Jameson")))
