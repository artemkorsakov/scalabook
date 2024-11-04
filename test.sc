type A
type B
type C

def f: A => B = ???
def g: B => C = ???
def h: A => C = f andThen g