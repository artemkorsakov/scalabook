var foo: "foo" = "foo"
foo = "foo"
foo = "bar"

var one: 1 = 1
one = 1
one = 2

case class Narrow[T <: Singleton](t: T)
Narrow("foo")
Narrow(1)
Narrow("foo": String)
