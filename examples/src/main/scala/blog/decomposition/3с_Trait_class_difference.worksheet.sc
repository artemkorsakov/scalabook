trait A(val x: Int)

abstract class B(val x: Int)

object C extends A(1)
C.x

object D extends B(2)
D.x